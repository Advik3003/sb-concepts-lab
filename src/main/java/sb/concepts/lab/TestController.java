package sb.concepts.lab;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sb.concepts.lab.cloud.CloudEnvironmentClient;

@RestController
@RequestMapping("app/v1/test")
public class TestController {

    private static final Logger log = LoggerFactory.getLogger(TestController.class);
    private final CloudEnvironmentClient cloudEnvironmentClient;

    public TestController(CloudEnvironmentClient cloudEnvironmentClient) {
        this.cloudEnvironmentClient = cloudEnvironmentClient;
    }

    @GetMapping("/message")
    public String test() {
        log.debug("Handling test message request");
        return "test";
    }

    @PostMapping
    public String testPost(@RequestParam String string) {
        log.info("Handling test post request payloadLength={}", string.length());
        return "Hello Post string: " + string;
    }

    @GetMapping("/cloud")
    public String cloudEnvironment() {
        log.info("Handling cloud environment description request");
        return cloudEnvironmentClient.describeEnvironment();
    }

    @PostMapping("/cloud")
    public String cloudEnvironmentPost(@RequestParam String string) {
        log.info("Handling cloud environment post request payloadLength={}", string.length());
        return cloudEnvironmentClient.echo(string);
    }
}
