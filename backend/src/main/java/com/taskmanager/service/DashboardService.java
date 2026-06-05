package com.taskmanager.service;

import com.taskmanager.dto.response.DashboardResponse;
import com.taskmanager.entity.Task;
import com.taskmanager.enums.TaskStatus;
import com.taskmanager.repository.ProjectRepository;
import com.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public DashboardResponse getDashboard(UUID userId) {
        List<UUID> projectIds = projectRepository.findAllByMemberUserId(userId)
                .stream().map(p -> p.getId()).collect(Collectors.toList());

        DashboardResponse resp = new DashboardResponse();
        resp.setTotalProjects(projectIds.size());

        if (projectIds.isEmpty()) {
            resp.setTotalTasks(0);
            resp.setStatusBreakdown(Map.of("TODO", 0L, "IN_PROGRESS", 0L, "DONE", 0L));
            resp.setOverdueTasks(List.of());
            resp.setMyAssignedTasks(0);
            resp.setCompletionRate(0);
            return resp;
        }

        long total = taskRepository.countByProject_IdIn(projectIds);
        long done = taskRepository.countByProject_IdInAndStatus(projectIds, TaskStatus.DONE);
        long inProgress = taskRepository.countByProject_IdInAndStatus(projectIds, TaskStatus.IN_PROGRESS);
        long todo = taskRepository.countByProject_IdInAndStatus(projectIds, TaskStatus.TODO);
        long myAssigned = taskRepository.countByAssignee_IdAndProject_IdIn(userId, projectIds);

        Map<String, Long> breakdown = new HashMap<>();
        breakdown.put("TODO", todo);
        breakdown.put("IN_PROGRESS", inProgress);
        breakdown.put("DONE", done);

        List<Task> overdue = taskRepository.findOverdueTasks(projectIds, LocalDateTime.now());
        List<DashboardResponse.OverdueTaskItem> overdueItems = overdue.stream().map(t -> {
            DashboardResponse.OverdueTaskItem item = new DashboardResponse.OverdueTaskItem();
            item.setId(t.getId().toString());
            item.setTitle(t.getTitle());
            item.setDueDate(t.getDueDate().toString());
            item.setProjectName(t.getProject().getName());
            item.setProjectId(t.getProject().getId().toString());
            return item;
        }).collect(Collectors.toList());

        resp.setTotalTasks(total);
        resp.setStatusBreakdown(breakdown);
        resp.setOverdueTasks(overdueItems);
        resp.setMyAssignedTasks(myAssigned);
        resp.setCompletionRate(total == 0 ? 0 : (int) ((done * 100) / total));
        return resp;
    }
}
