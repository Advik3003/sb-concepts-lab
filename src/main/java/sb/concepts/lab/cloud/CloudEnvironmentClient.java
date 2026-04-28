package sb.concepts.lab.cloud;

public interface CloudEnvironmentClient {

    String describeEnvironment();

    String echo(String message);
}
