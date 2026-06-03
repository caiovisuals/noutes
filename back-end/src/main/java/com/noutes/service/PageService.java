package com.noutes.service;

import com.noutes.controller.PageController.CreatePageRequest;
import com.noutes.controller.PageController.MovePageRequest;
import com.noutes.controller.PageController.UpdatePageRequest;
import com.noutes.dto.PageDto;
import com.noutes.entity.*;
import com.noutes.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PageService {

    private final PageRepository pageRepository;
    private final WorkspaceRepository workspaceRepository;

    public List<PageDto> listByWorkspace(UUID workspaceId, User user) {
        Workspace ws = getWorkspaceOrThrow(workspaceId);
        if (!ws.getOwner().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return pageRepository.findByWorkspaceIdOrderByCreatedAtAsc(workspaceId)
                .stream().map(PageDto::from).toList();
    }

    public PageDto get(UUID pageId, User user) {
        Page page = getPageOrThrow(pageId);
        if (!page.getWorkspace().getOwner().getId().equals(user.getId()) && !page.isPublic()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return PageDto.from(page);
    }

    public PageDto getPublic(UUID pageId) {
        Page page = getPageOrThrow(pageId);
        if (!page.isPublic()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return PageDto.from(page);
    }

    @Transactional
    public PageDto create(CreatePageRequest req, User user) {
        Workspace ws = getWorkspaceOrThrow(req.workspaceId());
        if (!ws.getOwner().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Page parent = null;
        if (req.parentPageId() != null) {
            parent = getPageOrThrow(req.parentPageId());
        }

        var page = Page.builder()
                .title(req.title() != null ? req.title() : "")
                .workspace(ws)
                .parentPage(parent)
                .isPublic(false)
                .createdBy(user)
                .build();

        return PageDto.from(pageRepository.save(page));
    }

    @Transactional
    public PageDto update(UUID pageId, UpdatePageRequest req, User user) {
        Page page = getPageOrThrow(pageId);
        if (!page.getWorkspace().getOwner().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (req.title() != null) page.setTitle(req.title());
        if (req.isPublic() != null) page.setPublic(req.isPublic());
        return PageDto.from(pageRepository.save(page));
    }

    @Transactional
    public void delete(UUID pageId, User user) {
        Page page = getPageOrThrow(pageId);
        if (!page.getWorkspace().getOwner().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        pageRepository.delete(page);
    }

    @Transactional
    public PageDto move(UUID pageId, MovePageRequest req, User user) {
        Page page = getPageOrThrow(pageId);
        if (!page.getWorkspace().getOwner().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (req.workspaceId() != null && !req.workspaceId().equals(page.getWorkspace().getId())) {
            Workspace target = getWorkspaceOrThrow(req.workspaceId());
            if (!target.getOwner().getId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
            page.setWorkspace(target);
            page.setParentPage(null);
        }

        if (req.parentPageId() != null) {
            if (req.parentPageId().equals(pageId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page cannot be its own parent");
            }
            Page newParent = getPageOrThrow(req.parentPageId());
            if (!newParent.getWorkspace().getId().equals(page.getWorkspace().getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent page must be in the same workspace");
            }
            page.setParentPage(newParent);
        } else if (req.workspaceId() == null) {
            page.setParentPage(null);
        }

        return PageDto.from(pageRepository.save(page));
    }

    public void checkAccess(UUID pageId, User user) {
        Page page = getPageOrThrow(pageId);
        if (!page.getWorkspace().getOwner().getId().equals(user.getId()) && !page.isPublic()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private Page getPageOrThrow(UUID id) {
        return pageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private Workspace getWorkspaceOrThrow(UUID id) {
        return workspaceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}