package Therapie_Management.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import Therapie_Management.backend.dto.ContentResponse;
import Therapie_Management.backend.dto.PageResponse;
import Therapie_Management.backend.service.ContentService;

/**
 * Lesezugriff auf alle Tipps - nur fuer Therapeuten. Patienten nutzen
 * /api/patients/{id}/contents und sehen dort nur freigeschaltete Module.
 */
@RestController
@RequestMapping("/api/contents")
@PreAuthorize("hasRole('THERAPEUT')")
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<ContentResponse>> getContents(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + PageResponse.DEFAULT_SIZE) int size) {
        return ResponseEntity.ok(contentService.getContents(categoryId, page, size));
    }

    @GetMapping("/{contentId}")
    public ResponseEntity<ContentResponse> getContent(@PathVariable Integer contentId) {
        return ResponseEntity.ok(contentService.getContent(contentId));
    }
}
