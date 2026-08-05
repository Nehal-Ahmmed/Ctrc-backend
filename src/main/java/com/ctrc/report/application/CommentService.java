package com.ctrc.report.application;

import com.ctrc.core.domain.exceptions.ResourceNotFoundException;
import com.ctrc.core.domain.exceptions.ValidationException;
import com.ctrc.report.domain.Comment;
import com.ctrc.report.domain.CommentRepository;
import com.ctrc.report.domain.CommentVoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentVoteRepository commentVoteRepository;

    public CommentService(CommentRepository commentRepository,
                          CommentVoteRepository commentVoteRepository) {
        this.commentRepository = commentRepository;
        this.commentVoteRepository = commentVoteRepository;
    }

    public List<Comment> getReportComments(Long reportId, Long viewerUserId) {
        return commentRepository.findByReportId(reportId, viewerUserId);
    }

    public List<Comment> getSubReportComments(Long subReportId, Long viewerUserId) {
        return commentRepository.findBySubReportId(subReportId, viewerUserId);
    }

    @Transactional
    public String voteComment(Long commentId, Long userId, String type) {
        if (!"up".equals(type) && !"down".equals(type)) {
            throw new ValidationException("vote type must be 'up' or 'down'");
        }

        commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "comment not found with id " + commentId));

        Optional<String> existing = commentVoteRepository.findVoteType(userId, commentId);

        if (existing.isPresent()) {
            commentVoteRepository.delete(userId, commentId);
            if (existing.get().equals(type)) {
                return null;
            }
        }

        commentVoteRepository.insert(userId, commentId, type);
        return type;
    }
}
