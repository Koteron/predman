import os
import heapq
import statistics
from neural_network_config import TRAIN_DATASET_FILEPATH


def estimate_dataset(directory):

    contents = []

    for filename in os.listdir(directory):
        if filename.endswith('.txt'):
            filepath = os.path.join(directory, filename)
            with open(filepath, 'r', encoding='utf-8') as file:
                file_content = file.read().strip()
                try:
                    value = int(file_content)
                    contents.append((filepath, value))
                except ValueError:
                    print(f"Skipping file '{filepath}': content is not a valid integer.")

    if not contents:
        print("No valid numeric file contents found.")
    else:
        max_pair = max(contents, key=lambda pair: pair[1])
        average = sum(pair[1] for pair in contents) / len(contents)
        median = statistics.median(pair[1] for pair in contents)
        five_smallest = heapq.nsmallest(5, contents, key=lambda pair: pair[1])
        five_biggest = heapq.nlargest(5, contents, key=lambda pair: pair[1])

        print("=" * 50)
        print("DATASET STATISTICS")
        print("=" * 50)
        print(f"Total Files Processed : {len(contents)}")
        print("-" * 50)
        print(f"Maximum:\n  File: {max_pair[0]}\n  Value: {max_pair[1]}")
        print(f"Average: {average:.2f}")
        print(f"Median : {median}")
        print("-" * 50)

        print("FIVE SMALLEST:")
        for filepath, value in five_smallest:
            print(f"  File: {filepath} -> Value: {value}")

        print("-" * 50)
        print("FIVE BIGGEST:")
        for filepath, value in five_biggest:
            print(f"  File: {filepath} -> Value: {value}")
        print("=" * 50)


if __name__ == "__main__":
    estimate_dataset(TRAIN_DATASET_FILEPATH)
