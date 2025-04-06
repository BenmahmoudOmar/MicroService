package com.esprit.spring.PiProject.Services;

import com.esprit.spring.PiProject.entities.Application;
import com.esprit.spring.PiProject.entities.Comment;

import java.util.List;

public interface ICommentService {
    public Comment AddCommment(Comment comment);
    public Comment EditComment(Long commentId, String newContent);
    public void DeleteComment(Long id);
    public List<Comment> GetAllComments();
    public Comment GetComment(Long id);

    Comment addCommentToOffer(Long offerId,String content);
}
