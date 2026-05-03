package sb.concepts.lab.cloud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!prod & !localstack")
public class DefaultCloudEnvironmentClient implements CloudEnvironmentClient {

    private static final Logger log = LoggerFactory.getLogger(DefaultCloudEnvironmentClient.class);

    @Override
    public String describeEnvironment() {
        log.debug("Describing default local cloud environment");
        return "Cloud environment: disabled. The active profile uses local application behavior only.";
    }

    @Override
    public String echo(String message) {
        log.debug("Handling default local cloud echo payloadLength={}", message.length());
        return "Local profile received: " + message;
    }
}
