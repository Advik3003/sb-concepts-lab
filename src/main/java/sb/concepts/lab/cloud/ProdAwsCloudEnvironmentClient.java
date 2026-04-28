package sb.concepts.lab.cloud;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("prod")
public class ProdAwsCloudEnvironmentClient implements CloudEnvironmentClient {

    private final AwsEnvironmentProperties properties;

    public ProdAwsCloudEnvironmentClient(AwsEnvironmentProperties properties) {
        this.properties = properties;
    }

    @Override
    public String describeEnvironment() {
        return "Cloud environment: aws-prod-simulation"
                + ", account: " + properties.getAccountId()
                + ", region: " + properties.getRegion()
                + ", s3 endpoint: " + properties.getS3().getEndpoint()
                + ", bucket prefix: " + properties.getS3().getBucketPrefix();
    }

    @Override
    public String echo(String message) {
        return "AWS production simulation accepted message for S3 workflow: " + message;
    }
}
