package com.taskmanager.dto.response;

import com.taskmanager.entity.ProjectMember;
import com.taskmanager.enums.Role;
import lombok.Data;

import java.util.UUID;

@Data
public class MemberResponse {
    private UUID userId;
    private String name;
    private String email;
    private Role role;

    public static MemberResponse from(ProjectMember pm) {
        MemberResponse r = new MemberResponse();
        r.userId = pm.getUser().getId();
        r.name = pm.getUser().getName();
        r.email = pm.getUser().getEmail();
        r.role = pm.getRole();
        return r;
    }
}
