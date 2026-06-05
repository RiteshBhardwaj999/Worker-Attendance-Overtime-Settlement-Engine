package com.taskmanager.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSiteRequest {

    @NotBlank(message = "Site name is required")
    private String siteName;

    private String location;
}
