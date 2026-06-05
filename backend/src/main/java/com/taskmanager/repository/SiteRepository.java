package com.taskmanager.repository;

import com.taskmanager.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SiteRepository extends JpaRepository<Site, UUID> {
}
