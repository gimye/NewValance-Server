package capston.new_valance.controller;

import capston.new_valance.dto.CommentDto;
import capston.new_valance.dto.req.CreateCommentRequest;
import capston.new_valance.jwt.UserPrincipal;
import capston.new_valance.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 1. 댓글 목록 조회 GET /api/comments/{articleId}

    @GetMapping("/{articleId}")
    public ResponseEntity<Map<String,Object>> list(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable("articleId") Long articleId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Pageable pg = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CommentDto> p = commentService.listComments(
                articleId,
                pg,
                user != null ? user.getUserId() : null
        );

        return ResponseEntity.ok(Map.of(
                "_embedded", Map.of("commentDtoList", p.getContent()),
                "page", Map.of(
                        "size", p.getSize(),
                        "totalElements", p.getTotalElements(),
                        "totalPages", p.getTotalPages(),
                        "number", p.getNumber()
                )
        ));
    }

    // 2. 댓글 작성 POST /api/comments/{articleId}
    @PostMapping("/{articleId}")
    public ResponseEntity<CommentDto> create(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable("articleId") Long articleId,
            @Validated @RequestBody CreateCommentRequest req
    ) {
        CommentDto created = commentService.createComment(articleId, user.getUserId(), req);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }



    // 3. 댓글 삭제 DELETE /api/comments/{articleId}/{commentId}
    @DeleteMapping("/{articleId}/{commentId}")
    public ResponseEntity<Map<String,String>> delete(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable("articleId") Long articleId,
            @PathVariable("commentId") Long commentId
    ) {
        commentService.deleteComment(articleId, commentId, user.getUserId());
        return ResponseEntity.ok(Map.of(
                "message", "댓글이 성공적으로 삭제되었습니다."
        ));
    }
}
