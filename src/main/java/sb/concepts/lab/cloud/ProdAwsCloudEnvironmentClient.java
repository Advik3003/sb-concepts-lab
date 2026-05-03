package sb.concepts.lab.cloud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("prod")
public class ProdAwsCloudEnvironmentClient implements CloudEnvironmentClient {

    private static final Logger log = LoggerFactory.getLogger(ProdAwsCloudEnvironmentClient.class);
    private final AwsEnvironmentProperties properties;

    public ProdAwsCloudEnvironmentClient(AwsEnvironmentProperties properties) {
        this.properties = properties;
    }

    @Override
    public String describeEnvironment() {
        log.info(
                "Describing prod cloud environment accountId={} region={} s3Endpoint={} bucketPrefix={}",
                properties.getAccountId(),
                properties.getRegion(),
                properties.getS3().getEndpoint(),
                properties.getS3().getBucketPrefix()
        );
        return "Cloud environment: aws-prod-simulation"
                + ", account: " + properties.getAccountId()
                + ", region: " + properties.getRegion()
                + ", s3 endpoint: " + properties.getS3().getEndpoint()
                + ", bucket prefix: " + properties.getS3().getBucketPrefix();
    }

    @Override
    public String echo(String message) {
        log.info("Handling prod cloud echo payloadLength={}", message.length());
        return "AWS production simulation accepted message for S3 workflow: " + message;
    }
}
