package com.esprit.spring.PiProject.Controllers;

import com.esprit.spring.PiProject.Services.ICommentService;
import com.esprit.spring.PiProject.entities.Application;
import com.esprit.spring.PiProject.entities.Comment;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/Comment")
@CrossOrigin(origins = "http://localhost:4200")
public class CommentController {
    @Autowired
    ICommentService commentService;

    @GetMapping("/retrieve-all-comments")
    public List<Comment> getComments() {
        List<Comment> listComments = commentService.GetAllComments();
        return listComments;
    }

    @PostMapping("/Add-Comment")
    public Comment addComment(@RequestBody Comment comment) {
        return commentService.AddCommment(comment);
    }

    @DeleteMapping("/Delete-Comment/{id}")
    public void deleteComment(@PathVariable Long id) {
        commentService.DeleteComment(id);
    }

    @PutMapping("/update/{commentId}")
    public Comment updateComment(@PathVariable Long commentId, @RequestBody String newContent) {
        return commentService.EditComment(commentId, newContent);
    }

    @PutMapping("/Add-Comment-Offer/{offerId}")
     public Comment addCommentOffer(@PathVariable Long offerId, @RequestParam String content) {
        return commentService.addCommentToOffer(offerId,content);
    }
}
