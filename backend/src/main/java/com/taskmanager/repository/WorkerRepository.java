package com.taskmanager.repository;

import com.taskmanager.entity.Worker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WorkerRepository extends JpaRepository<Worker, UUID> {

    Optional<Worker> findByPhone(String phone);

    boolean existsByPhone(String phone);
}
