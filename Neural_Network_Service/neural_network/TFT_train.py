import os

os.environ['TF_GPU_ALLOCATOR'] = 'cuda_malloc_async'

import tensorflow as tf
import numpy as np
import glob
import pandas as pd
from neural_network_config import TRAIN_DATASET_FILEPATH, NUM_FEATURES, \
    CHECKPOINT_FILEPATH, TEST_DATASET_FILEPATH
from tensorflow.keras import mixed_precision
from TFT import TFT
mixed_precision.set_global_policy('mixed_float16')


# Custom Gaussian negative log-likelihood loss function
def gaussian_nll(y_true, y_pred):
    # y_true shape: (batch_size, 1) and y_pred shape: (batch_size, 2) [mu, sigma]
    mu = y_pred[:, 0]
    sigma = y_pred[:, 1] + 1e-6
    nll = 0.5 * tf.math.log(2 * np.pi) + tf.math.log(sigma) + \
          tf.square(y_true[:, 0] - mu) / (2 * tf.square(sigma))
    return tf.reduce_mean(nll)


def nll_with_sigma_penalty(y_true, y_pred, lambda_sigma=0.1):
    mu = y_pred[:, 0]
    sigma = y_pred[:, 1] + 1e-6
    nll = 0.5 * tf.math.log(2.0*np.pi) \
          + tf.math.log(sigma) \
          + tf.square(y_true[:,0] - mu) / (2.0 * tf.square(sigma))
    # штраф за слишком большую нестабильность
    sigma_penalty = lambda_sigma * tf.reduce_mean(tf.square(sigma))
    return tf.reduce_mean(nll) + sigma_penalty


def mae_mu(y_true, y_pred):
    # y_pred shape is (batch_size, 2): use only the first column (mu).
    mu = y_pred[:, 0]
    # Assuming y_true is also of shape (batch_size, 1) or (batch_size,)
    return tf.reduce_mean(tf.abs(y_true[:, 0] - mu))


class CSVDataGenerator(tf.keras.utils.Sequence):
    def __init__(self, folder_path, batch_size=32, shuffle=True):
        """
        folder_path: Path to the folder that contains CSV files and their associated deadline files.
        batch_size: How many CSV files (samples) per batch.
        shuffle: Whether to shuffle the order of CSV files each epoch.
        """
        self.folder_path = folder_path
        self.batch_size = batch_size
        self.files = sorted(glob.glob(os.path.join(folder_path, "*.csv")))
        self.shuffle = shuffle
        self.on_epoch_end()

    def __len__(self):
        return int(np.ceil(len(self.files) / float(self.batch_size)))

    def __getitem__(self, index):
        """
        Generate one batch of data.
        For each CSV file in this batch, it reads the project history and loads the corresponding
        deadline from a text file.
        """
        batch_files = self.files[index * self.batch_size:(index + 1) * self.batch_size]

        dataset = []
        deadlines = []

        for file in batch_files:
            df = pd.read_csv(file)
            sequence = df.values.tolist()
            dataset.append(sequence)

            deadline_file = file.replace('.csv', '_deadline.txt')
            if os.path.exists(deadline_file):
                with open(deadline_file, 'r') as f:
                    deadline = float(f.read().strip())
            else:
                deadline = sequence[-1][-1]
            deadlines.append(deadline)

        X = tf.keras.preprocessing.sequence.pad_sequences(dataset, dtype='float16', padding='post')
        y = np.array(deadlines, dtype='float16').reshape(-1, 1)
        return X, y

    def on_epoch_end(self):
        if self.shuffle:
            np.random.shuffle(self.files)


def adapt_normalizer():
    normalizer = tf.keras.layers.Normalization(axis=-1)
    all_data = []
    for file in sorted(glob.glob(os.path.join(TRAIN_DATASET_FILEPATH, "*.csv"))):
        df = pd.read_csv(file)
        all_data.append(df.values)
    all_data = np.vstack(all_data)
    return normalizer.adapt(all_data)


if __name__ == "__main__":
    checkpoint_callback = tf.keras.callbacks.ModelCheckpoint(
        filepath=CHECKPOINT_FILEPATH,
        monitor='loss',
        save_best_only=True,
        save_weights_only=True,
        verbose=1,
        mode='min',
        save_freq='epoch'
    )

    lr_scheduler = tf.keras.callbacks.ReduceLROnPlateau(
        monitor='loss',
        factor=0.5,
        patience=5,
        verbose=1,
        min_lr=1e-6
    )
    opt = tf.keras.optimizers.Adam(learning_rate=0.000005)

    train_generator = CSVDataGenerator(TRAIN_DATASET_FILEPATH, batch_size=50, shuffle=True)
    test_generator = CSVDataGenerator(TEST_DATASET_FILEPATH, batch_size=50, shuffle=False)
    val_generator = CSVDataGenerator('./validation_dataset', batch_size=50, shuffle=False)

    normalizer = adapt_normalizer()
    model = TFT(
        d_model=64, num_heads=2,
        ff_dim=128, num_encoder_layers=2,
        dropout_rate=0.1, forecast_horizon=1,
        normalizer=normalizer,
    )
    model.build(input_shape=(None, None, NUM_FEATURES))
    _ = model(tf.zeros((1, 10, NUM_FEATURES)))
    model.load_weights(CHECKPOINT_FILEPATH)

    model.compile(optimizer=opt, loss=gaussian_nll, metrics=[mae_mu])

    model.summary()
    model.fit(train_generator,
              epochs=50,
              validation_data=val_generator,
              callbacks=[lr_scheduler, checkpoint_callback])

    model.save('TFT.keras')
    print("Model trained and saved!")

    model.evaluate(test_generator)