package com.taskmanager.service;

import com.taskmanager.dto.request.CreateSiteRequest;
import com.taskmanager.dto.response.SiteResponse;
import com.taskmanager.entity.Site;
import com.taskmanager.exception.ApiException;
import com.taskmanager.exception.ErrorCode;
import com.taskmanager.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SiteService {

    private final SiteRepository siteRepository;

    @Transactional
    public SiteResponse create(CreateSiteRequest req) {
        Site site = Site.builder()
                .siteName(req.getSiteName())
                .location(req.getLocation())
                .active(true)
                .build();
        return SiteResponse.from(siteRepository.save(site));
    }

    public List<SiteResponse> list() {
        return siteRepository.findAll().stream().map(SiteResponse::from).toList();
    }

    public SiteResponse get(UUID id) {
        return SiteResponse.from(getSiteOrThrow(id));
    }

    /** Shared lookup used by the attendance service. */
    public Site getSiteOrThrow(UUID id) {
        return siteRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SITE_NOT_FOUND, "Site not found: " + id));
    }
}
