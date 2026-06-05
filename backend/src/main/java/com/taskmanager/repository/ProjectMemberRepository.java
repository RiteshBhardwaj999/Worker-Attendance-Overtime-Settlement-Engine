package com.taskmanager.repository;

import com.taskmanager.entity.Project;
import com.taskmanager.entity.ProjectMember;
import com.taskmanager.entity.User;
import com.taskmanager.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {
    Optional<ProjectMember> findByUserAndProject(User user, Project project);
    List<ProjectMember> findAllByProject(Project project);
    boolean existsByUserAndProject(User user, Project project);
    void deleteByUserAndProject(User user, Project project);

    Optional<ProjectMember> findByUser_IdAndProject_Id(UUID userId, UUID projectId);
    boolean existsByUser_IdAndProject_Id(UUID userId, UUID projectId);

    List<ProjectMember> findAllByProject_Id(UUID projectId);

    Optional<ProjectMember> findByUser_IdAndProject_IdAndRole(UUID userId, UUID projectId, Role role);

    void deleteAllByProject_Id(UUID projectId);
}
