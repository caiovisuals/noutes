package com.noutes.repository;

import com.noutes.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {
    Optional<Workspace> findByOwnerId(UUID ownerId);
}