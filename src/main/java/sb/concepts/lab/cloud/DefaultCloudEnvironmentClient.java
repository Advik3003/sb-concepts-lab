package sb.concepts.lab.cloud;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!prod & !localstack")
public class DefaultCloudEnvironmentClient implements CloudEnvironmentClient {

    @Override
    public String describeEnvironment() {
        return "Cloud environment: disabled. The active profile uses local application behavior only.";
    }

    @Override
    public String echo(String message) {
        return "Local profile received: " + message;
    }
}
