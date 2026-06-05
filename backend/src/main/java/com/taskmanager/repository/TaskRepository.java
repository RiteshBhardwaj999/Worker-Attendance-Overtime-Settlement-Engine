package com.taskmanager.repository;

import com.taskmanager.entity.Task;
import com.taskmanager.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findAllByProject_Id(UUID projectId);

    long countByProject_IdInAndStatus(List<UUID> projectIds, TaskStatus status);

    long countByProject_IdIn(List<UUID> projectIds);

    long countByAssignee_IdAndProject_IdIn(UUID assigneeId, List<UUID> projectIds);

    @Query("SELECT t FROM Task t WHERE t.project.id IN :projectIds AND t.dueDate < :now AND t.status != 'DONE'")
    List<Task> findOverdueTasks(@Param("projectIds") List<UUID> projectIds, @Param("now") LocalDateTime now);

    void deleteAllByProject_Id(UUID projectId);
}
