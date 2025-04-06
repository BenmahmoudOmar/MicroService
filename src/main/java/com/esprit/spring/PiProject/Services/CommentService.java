package com.esprit.spring.PiProject.Services;

import com.esprit.spring.PiProject.Repository.CommentRepository;
import com.esprit.spring.PiProject.Repository.OfferRepository;
import com.esprit.spring.PiProject.Repository.UserRepository;
import com.esprit.spring.PiProject.entities.Comment;
import com.esprit.spring.PiProject.entities.Offer;
import com.esprit.spring.PiProject.entities.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class CommentService implements ICommentService{
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    OfferRepository offerRepository;
    UserRepository userRepository;

    public static List<String> BAD_WORDS = Arrays.asList(
            "fuck", "shit", "idiot","bad word" // bad words
    );

    public static boolean containsBadWords(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (String word : BAD_WORDS) {
            if (text.toLowerCase().contains(word.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    @Override
    public Comment AddCommment(Comment comment) {
        if (containsBadWords(comment.getContent())) {
            throw new IllegalArgumentException("Your comment contains inappropriate language.");
        }
        return commentRepository.save(comment);
    }

    @Override
    public Comment EditComment(Long commentId, String newContent) {
        return commentRepository.findById(commentId).map(existingComment -> {
            if (containsBadWords(newContent)) {
                throw new IllegalArgumentException("Your comment contains inappropriate language.");
            }
            existingComment.setContent(newContent);
            existingComment.setCreationDate(new Date());
            return commentRepository.save(existingComment);
        }).orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));
    }



    @Override
    public void DeleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    @Override
    public List<Comment> GetAllComments() {
        return commentRepository.findAll();
    }

    @Override
    public Comment GetComment(Long id) {
        return commentRepository.findById(id).get();
    }

    @Override
    public Comment addCommentToOffer(Long offerId, String content) {
        if (containsBadWords(content)) {
            throw new IllegalArgumentException("Your comment contains inappropriate language.");
        } else {
            Offer offer = offerRepository.findById(offerId)
                    .orElseThrow(() -> new RuntimeException("Offer not found"));


            Comment comment = new Comment();
            comment.setContent(content);
            comment.setCreationDate(new Date());
            comment.setOffer(offer);

            return commentRepository.save(comment);
        }

    }




}
