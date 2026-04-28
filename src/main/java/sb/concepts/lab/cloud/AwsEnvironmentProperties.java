package sb.concepts.lab.cloud;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.aws")
public class AwsEnvironmentProperties {

    private String region = "not-configured";
    private String accountId = "not-configured";
    private String accessKey = "not-configured";
    private String secretKey = "not-configured";
    private final S3 s3 = new S3();

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public S3 getS3() {
        return s3;
    }

    public static class S3 {

        private String endpoint = "aws-managed";
        private String bucketPrefix = "not-configured";

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getBucketPrefix() {
            return bucketPrefix;
        }

        public void setBucketPrefix(String bucketPrefix) {
            this.bucketPrefix = bucketPrefix;
        }
    }
}
