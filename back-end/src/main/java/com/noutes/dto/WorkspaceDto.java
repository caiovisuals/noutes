package com.noutes.dto;

import com.noutes.entity.Workspace;

import java.util.UUID;

public record WorkspaceDto(UUID id, String name, UUID ownerId) {
    public static WorkspaceDto from(Workspace w) {
        return new WorkspaceDto(w.getId(), w.getName(), w.getOwner().getId());
    }
}