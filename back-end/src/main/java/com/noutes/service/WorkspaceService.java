package com.noutes.service;

import com.noutes.dto.WorkspaceDto;
import com.noutes.entity.*;
import com.noutes.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;

    @Transactional
    public WorkspaceDto getOrCreate(User user) {
        return workspaceRepository.findFirstByOwnerIdOrderByCreatedAtAsc(user.getId())
                .map(WorkspaceDto::from)
                .orElseGet(() -> create(user.getName() + "'s workspace", user));
    }

    public List<WorkspaceDto> listByOwner(User user) {
        return workspaceRepository.findAllByOwnerIdOrderByCreatedAtAsc(user.getId())
                .stream().map(WorkspaceDto::from).toList();
    }

    public WorkspaceDto getById(UUID workspaceId, User user) {
        Workspace ws = getAndVerifyOwner(workspaceId, user);
        return WorkspaceDto.from(ws);
    }

    @Transactional
    public WorkspaceDto create(String name, User user) {
        var ws = Workspace.builder()
                .name(name)
                .owner(user)
                .build();
        return WorkspaceDto.from(workspaceRepository.save(ws));
    }

    @Transactional
    public WorkspaceDto rename(UUID workspaceId, String name, User user) {
        Workspace ws = getAndVerifyOwner(workspaceId, user);
        ws.setName(name);
        return WorkspaceDto.from(workspaceRepository.save(ws));
    }

    @Transactional
    public void delete(UUID workspaceId, User user) {
        Workspace ws = getAndVerifyOwner(workspaceId, user);
        workspaceRepository.delete(ws);
    }

    private Workspace getAndVerifyOwner(UUID workspaceId, User user) {
        Workspace ws = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!ws.getOwner().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return ws;
    }
}