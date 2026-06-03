package com.noutes.dto;

import com.noutes.entity.Page;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PageDto(
    UUID id,
    String title,
    UUID workspaceId,
    UUID parentPageId,
    boolean isPublic,
    UUID createdBy,
    Instant createdAt,
    Instant updatedAt,
    List<PageDto> children
) {
    public static PageDto from(Page p) {
        return new PageDto(
                p.getId(),
                p.getTitle(),
                p.getWorkspace().getId(),
                p.getParentPage() != null ? p.getParentPage().getId() : null,
                p.isPublic(),
                p.getCreatedBy().getId(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                List.of()
        );
    }
}