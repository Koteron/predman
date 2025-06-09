package com.predman.content.service;

import com.predman.content.common.TaskStatus;
import com.predman.content.dto.task.SortedTasksDto;
import com.predman.content.dto.task.TaskCreationDto;
import com.predman.content.dto.task.TaskDto;
import com.predman.content.dto.task.TaskUpdateDto;
import com.predman.content.entity.Task;
import com.predman.content.exception.ForbiddenException;
import com.predman.content.exception.NotFoundException;
import com.predman.content.mapper.TaskMapper;
import com.predman.content.repository.TaskRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    public final TaskRepository taskRepository;
    public final TaskMapper taskMapper;
    public final EntityManager entityManager;
    private record NextPrev(Task next, Task prev) {}

    @Override
    @Transactional
    public TaskDto create(TaskCreationDto taskCreationDto) {
        NextPrev nextPrev = findNextPrevByNewNext(taskCreationDto.next(), TaskStatus.PLANNED);

        if (nextPrev.next() != null && !nextPrev.next().getStatus().equals(TaskStatus.PLANNED)) {
            throw new ForbiddenException("Cannot link a new task to not a planned task");
        }

        Task newTask = taskMapper.convertToNewTaskEntity(taskCreationDto, nextPrev.next);
        if (nextPrev.prev() != null) {
            nextPrev.prev().setNext(newTask);
            taskRepository.save(nextPrev.prev());
        }
        return taskMapper.convertToTaskDto(taskRepository.save(newTask));
    }

    @Override
    @Transactional
    public TaskDto update(Task originalTask, TaskUpdateDto taskUpdateDto) {
        Task updatedTask = Task.builder()
                .id(originalTask.getId())
                .project(originalTask.getProject())
                .name(taskUpdateDto.name() == null ? originalTask.getName() : taskUpdateDto.name())
                .description(taskUpdateDto.description() == null ?
                        originalTask.getDescription() : taskUpdateDto.description())
                .status(taskUpdateDto.status() == null ? originalTask.getStatus() : taskUpdateDto.status())
                .storyPoints(taskUpdateDto.storyPoints() == null ?
                        originalTask.getStoryPoints() : taskUpdateDto.storyPoints())
                .updatedAt(LocalDateTime.now())
                .createdAt(originalTask.getCreatedAt())
                .build();
        if (taskUpdateDto.isNextUpdated()) {
            Task prevOriginal = findPrevByCurrentEntity(originalTask);
            if (prevOriginal != null) {
                Task nextOriginal = originalTask.getNext();
                prevOriginal.setNext(nextOriginal);
                taskRepository.save(prevOriginal);
            }
            NextPrev nextPrev = findNextPrevByNewNext(taskUpdateDto.next(),
                    taskUpdateDto.status() == null ? originalTask.getStatus() : taskUpdateDto.status());
            if (taskUpdateDto.status() != null && nextPrev.next() != null &&
                    !nextPrev.next().getStatus().equals(taskUpdateDto.status())) {
                throw new ForbiddenException("Cannot link a task to a task of different status " +
                        "without changing its status");
            }
            updatedTask.setNext(nextPrev.next());
            if (nextPrev.prev() != null) {
                nextPrev.prev().setNext(updatedTask);
                taskRepository.save(nextPrev.prev());
            }
        }
        else {
            updatedTask.setNext(originalTask.getNext());
        }

        return taskMapper.convertToTaskDto(taskRepository.save(updatedTask));
    }

    @Override
    public TaskDto getById(UUID id) {
        return taskMapper.convertToTaskDto(taskRepository.findById(id).orElseThrow(()
                -> new NotFoundException("Task not found")));
    }

    @Override
    public Task getEntityById(UUID id) {
        Task task = entityManager.find(Task.class, id);
        if (task == null) {
            throw new NotFoundException("Task not found");
        }
        return task;
    }

    @Override
    public SortedTasksDto getSortedAllByProjectId(UUID projectId) {
        return sortLinkedTasks(taskRepository.findByProjectId(projectId));
    }

    @Override
    public List<TaskDto> getAllByProjectId(UUID projectId) {
        return taskRepository.findByProjectId(projectId).stream().map(taskMapper::convertToTaskDto).toList();
    }

    @Override
    @Transactional
    public void deleteById(UUID taskId) {
        Task currentTask = taskRepository.findById(taskId).orElseThrow(() ->
                new NotFoundException("Could not find a task with such ID to link to"));
        Task next = currentTask.getNext();
        Task prev = taskRepository.findByNext(currentTask).stream()
                .filter((task)-> task.getStatus().equals(currentTask.getStatus())).findFirst().orElse(null);

        if (prev != null) {
            if (next != null) {
                next = entityManager.merge(next);
            }
            prev.setNext(next);
            taskRepository.save(prev);
        }

        taskRepository.deleteById(taskId);
    }

    private SortedTasksDto sortLinkedTasks(List<Task> tasks) {
        Set<UUID> plannedNextIds = new HashSet<>();
        Set<UUID> inProgressNextIds = new HashSet<>();
        Set<UUID> completedNextIds = new HashSet<>();

        for (Task task : tasks) {
            Task next = task.getNext();
            if (next != null) {
                switch (task.getStatus()) {
                    case PLANNED -> plannedNextIds.add(next.getId());
                    case IN_PROGRESS -> inProgressNextIds.add(next.getId());
                    case COMPLETED -> completedNextIds.add(next.getId());
                }
            }
        }

        Task headPlanned = null;
        Task headInProgress = null;
        Task headCompleted = null;

        for (Task task : tasks) {
            UUID id = task.getId();
            switch (task.getStatus()) {
                case PLANNED:
                    if (!plannedNextIds.contains(id)) headPlanned = task;
                    break;
                case IN_PROGRESS:
                    if (!inProgressNextIds.contains(id)) headInProgress = task;
                    break;
                case COMPLETED:
                    if (!completedNextIds.contains(id)) headCompleted = task;
                    break;
            }

            if (headPlanned != null && headInProgress != null && headCompleted != null) break;
        }

        if (headPlanned == null && !plannedNextIds.isEmpty()) {
            throw new IllegalStateException("Planned linked list has cycle or is broken");
        }
        else if (headInProgress == null && !inProgressNextIds.isEmpty()) {
            throw new IllegalStateException("In_progress linked list has cycle or is broken");
        }
        else if (headCompleted == null && !completedNextIds.isEmpty()) {
            throw new IllegalStateException("Completed linked list has cycle or is broken");
        }

        List<Task> sortedPlanned = new ArrayList<>();
        List<Task> sortedInProgress = new ArrayList<>();
        List<Task> sortedCompleted = new ArrayList<>();
        while (headPlanned != null) {
            sortedPlanned.add(headPlanned);
            headPlanned = headPlanned.getNext();
        }
        while (headInProgress != null) {
            sortedInProgress.add(headInProgress);
            headInProgress = headInProgress.getNext();
        }
        while (headCompleted != null) {
            sortedCompleted.add(headCompleted);
            headCompleted = headCompleted.getNext();
        }
        return SortedTasksDto.builder()
                .planned(sortedPlanned.stream().map(taskMapper::convertToTaskDto).toList())
                .inprogress(sortedInProgress.stream().map(taskMapper::convertToTaskDto).toList())
                .completed(sortedCompleted.stream().map(taskMapper::convertToTaskDto).toList())
                .build();
    }

    private Task findPrevByCurrentEntity(Task task) {
        List<Task> list = taskRepository.findByNext(task);
        return list.isEmpty() ? null : list.getFirst();
    }

    private NextPrev findNextPrevByNewNext(UUID nextId, TaskStatus status) {
        Task nextTask;
        Task prevTask;
        if (nextId != null) {
            nextTask = taskRepository.findById(nextId).orElseThrow(() ->
                    new NotFoundException("Could not find a task with such ID to link to"));
        } else {
            nextTask = null;
        }
        prevTask = taskRepository.findByNext(nextTask).stream()
                .filter((task)-> task.getStatus().equals(status)).findFirst().orElse(null);
        return new NextPrev(nextTask, prevTask);
    }
}
