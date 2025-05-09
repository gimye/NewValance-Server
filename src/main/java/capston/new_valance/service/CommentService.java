// src/main/java/capston/new_valance/service/CommentService.java
package capston.new_valance.service;

import capston.new_valance.dto.CommentDto;
import capston.new_valance.dto.req.CreateCommentRequest;
import capston.new_valance.model.Comment;
import capston.new_valance.model.NewsArticle;
import capston.new_valance.model.User;
import capston.new_valance.repository.CommentRepository;
import capston.new_valance.repository.NewsArticleRepository;
import capston.new_valance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final NewsArticleRepository articleRepository;
    private final UserRepository userRepository;

    /** 댓글 목록 조회 */
    public Page<CommentDto> listComments(Long articleId, Pageable pageable, Long currentUserId) {
        articleRepository.findById(articleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "해당 뉴스 영상을 찾을 수 없습니다."
                ));

        Page<Comment> comments = commentRepository.findByArticle_ArticleId(articleId, pageable);

        return comments.map(c -> {
            User u = userRepository.findById(c.getUserId()).orElseThrow();
            return CommentDto.builder()
                    .commentId(c.getCommentId())
                    .username(u.getUsername())
                    .profileImgUrl(u.getProfilePictureUrl())
                    .content(c.getContent())
                    .isMine(currentUserId != null && currentUserId.equals(c.getUserId()))
                    .build();
        });
    }

    /** 댓글 작성 */
    public CommentDto createComment(Long articleId, Long userId, CreateCommentRequest req) {
        NewsArticle article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "해당 뉴스 영상을 찾을 수 없습니다."
                ));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "유효하지 않은 사용자입니다."
                ));

        Comment saved = commentRepository.save(
                Comment.builder()
                        .article(article)
                        .userId(userId)
                        .content(req.getContent())
                        .build()
        );

        return CommentDto.builder()
                .commentId(saved.getCommentId())
                .username(user.getUsername())
                .profileImgUrl(user.getProfilePictureUrl())
                .content(saved.getContent())
                .isMine(true)
                .build();
    }

    /** 댓글 삭제 */
    public void deleteComment(Long articleId, Long commentId, Long userId) {
        Comment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "해당 댓글이 존재하지 않습니다."
                ));
        if (!c.getArticle().getArticleId().equals(articleId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "articleId와 commentId가 일치하지 않습니다."
            );
        }
        if (!c.getUserId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "본인이 작성한 댓글만 삭제할 수 있습니다."
            );
        }
        commentRepository.delete(c);
    }
}
