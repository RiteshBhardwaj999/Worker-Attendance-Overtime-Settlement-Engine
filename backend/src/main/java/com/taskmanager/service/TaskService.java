package com.taskmanager.service;

import com.taskmanager.dto.request.CreateTaskRequest;
import com.taskmanager.dto.request.UpdateTaskRequest;
import com.taskmanager.dto.request.UpdateTaskStatusRequest;
import com.taskmanager.dto.response.TaskResponse;
import com.taskmanager.entity.Project;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.enums.Role;
import com.taskmanager.enums.TaskStatus;
import com.taskmanager.exception.ForbiddenException;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.repository.ProjectMemberRepository;
import com.taskmanager.repository.ProjectRepository;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository memberRepository;
    private final ProjectService projectService;

    public List<TaskResponse> listTasks(UUID callerId, UUID projectId) {
        projectService.requireMember(callerId, projectId);
        return taskRepository.findAllByProject_Id(projectId)
                .stream().map(TaskResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public TaskResponse createTask(UUID callerId, UUID projectId, CreateTaskRequest req) {
        projectService.requireAdmin(callerId, projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        User creator = userRepository.findById(callerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        User assignee = null;
        if (req.getAssigneeId() != null) {
            if (!memberRepository.existsByUser_IdAndProject_Id(req.getAssigneeId(), projectId)) {
                throw new IllegalArgumentException("Assignee must be a project member");
            }
            assignee = userRepository.findById(req.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
        }

        Task task = Task.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .status(req.getStatus() != null ? req.getStatus() : TaskStatus.TODO)
                .dueDate(req.getDueDate())
                .project(project)
                .creator(creator)
                .assignee(assignee)
                .build();
        return TaskResponse.from(taskRepository.save(task));
    }

    public TaskResponse getTask(UUID callerId, UUID projectId, UUID taskId) {
        projectService.requireMember(callerId, projectId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse updateTask(UUID callerId, UUID projectId, UUID taskId, UpdateTaskRequest req) {
        projectService.requireAdmin(callerId, projectId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (req.getTitle() != null && !req.getTitle().isBlank()) task.setTitle(req.getTitle());
        if (req.getDescription() != null) task.setDescription(req.getDescription());
        if (req.getStatus() != null) task.setStatus(req.getStatus());
        if (req.getDueDate() != null) task.setDueDate(req.getDueDate());

        if (req.getAssigneeId() != null) {
            if (!memberRepository.existsByUser_IdAndProject_Id(req.getAssigneeId(), projectId)) {
                throw new IllegalArgumentException("Assignee must be a project member");
            }
            User assignee = userRepository.findById(req.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
            task.setAssignee(assignee);
        }

        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateTaskStatus(UUID callerId, UUID projectId, UUID taskId, UpdateTaskStatusRequest req) {
        projectService.requireMember(callerId, projectId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        boolean isAdmin = memberRepository
                .findByUser_IdAndProject_IdAndRole(callerId, projectId, Role.ADMIN).isPresent();
        boolean isAssignee = task.getAssignee() != null && task.getAssignee().getId().equals(callerId);

        if (!isAdmin && !isAssignee) {
            throw new ForbiddenException("Only the task assignee or an admin can update the status");
        }

        task.setStatus(req.getStatus());
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(UUID callerId, UUID projectId, UUID taskId) {
        projectService.requireAdmin(callerId, projectId);
        taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        taskRepository.deleteById(taskId);
    }
}
