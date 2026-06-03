package com.noutes.controller;

import com.noutes.dto.WorkspaceDto;
import com.noutes.entity.User;
import com.noutes.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @GetMapping("/me")
    public WorkspaceDto getMyWorkspace(@AuthenticationPrincipal User user) {
        return workspaceService.getOrCreate(user);
    }

    @PostMapping
    public ResponseEntity<WorkspaceDto> create(@RequestBody CreateWorkspaceRequest req,
                                               @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workspaceService.create(req.name(), user));
    }

    public record CreateWorkspaceRequest(String name) {}
}