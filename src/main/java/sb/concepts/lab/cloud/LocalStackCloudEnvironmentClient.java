package sb.concepts.lab.cloud;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("localstack")
public class LocalStackCloudEnvironmentClient implements CloudEnvironmentClient {

    private final AwsEnvironmentProperties properties;

    public LocalStackCloudEnvironmentClient(AwsEnvironmentProperties properties) {
        this.properties = properties;
    }

    @Override
    public String describeEnvironment() {
        return "Cloud environment: localstack"
                + ", region: " + properties.getRegion()
                + ", s3 endpoint: " + properties.getS3().getEndpoint()
                + ", bucket prefix: " + properties.getS3().getBucketPrefix();
    }

    @Override
    public String echo(String message) {
        return "LocalStack S3 simulation accepted message: " + message;
    }
}
