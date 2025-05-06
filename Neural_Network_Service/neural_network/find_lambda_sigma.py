import math

from TFT import initialize_model, CSVDataGenerator
from neural_network_config import TRAIN_DATASET_FILEPATH
import tensorflow as tf
import numpy as np

if __name__ == "__main__":
    train_generator = CSVDataGenerator(TRAIN_DATASET_FILEPATH, batch_size=50, shuffle=True)
    val_generator = CSVDataGenerator('./validation_dataset', batch_size=50, shuffle=False)

    model = initialize_model()

    best = {'lambda_sigma': None, 'conf_diff': 1e9}
    for lambda_sigma in [1e-4, 5e-4, 1e-3, 5e-3, 1e-2, 5e-2, 1e-1]:
        # компилируем модель с этой регуляризацией
        def loss_fn(y_true, y_pred, ls=lambda_sigma):
            mu    = y_pred[:, 0]
            sigma = y_pred[:, 1] + 1e-6
            nll   = 0.5 * tf.math.log(2.0*np.pi) \
                  + tf.math.log(sigma) \
                  + tf.square(y_true[:,0] - mu) / (2.0 * tf.square(sigma))
            sigma_penalty = ls * tf.reduce_mean(tf.square(sigma))
            return tf.reduce_mean(nll) + sigma_penalty

        model.compile(optimizer='adam', loss=loss_fn)
        history = model.fit(train_generator, epochs=5, validation_data=val_generator)

        # посчитать mean predictive confidence на валидации:
        # (как мы делали раньше)

        y_preds = []
        y_trues = []
        for idx in range(len(val_generator)):
            X_batch, y_batch = val_generator[idx]
            preds = model.predict(X_batch)
            y_preds.append(preds)
            y_trues.append(y_batch)

        y_preds = np.concatenate(y_preds, axis=0)
        y_trues = np.concatenate(y_trues, axis=0)

        mu = y_preds[:, 0]
        sigma = y_preds[:, 1]
        absolute_errors = np.abs(y_trues.flatten() - mu)
        # 2) Predictive confidence на каждом примере:
        #    P(|X−μ| ≤ |y_true−μ|) = 2Φ(z)−1, z = |error|/σ
        confidences = 2 * (0.5 * (
                1 + np.vectorize(lambda z: math.erf(z / math.sqrt(2)))(
            absolute_errors / sigma))) - 1
        # упрощённо: confidences = erf(z/√2)

        mean_conf = np.mean(confidences)
        diff = abs(mean_conf - 0.68)  # стремимся к 68%
        if diff < best['conf_diff']:
            best.update({'lambda_sigma': lambda_sigma, 'conf_diff': diff, 'conf': mean_conf})

    print("Лучший lambda_sigma =", best['lambda_sigma'],
          "  mean_confidence =", best['conf'] )
