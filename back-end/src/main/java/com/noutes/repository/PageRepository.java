package com.noutes.repository;

import com.noutes.entity.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PageRepository extends JpaRepository<Page, UUID> {
    List<Page> findByWorkspaceIdOrderByCreatedAtAsc(UUID workspaceId);
    List<Page> findByParentPageIdOrderByCreatedAtAsc(UUID parentPageId);
}