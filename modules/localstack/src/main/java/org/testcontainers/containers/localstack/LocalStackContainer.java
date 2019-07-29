package org.testcontainers.containers.localstack;

import java.net.URI;

/**
 * <p>Container for Atlassian Labs Localstack, 'A fully functional local AWS cloud stack'.</p>
 * <p>{@link BaseLocalStackContainer#withServices(Service...)} should be used to select which services
 * are to be launched. See {@link Service} for available choices. It is advised that
 * {@link LocalStackContainer#getServiceClient(Service, Class)} be used to obtain compatible client.</p>
 *
 * The LocalStack docker container does not require credentials to a region to be set, but
 * {@link LocalStackContainer#withCredentialsProvider(Object)} and {@link LocalStackContainer#withRegion(Object)}
 * to control how clients are configured.
 *
 * @param <CredProviderT> credential provider base class type. Differs for AWS SDK v1 and v2
 * @param <RegionT> region class type. Differs for AWS SDK v1 and v2
 */
public interface LocalStackContainer<CredProviderT, RegionT> {


    CredProviderT getCredentialsProvider();
    RegionT getRegion();
    LocalStackContainer withServices(Service... services);
    LocalStackContainer withCredentialsProvider(CredProviderT credentialsProvider);
    LocalStackContainer withRegion(RegionT region);
    URI getEndpointUri(Service service);
    <T> T getServiceClient(Service service, Class<T> clazz) throws ReflectiveOperationException;
}
