package com.taskmanager.dto.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DashboardResponse {
    private long totalProjects;
    private long totalTasks;
    private Map<String, Long> statusBreakdown;
    private List<OverdueTaskItem> overdueTasks;
    private long myAssignedTasks;
    private int completionRate;

    @Data
    public static class OverdueTaskItem {
        private String id;
        private String title;
        private String dueDate;
        private String projectName;
        private String projectId;
    }
}
