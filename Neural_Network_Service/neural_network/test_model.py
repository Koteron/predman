import math

from TFT import initialize_model
from TFT_train import CSVDataGenerator
from neural_network_config import TEST_DATASET_FILEPATH
import numpy as np

if __name__ == "__main__":
    test_generator = CSVDataGenerator(TEST_DATASET_FILEPATH, batch_size=50, shuffle=False)

    model = initialize_model()

    y_preds = []
    y_trues = []

    for idx in range(len(test_generator)):
        X_batch, y_batch = test_generator[idx]
        preds = model.predict(X_batch)
        print(preds)
        input()
        y_preds.append(preds)
        y_trues.append(y_batch)

    y_preds = np.concatenate(y_preds, axis=0)
    y_trues = np.concatenate(y_trues, axis=0)

    mu = y_preds[:, 0]
    sigma = y_preds[:, 1]
    absolute_errors = np.abs(y_trues.flatten() - mu)

    thresholds = [1, 2, 3, 5]

    stats = {}
    for thr in thresholds:
        stats[f"Absolute error < {thr}"] = np.sum(absolute_errors < thr)

    print("Evaluation statistics:")
    for key, value in stats.items():
        print(f"{key}: {value}")

    total = len(absolute_errors)
    for thr in thresholds:
        percent = 100 * np.sum(absolute_errors < thr) / total
        print(f"Percent of test cases with absolute error < {thr}: {percent:.2f}%")

    # 1) Coverage внутри kσ
    print("Coverage within k·σ:")
    for k in (1, 2, 3):
        count = np.sum(absolute_errors <= k * sigma)
        print(f"  |error| ≤ {k}σ : {count}/{total} = {100 * count / total:.2f}%")

    # 2) Predictive confidence на каждом примере:
    #    P(|X−μ| ≤ |y_true−μ|) = 2Φ(z)−1, z = |error|/σ
    confidences = 2 * (0.5 * (
                1 + np.vectorize(lambda z: math.erf(z / math.sqrt(2)))(absolute_errors / sigma))) - 1
    # упрощённо: confidences = erf(z/√2)

    mean_conf = np.mean(confidences)
    print(f"\nMean predictive confidence: {mean_conf * 100:.2f}%")

    # Доля примеров с confidence ≥ стандартных уровней
    for lvl in (0.68, 0.95, 0.997):
        cnt = np.sum(confidences >= lvl)
        print(f"  Confidence ≥ {round(lvl * 100, 1)}% : {cnt}/{total} = {100 * cnt / total:.2f}%")
