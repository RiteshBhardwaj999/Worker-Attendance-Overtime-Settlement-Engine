package com.taskmanager.service;

import com.taskmanager.dto.request.CreateProjectRequest;
import com.taskmanager.dto.response.ProjectResponse;
import com.taskmanager.entity.Project;
import com.taskmanager.entity.ProjectMember;
import com.taskmanager.entity.User;
import com.taskmanager.enums.Role;
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
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProjectResponse createProject(UUID userId, CreateProjectRequest req) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Project project = Project.builder()
                .name(req.getName())
                .description(req.getDescription())
                .owner(owner)
                .build();
        projectRepository.save(project);

        ProjectMember membership = ProjectMember.builder()
                .user(owner)
                .project(project)
                .role(Role.ADMIN)
                .build();
        memberRepository.save(membership);

        return ProjectResponse.from(project);
    }

    public List<ProjectResponse> getUserProjects(UUID userId) {
        return projectRepository.findAllByMemberUserId(userId)
                .stream().map(ProjectResponse::from).collect(Collectors.toList());
    }

    public ProjectResponse getProjectById(UUID userId, UUID projectId) {
        requireMember(userId, projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectResponse updateProject(UUID userId, UUID projectId, CreateProjectRequest req) {
        requireAdmin(userId, projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        if (req.getName() != null && !req.getName().isBlank()) project.setName(req.getName());
        if (req.getDescription() != null) project.setDescription(req.getDescription());
        return ProjectResponse.from(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(UUID userId, UUID projectId) {
        requireAdmin(userId, projectId);
        taskRepository.deleteAllByProject_Id(projectId);
        memberRepository.deleteAllByProject_Id(projectId);
        projectRepository.deleteById(projectId);
    }

    public void requireMember(UUID userId, UUID projectId) {
        if (!memberRepository.existsByUser_IdAndProject_Id(userId, projectId)) {
            throw new ForbiddenException("You are not a member of this project");
        }
    }

    public void requireAdmin(UUID userId, UUID projectId) {
        memberRepository.findByUser_IdAndProject_IdAndRole(userId, projectId, Role.ADMIN)
                .orElseThrow(() -> new ForbiddenException("Admin access required"));
    }
}
