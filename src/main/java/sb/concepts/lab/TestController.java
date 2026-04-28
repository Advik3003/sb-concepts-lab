package sb.concepts.lab;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sb.concepts.lab.cloud.CloudEnvironmentClient;

@RestController
@RequestMapping("app/v1/test")
public class TestController {

    private final CloudEnvironmentClient cloudEnvironmentClient;

    public TestController(CloudEnvironmentClient cloudEnvironmentClient) {
        this.cloudEnvironmentClient = cloudEnvironmentClient;
    }

    @GetMapping("/message")
    public String test() {
        return "test";
    }

    @PostMapping
    public String testPost(@RequestParam String string) {
        return "Hello Post string: " + string;
    }

    @GetMapping("/cloud")
    public String cloudEnvironment() {
        return cloudEnvironmentClient.describeEnvironment();
    }

    @PostMapping("/cloud")
    public String cloudEnvironmentPost(@RequestParam String string) {
        return cloudEnvironmentClient.echo(string);
    }
}
