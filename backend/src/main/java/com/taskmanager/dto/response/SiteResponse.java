package com.taskmanager.dto.response;

import com.taskmanager.entity.Site;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SiteResponse {
    private UUID id;
    private String siteName;
    private String location;
    private boolean active;
    private LocalDateTime createdAt;

    public static SiteResponse from(Site s) {
        SiteResponse r = new SiteResponse();
        r.id = s.getId();
        r.siteName = s.getSiteName();
        r.location = s.getLocation();
        r.active = s.isActive();
        r.createdAt = s.getCreatedAt();
        return r;
    }
}
