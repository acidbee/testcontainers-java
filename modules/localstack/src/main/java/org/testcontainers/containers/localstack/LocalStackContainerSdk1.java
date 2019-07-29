package org.testcontainers.containers.localstack;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;

public class LocalStackContainerSdk1 extends BaseLocalStackContainer<AWSCredentialsProvider, Regions, LocalStackContainerSdk1> {

    private AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials("accesskey", "secretkey"));
    private Regions region = Regions.US_WEST_1;

    public LocalStackContainerSdk1() {
        super();
    }

    public LocalStackContainerSdk1(String version) {
        super(version);
    }

    /**
     * Provides a {@link AWSCredentialsProvider} that is preconfigured to communicate with a given simulated service.
     * The credentials provider should be set in the AWS Java SDK when building a client.
     * @return an {@link AWSCredentialsProvider}
     */
    @Override
    public AWSCredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }

    /**
     * Provides a {@link Regions} that is preconfigured to communicate with a given simulated service.
     * The region should be set in the AWS Java SDK when building a client.
     * @return an {@link Regions}
     */
    @Override
    public Regions getRegion() {
        return region;
    }

    /**
     * Declare the credentials provider to use when creating clients. Defaults to {@link AWSStaticCredentialsProvider}
     * of {@link BasicAWSCredentials} with access key value 'accesskey' and secret key value 'secretkey'.
     * @param credentialsProvider to use
     * @return this container object
     */
    @Override
    public LocalStackContainer withCredentialsProvider(AWSCredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
        return self();
    }

    /**
     * Declare the region that should be used to create services and clients. Defaults to us-west-1.
     * @param region to use
     * @return this container object
     */
    @Override
    public LocalStackContainer withRegion(Regions region) {
        this.region = region;
        return self();
    }

    /**
     * Provides an endpoint configuration that is preconfigured to communicate with a given simulated service.
     * The provided endpoint configuration should be set in the AWS Java SDK (v1) when building a client, e.g.:
     * <pre><code>AmazonS3 s3 = AmazonS3ClientBuilder
            .standard()
            .withEndpointConfiguration(localstack.getEndpointConfiguration(S3))
            .withCredentials(localstack.getCredentialsProvider())
            .build();
     </code></pre>
     * @param service the service that is to be accessed
     * @return an {@link AwsClientBuilder.EndpointConfiguration}
     */
    public AwsClientBuilder.EndpointConfiguration getEndpointConfiguration(Service service) {
        return new AwsClientBuilder.EndpointConfiguration(getEndpointUri(service).toString(), getRegion().getName());
    }

    /**
     * Request a client that is configured to talk to the requested service.
     * @param service client is needed for
     * @param clientClass of an SdkClient
     * @return @return a configured {@link AmazonWebServiceClient} class
     * @throws ReflectiveOperationException if the requested class is invalid or if there is a problem building the client
     */
    public <T> T getServiceClient(Service service, Class<T> clientClass) throws ReflectiveOperationException {
        AwsClientBuilder clientBuilder = getBuilderForService(service, clientClass);

        clientBuilder.withEndpointConfiguration(
            getEndpointConfiguration(service))
            .withCredentials(getCredentialsProvider());

        Object client = clientBuilder.build();

        if (!(clientClass.isInstance(client))) {
            throw new IllegalStateException("Somehow the class built is different than the one requested.");
        }

        return clientClass.cast(client);
    }
}
