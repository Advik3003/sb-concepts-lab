package sb.concepts.lab;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("app/v1/test")
public class TestController {

    @GetMapping("/message")
    public String test() {
        return "test";
    }

    @PostMapping
    public String testPost(String string) {
        return "Hello Post string: " + string;
    }
}
