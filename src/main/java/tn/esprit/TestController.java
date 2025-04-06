package tn.esprit;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    // This endpoint will return a simple test message
    @GetMapping("/test")
    public String testMessage() {
        return "This is a test message from the TestController!";
    }
}
