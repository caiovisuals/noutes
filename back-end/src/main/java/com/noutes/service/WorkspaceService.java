package com.noutes.service;

import com.noutes.dto.WorkspaceDto;
import com.noutes.entity.*;
import com.noutes.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;

    @Transactional
    public WorkspaceDto getOrCreate(User user) {
        return workspaceRepository.findByOwnerId(user.getId())
                .map(WorkspaceDto::from)
                .orElseGet(() -> create(user.getName() + "'s workspace", user));
    }

    @Transactional
    public WorkspaceDto create(String name, User user) {
        var ws = Workspace.builder()
                .name(name)
                .owner(user)
                .build();
        return WorkspaceDto.from(workspaceRepository.save(ws));
    }
}