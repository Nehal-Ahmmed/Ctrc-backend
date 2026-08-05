package com.ctrc.report.presentation;

import com.ctrc.core.presentation.ApiResponse;
import com.ctrc.report.application.CommentService;
import com.ctrc.report.application.dto.VoteRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/{id}/vote")
    public ApiResponse<Map<String, String>> voteComment(
            @PathVariable("id") Long commentId,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody VoteRequest request) {
        String vote = commentService.voteComment(commentId, userId, request.getType());
        return ApiResponse.success(Collections.singletonMap("userVoteType", vote));
    }
}
