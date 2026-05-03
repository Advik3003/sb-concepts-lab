package sb.concepts.lab.cloud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("localstack")
public class LocalStackCloudEnvironmentClient implements CloudEnvironmentClient {

    private static final Logger log = LoggerFactory.getLogger(LocalStackCloudEnvironmentClient.class);
    private final AwsEnvironmentProperties properties;

    public LocalStackCloudEnvironmentClient(AwsEnvironmentProperties properties) {
        this.properties = properties;
    }

    @Override
    public String describeEnvironment() {
        log.info(
                "Describing LocalStack cloud environment region={} s3Endpoint={} bucketPrefix={}",
                properties.getRegion(),
                properties.getS3().getEndpoint(),
                properties.getS3().getBucketPrefix()
        );
        return "Cloud environment: localstack"
                + ", region: " + properties.getRegion()
                + ", s3 endpoint: " + properties.getS3().getEndpoint()
                + ", bucket prefix: " + properties.getS3().getBucketPrefix();
    }

    @Override
    public String echo(String message) {
        log.info("Handling LocalStack cloud echo payloadLength={}", message.length());
        return "LocalStack S3 simulation accepted message: " + message;
    }
}
