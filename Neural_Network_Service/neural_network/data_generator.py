import math

from dataset_estimation import estimate_dataset
import pandas as pd
import numpy as np
import random
import os
from concurrent.futures import ProcessPoolExecutor
from itertools import repeat
from neural_network_config import TRAIN_DATASET_FILEPATH, TEST_DATASET_FILEPATH, \
    TRAIN_DATASET_SIZE, TEST_DATASET_SIZE


def generate_number_finite(lower_bound, upper_bound, alpha=1.5):
    numbers = np.arange(lower_bound, upper_bound + 1)
    if lower_bound == 0:
        numbers_shifted = np.arange(lower_bound+1, upper_bound+2)
        weights = 1.0 / np.power(numbers_shifted, alpha)
        weights /= weights.sum()
    else:
        weights = 1.0 / np.power(numbers, alpha)
        weights /= weights.sum()
    return np.random.choice(numbers, p=weights)


def compute_critical_path(tasks):
    """
    Compute the longest path (in terms of the sum of story points) among the remaining (not completed) tasks.
    The dependency graph is assumed to be acyclic because each task can only depend on tasks with lower indices.
    """
    longest_path = {}
    for i, task in enumerate(tasks):
        if task['completed']:
            continue
        valid_deps = [j for j in task.get('dependencies', []) if
                      (not tasks[j]['completed']) and j in longest_path]
        if valid_deps:
            longest_path[i] = task['story_points'] + max(longest_path[j] for j in valid_deps)
        else:
            longest_path[i] = task['story_points']
    return max(longest_path.values()) if longest_path else 0


def generate_project_data_new(
        team_size=5,
        initial_tasks=50,
        external_risk_change=0.1,
        team_size_change_probability=0.0005,
        task_add_probability=0.001,
        truncation_probability=0.3,
        external_risk=0.05,
        sp_min=1, sp_max=10,
        dependency_probability=0.3,
        max_dependencies=3,
        max_team_change=4
):
    snapshots = []
    tasks = []
    team_experience = []
    # Create the initial list of tasks with dependencies.
    for i in range(initial_tasks):
        sp = random.randint(sp_min, sp_max)
        task = {
            'story_points': sp,
            'completed': False,
            'dependencies': []
        }
        if i > 0 and random.random() < dependency_probability:
            num_deps = random.randint(1, min(max_dependencies, i))
            deps = random.sample(range(0, i), num_deps)
            task['dependencies'] = deps
        tasks.append(task)

    for i in range(team_size):
        team_experience.append(generate_number_finite(0, 10))

    sum_experience = sum(team_experience)
    day = 0
    available_hours_array = []
    for i in range(team_size):
        if team_experience[i] == 0 and random.random() < 0.5 or random.random() < 0.90:
            available_hours_array.append(40)
        else:
            available_hours_array.append(30)
    available_hours = sum(available_hours_array)

    completion_coefficient = 1

    while True:
        remaining_tasks = [task for task in tasks if not task['completed']]
        total_story_points = sum(task['story_points'] for task in remaining_tasks)

        tasks_with_deps = sum(1 for task in remaining_tasks if task.get('dependencies'))
        dependency_coefficient = (tasks_with_deps / len(remaining_tasks)) if remaining_tasks else 0

        critical_path_length = compute_critical_path(tasks)

        snapshots.append([
            day,
            len(remaining_tasks),
            total_story_points,
            dependency_coefficient,
            critical_path_length,
            team_size,
            sum_experience,
            available_hours,
            external_risk
        ])

        # Terminate simulation when all tasks are complete.
        if len(remaining_tasks) == 0:
            break

        # Possibly update team size, sum_experience and available hours.
        if random.random() < team_size_change_probability:
            change = round(random.uniform(1, max_team_change))
            if team_size > change:
                change = random.choice([-change, change])
            team_size += change
            for _ in range(abs(change)):
                if change > 0:
                    team_experience.append(generate_number_finite(0, 10))
                    if random.random() < 0.90:
                        available_hours_array.append(40)
                    else:
                        available_hours_array.append(30)
                else:
                    leaving_index = random.randrange(team_size)
                    team_experience.pop(leaving_index)
                    available_hours -= available_hours_array.pop(leaving_index)
            sum_experience = sum(team_experience)
            available_hours = sum(available_hours_array)

        # With some probability, add a new task (with potential dependencies).
        if random.random() < task_add_probability:
            sp = random.randint(sp_min, sp_max)
            new_task = {
                'story_points': sp,
                'completed': False,
                'dependencies': []
            }
            new_index = len(tasks)
            if new_index > 0 and random.random() < dependency_probability:
                num_deps = random.randint(1, min(max_dependencies, new_index))
                deps = random.sample(range(0, new_index), num_deps)
                new_task['dependencies'] = deps
            tasks.append(new_task)

        daily_capacity = 0
        for i in range(team_size):
            daily_capacity += available_hours_array[i] / 5 *\
                              (math.log(team_experience[i] + 1) + 1) * 0.1
        if random.random() < external_risk:
            daily_capacity *= completion_coefficient * 0.4
        else:
            daily_capacity *= completion_coefficient * 0.5

        capacity = daily_capacity
        while capacity > 0:
            available_tasks = [task for task in tasks if not task['completed'] and
                               (not task.get('dependencies') or all(
                                   tasks[d]['completed'] for d in task['dependencies'])) and
                               task['story_points'] <= capacity]
            #print(day, daily_capacity, capacity, available_tasks)
            if not available_tasks:
                if capacity == daily_capacity:
                    completion_coefficient *= 1.5
                else:
                    completion_coefficient = 1
                break
            task_to_complete = random.choice(available_tasks)
            task_to_complete['completed'] = True
            capacity -= task_to_complete['story_points']

        if random.random() < 0.2:
            external_risk = round(
                float(np.clip(
                    external_risk + random.uniform(-external_risk_change, external_risk_change),
                    0,
                    1
                )),
                2
            )
        day += 1

    if len(snapshots) > 5 and random.random() < truncation_probability:
        trunc_idx = int(random.uniform(5, len(snapshots)))
        snapshots = snapshots[:trunc_idx]
    #print(team_size, sum_experience, len(tasks), sum(task['story_points'] for task in tasks), day)
    columns = [
        "snapshot_day",
        "remaining_tasks",
        "total_story_points",
        "dependency_coefficient",
        "critical_path_length",
        "team_size",
        "sum_experience",
        "available_hours",
        "external_risk_probability"
    ]
    df = pd.DataFrame(snapshots, columns=columns)
    return df, day


def save_project_history(folder_path, index):
    df, final_day = generate_project_data_new(
        team_size=round(random.uniform(5, 15)),
        initial_tasks=round(random.uniform(15, 150)),
        external_risk_change=round(random.uniform(0.05, 0.1), 2),
        team_size_change_probability=random.uniform(0.00001, 0.001),
        task_add_probability=random.uniform(0.0001, 0.001),
        truncation_probability=0.95,
        external_risk=round(random.uniform(0.01, 0.1), 4),
        sp_min=1, sp_max=10,
        dependency_probability=0.3,
        max_dependencies=5,
        max_team_change=4
    )
    output_path = os.path.join(folder_path, f'project_history_{index}.csv')
    print("Writing:", output_path)
    df.to_csv(output_path, index=False)

    final_day_path = os.path.join(folder_path, f'project_history_{index}_deadline.txt')
    with open(final_day_path, 'w') as f:
        f.write(str(final_day))


if __name__ == "__main__":
    try:
        with ProcessPoolExecutor() as executor:
            list(executor.map(save_project_history, repeat(TRAIN_DATASET_FILEPATH),
                              range(TRAIN_DATASET_SIZE)))
        print('Train data generated!')

        with ProcessPoolExecutor() as executor:
            list(executor.map(save_project_history, repeat(TEST_DATASET_FILEPATH),
                              range(TEST_DATASET_SIZE)))
        print('Test data generated!')
    except Exception as e:
        print("An error occurred during processing:", e)
        raise
    estimate_dataset(TRAIN_DATASET_FILEPATH)
