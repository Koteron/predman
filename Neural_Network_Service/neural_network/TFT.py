import os

os.environ['TF_GPU_ALLOCATOR'] = 'cuda_malloc_async'

import tensorflow as tf
from tensorflow.keras import layers, Model
from .neural_network_config import NUM_FEATURES, CHECKPOINT_FILEPATH


# ==============================================================================
# Utility Functions (for non-variable-creating operations)
# ==============================================================================
def add_and_norm(x_list):
    """Applies skip connection followed by layer normalization."""
    tmp = tf.keras.layers.Add()(x_list)
    tmp = tf.keras.layers.LayerNormalization()(tmp)
    return tmp


def initialize_model(checkpoint_filepath=CHECKPOINT_FILEPATH):
    model = TFT(
        d_model=64, num_heads=2,
        ff_dim=128, num_encoder_layers=2,
        dropout_rate=0.1, forecast_horizon=1,
    )
    model.build(input_shape=(None, None, NUM_FEATURES))
    _ = model(tf.zeros((1, 10, NUM_FEATURES)))
    model.load_weights(checkpoint_filepath)
    return model


# ==============================================================================
# Custom Gated Residual Network (GRN) as a Layer
# ==============================================================================
class GatedResidualNetwork(layers.Layer):
    def __init__(self, hidden_layer_size, output_size=None, dropout_rate=None,
                 use_time_distributed=True, **kwargs):
        """
        Args:
          hidden_layer_size: Internal state size.
          output_size: Desired output size. If None, defaults to hidden_layer_size.
          dropout_rate: Optional dropout rate.
          use_time_distributed: Whether to apply layers across the time dimension.
        """
        super(GatedResidualNetwork, self).__init__(**kwargs)
        self.hidden_layer_size = hidden_layer_size
        self.output_size = output_size if output_size is not None else hidden_layer_size
        self.use_time_distributed = use_time_distributed
        self.dropout_rate = dropout_rate

        # Set up the skip connection: if output size is different, project the input.
        if output_size is None:
            self.skip_layer = None  # Identity, no projection needed.
        else:
            dense = tf.keras.layers.Dense(self.output_size)
            if self.use_time_distributed:
                self.skip_layer = tf.keras.layers.TimeDistributed(dense)
            else:
                self.skip_layer = dense

        # Main feed-forward pathway.
        self.linear1 = tf.keras.layers.Dense(self.hidden_layer_size, activation=None)
        self.linear2 = tf.keras.layers.Dense(self.hidden_layer_size, activation=None)
        if self.use_time_distributed:
            self.linear1 = tf.keras.layers.TimeDistributed(self.linear1)
            self.linear2 = tf.keras.layers.TimeDistributed(self.linear2)

        self.elu = tf.keras.layers.Activation('elu')

        # Gating part: two Dense layers, one with sigmoid activation.
        self.glu_dense1 = tf.keras.layers.Dense(self.output_size)
        self.glu_dense2 = tf.keras.layers.Dense(self.output_size, activation='sigmoid')
        if self.use_time_distributed:
            self.glu_dense1 = tf.keras.layers.TimeDistributed(self.glu_dense1)
            self.glu_dense2 = tf.keras.layers.TimeDistributed(self.glu_dense2)

        self.layernorm = tf.keras.layers.LayerNormalization()

        if self.dropout_rate is not None:
            self.dropout = tf.keras.layers.Dropout(self.dropout_rate)
        else:
            self.dropout = None

    def __call__(self, x, additional_context=None):
        # Skip connection: project if necessary.
        if self.skip_layer is not None:
            skip = self.skip_layer(x)
        else:
            skip = x

        # Main pathway.
        hidden = self.linear1(x)
        if additional_context is not None:
            # Note: Depending on your architecture, you might want a separate linear layer for the context.
            hidden += self.linear1(additional_context)
        hidden = self.elu(hidden)
        hidden = self.linear2(hidden)
        if self.dropout is not None:
            hidden = self.dropout(hidden)
        # Gating: obtain two transformations and multiply.
        activation_layer = self.glu_dense1(hidden)
        gated_layer = self.glu_dense2(hidden)
        out = activation_layer * gated_layer

        # Add & norm.
        return self.layernorm(skip + out)


# ==============================================================================
# Positional Encoding Layer
# ==============================================================================
class PositionalEncoding(layers.Layer):
    def __init__(self, d_model, **kwargs):
        super(PositionalEncoding, self).__init__(**kwargs)
        self.d_model = d_model

    def __call__(self, inputs):
        # inputs shape: (batch_size, seq_len, d_model)
        seq_len = tf.shape(inputs)[1]

        # Используем dtype самого тензора inputs
        dtype = inputs.dtype

        # позиции и индексы
        pos = tf.cast(tf.range(seq_len)[:, tf.newaxis], dtype=dtype)    # shape (seq_len,1)
        i   = tf.cast(tf.range(self.d_model)[tf.newaxis, :], dtype=dtype)  # shape (1,d_model)

        # частоты углов
        angle_rates = 1 / tf.pow(
            tf.constant(10000.0, dtype=dtype),
            (2 * (i // 2)) / tf.cast(self.d_model, dtype)
        )
        angle_rads = pos * angle_rates  # shape (seq_len, d_model)

        # синусы и косинусы
        sin_part = tf.math.sin(angle_rads[:, 0::2])
        cos_part = tf.math.cos(angle_rads[:, 1::2])

        # восстанавливаем interleaved формат [sin, cos, sin, cos…]
        pos_encoding = tf.reshape(
            tf.transpose(  # (2, seq_len, d_model/2) -> (seq_len,2,d_model/2)
                tf.reshape(
                    tf.concat([sin_part, cos_part], axis=-1),
                    (seq_len, 2, self.d_model // 2)
                )
            ),
            (seq_len, self.d_model)
        )
        # добавляем по последней оси
        return inputs + pos_encoding



# ==============================================================================
# Transformer Block
# ==============================================================================
class TransformerBlock(layers.Layer):
    def __init__(self, d_model, num_heads, ff_dim, dropout_rate=0.1):
        super(TransformerBlock, self).__init__()
        self.att = tf.keras.layers.MultiHeadAttention(num_heads=num_heads, key_dim=d_model)
        self.ffn = tf.keras.Sequential([
            tf.keras.layers.Dense(ff_dim, activation='relu'),
            tf.keras.layers.Dense(d_model)
        ])
        self.layernorm1 = tf.keras.layers.LayerNormalization(epsilon=1e-6)
        self.layernorm2 = tf.keras.layers.LayerNormalization(epsilon=1e-6)
        self.dropout1 = tf.keras.layers.Dropout(dropout_rate)
        self.dropout2 = tf.keras.layers.Dropout(dropout_rate)

    def __call__(self, inputs, training=False):
        attn_output = self.att(inputs, inputs)
        attn_output = self.dropout1(attn_output, training=training)
        out1 = self.layernorm1(inputs + attn_output)
        ffn_output = self.ffn(out1)
        ffn_output = self.dropout2(ffn_output, training=training)
        return self.layernorm2(out1 + ffn_output)


# ==============================================================================
# TFT Model
# ==============================================================================
class TFT(Model):
    def __init__(self, d_model=64, num_heads=4, ff_dim=128,
                 num_encoder_layers=2, dropout_rate=0.1, forecast_horizon=1,
                 normalizer=None):
        super(TFT, self).__init__()
        self.d_model = d_model
        self.dropout_rate = dropout_rate

        # 1. Masking and input normalization.
        self.masking = tf.keras.layers.Masking(mask_value=0.0)
        self.normalizer = normalizer  # Expecting an instance of tf.keras.layers.Normalization

        # 2. Initial projection of input features.
        self.dense_proj = tf.keras.layers.TimeDistributed(
            tf.keras.layers.Dense(d_model)
        )

        # 3. Positional Encoding.
        self.pos_encoding = PositionalEncoding(d_model=d_model)

        # 4. LSTM Encoder to learn local temporal patterns.
        self.lstm_enc = tf.keras.layers.LSTM(d_model, return_sequences=True)

        # 5. Instantiate GRN layers.
        self.grn_proj = GatedResidualNetwork(
            hidden_layer_size=d_model,
            dropout_rate=dropout_rate,
            use_time_distributed=True
        )
        self.grn_lstm = GatedResidualNetwork(
            hidden_layer_size=d_model,
            dropout_rate=dropout_rate,
            use_time_distributed=True
        )

        # 6. Transformer Blocks for global attention.
        self.transformer_blocks = [
            TransformerBlock(d_model, num_heads, ff_dim, dropout_rate)
            for _ in range(num_encoder_layers)
        ]
        # Optionally, add a GRN after each transformer block.
        self.grn_transformer = [
            GatedResidualNetwork(
                hidden_layer_size=d_model,
                dropout_rate=dropout_rate,
                use_time_distributed=False
            )
            for _ in range(num_encoder_layers)
        ]

        # 7. Global pooling and final output layer for forecast.
        self.global_pool = tf.keras.layers.GlobalAveragePooling1D()
        self.output_layer = tf.keras.layers.Dense(2 * forecast_horizon, activation=None)

    def __call__(self, inputs, training=False):
        # Optionally apply input normalization.
        if self.normalizer is not None:
            inputs = self.normalizer(inputs)

        # Mask padded values.
        x = self.masking(inputs)

        # Project inputs to a higher-dimensional space.
        x = self.dense_proj(x)

        # GRN 1: Apply GRN after the initial projection (variable selection/gating).
        x = self.grn_proj(x)

        # Apply positional encoding.
        x = self.pos_encoding(x)

        # Process with LSTM to capture local temporal dynamics.
        x = self.lstm_enc(x)

        # GRN 2: Refine LSTM outputs with another GRN.
        x = self.grn_lstm(x)

        # Process through transformer blocks.
        for block, grn in zip(self.transformer_blocks, self.grn_transformer):
            x = block(x, training=training)
            x = grn(x)

        # Global pooling and final output projection.
        x = self.global_pool(x)
        raw_output = self.output_layer(x)
        mu = raw_output[:, 0:1]
        sigma = tf.nn.softplus(raw_output[:, 1:2])
        return tf.concat([mu, sigma], axis=-1)
