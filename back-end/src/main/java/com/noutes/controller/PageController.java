package com.noutes.controller;

import com.noutes.dto.PageDto;
import com.noutes.entity.User;
import com.noutes.service.PageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PageController {

    private final PageService pageService;

    @GetMapping("/workspaces/{workspaceId}/pages")
    public List<PageDto> listPages(@PathVariable UUID workspaceId,
                                   @AuthenticationPrincipal User user) {
        return pageService.listByWorkspace(workspaceId, user);
    }

    @GetMapping("/pages/{pageId}")
    public PageDto getPage(@PathVariable UUID pageId,
                           @AuthenticationPrincipal User user) {
        return pageService.get(pageId, user);
    }

    @GetMapping("/pages/{pageId}/public")
    public PageDto getPublicPage(@PathVariable UUID pageId) {
        return pageService.getPublic(pageId);
    }

    @PostMapping("/pages")
    public ResponseEntity<PageDto> createPage(@Valid @RequestBody CreatePageRequest req,
                                              @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pageService.create(req, user));
    }

    @PatchMapping("/pages/{pageId}")
    public PageDto updatePage(@PathVariable UUID pageId,
                              @RequestBody UpdatePageRequest req,
                              @AuthenticationPrincipal User user) {
        return pageService.update(pageId, req, user);
    }

    @DeleteMapping("/pages/{pageId}")
    public ResponseEntity<Void> deletePage(@PathVariable UUID pageId,
                                           @AuthenticationPrincipal User user) {
        pageService.delete(pageId, user);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/pages/{pageId}/move")
    public PageDto movePage(@PathVariable UUID pageId,
                            @RequestBody MovePageRequest req,
                            @AuthenticationPrincipal User user) {
        return pageService.move(pageId, req, user);
    }

    @GetMapping("/pages/{pageId}/access")
    public ResponseEntity<Void> checkAccess(@PathVariable UUID pageId,
                                            @AuthenticationPrincipal User user) {
        pageService.checkAccess(pageId, user);
        return ResponseEntity.ok().build();
    }

    public record CreatePageRequest(
            @jakarta.validation.constraints.NotNull UUID workspaceId,
            String title,
            UUID parentPageId
    ) {}

    public record UpdatePageRequest(String title, Boolean isPublic) {}

    public record MovePageRequest(UUID parentPageId, UUID workspaceId) {}
}