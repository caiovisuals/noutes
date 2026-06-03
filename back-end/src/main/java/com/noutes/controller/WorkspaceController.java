package com.noutes.controller;

import com.noutes.dto.WorkspaceDto;
import com.noutes.entity.User;
import com.noutes.service.WorkspaceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @GetMapping
    public List<WorkspaceDto> listWorkspaces(@AuthenticationPrincipal User user) {
        return workspaceService.listByOwner(user);
    }

    @GetMapping("/me")
    public WorkspaceDto getMyWorkspace(@AuthenticationPrincipal User user) {
        return workspaceService.getOrCreate(user);
    }

    @GetMapping("/{workspaceId}")
    public WorkspaceDto getWorkspace(@PathVariable UUID workspaceId,
                                     @AuthenticationPrincipal User user) {
        return workspaceService.getById(workspaceId, user);
    }

    @PostMapping
    public ResponseEntity<WorkspaceDto> create(@Valid @RequestBody WorkspaceRequest req,
                                               @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workspaceService.create(req.name(), user));
    }

    @PatchMapping("/{workspaceId}")
    public WorkspaceDto rename(@PathVariable UUID workspaceId,
                               @Valid @RequestBody WorkspaceRequest req,
                               @AuthenticationPrincipal User user) {
        return workspaceService.rename(workspaceId, req.name(), user);
    }

    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<Void> delete(@PathVariable UUID workspaceId,
                                       @AuthenticationPrincipal User user) {
        workspaceService.delete(workspaceId, user);
        return ResponseEntity.noContent().build();
    }

    public record WorkspaceRequest(@NotBlank String name) {}
}