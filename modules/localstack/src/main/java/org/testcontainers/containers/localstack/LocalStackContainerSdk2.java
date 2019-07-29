package org.testcontainers.containers.localstack;

import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.regions.Region;

public class LocalStackContainerSdk2 extends BaseLocalStackContainer<AwsCredentialsProvider, Region, LocalStackContainerSdk2> {

    private AwsCredentialsProvider credentialsProvider = AnonymousCredentialsProvider.create();
    private Region region = Region.US_WEST_1;

    public LocalStackContainerSdk2() {
        super();
    }

    public LocalStackContainerSdk2(String version) {
        super(version);
    }

    /**
     * Provides a {@link AwsCredentialsProvider} that is preconfigured to communicate with a given simulated service.
     * The credentials provider should be set in the AWS Java SDK when building a client.
     * @return an {@link AwsCredentialsProvider}
     */
    @Override
    public AwsCredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }

    /**
     * Provides a {@link Region} that is preconfigured to communicate with a given simulated service.
     * The region should be set in the AWS Java SDK when building a client.
     * @return an {@link Region}
     */
    @Override
    public Region getRegion() {
        return region;
    }

    /**
     * Declare the credentials provider to use when creating clients. Defaults to {@link AnonymousCredentialsProvider}
     * @param credentialsProvider to use
     * @return this container object
     */
    @Override
    public LocalStackContainerSdk2 withCredentialsProvider(AwsCredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
        return self();
    }

    /**
     * Declare the region that should be used to create services and clients. Defaults to us-west-1.
     * @param region to use
     * @return this container object
     */
    @Override
    public LocalStackContainerSdk2 withRegion(Region region) {
        this.region = region;
        return self();
    }

    /**
     * Request a client that is configured to talk to the requested service.
     * @param service client is needed for
     * @param clientClass of the client desired. Must derive from {@link SdkClient}
     * @return a configured {@link SdkClient} class
     * @throws ReflectiveOperationException if the requested class is invalid or if there is a problem building the client
     */
    @Override
    public <T> T getServiceClient(Service service, Class<T> clientClass) throws ReflectiveOperationException {
        AwsClientBuilder clientBuilder = getBuilderForService(service, clientClass);

        clientBuilder.credentialsProvider(getCredentialsProvider())
                     .region(getRegion())
                     .endpointOverride(getEndpointUri(service));

        Object client = clientBuilder.build();

        if (!(clientClass.isInstance(client))) {
            throw new IllegalStateException("Somehow the class built is different than the one requested.");
        }

        return clientClass.cast(client);
    }
}
