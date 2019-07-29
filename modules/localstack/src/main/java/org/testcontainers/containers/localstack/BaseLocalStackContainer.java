package org.testcontainers.containers.localstack;

import org.rnorth.ducttape.Preconditions;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseLocalStackContainer<CredProviderT, RegionT, SELF extends BaseLocalStackContainer<CredProviderT, RegionT, SELF>> extends GenericContainer<SELF> implements LocalStackContainer<CredProviderT, RegionT> {

    public static final String VERSION = "0.9.6";

    protected final List<Service> services = new ArrayList<>();

    public BaseLocalStackContainer() {
        this(VERSION);
    }

    public BaseLocalStackContainer(String version) {
        super("localstack/localstack:" + version);

        withEnv("DEBUG", "1");
        withFileSystemBind("//var/run/docker.sock", "/var/run/docker.sock");
        waitingFor(Wait.forLogMessage(".*Ready\\.\n", 1));
    }

    @Override
    protected void configure() {
        super.configure();

        Preconditions.check("services list must not be empty", !services.isEmpty());

        withEnv("SERVICES", services.stream().map(Service::getLocalStackName).collect(Collectors.joining(",")));

        for (Service service : services) {
            addExposedPort(service.getPort());
        }
    }

    /**
     * Declare a set of simulated AWS services that should be launched by this container.
     * @param services one or more service names
     * @return this container object
     */
    public SELF withServices(Service... services) {
        this.services.addAll(Arrays.asList(services));
        return self();
    }

    public URI getEndpointUri(Service service) {
        final String address = getContainerIpAddress();
        String ipAddress = address;
        try {
            // resolve IP address and use that as the endpoint so that path-style access is automatically used for S3
            ipAddress = InetAddress.getByName(address).getHostAddress();
        } catch (UnknownHostException ignored) {

        }

        return URI.create("http://" + ipAddress + ":" + getMappedPort(service.getPort()));
    }

    protected <T> T getBuilderForService(Service service, Class<?> clientClass) throws ReflectiveOperationException {
        validateServiceRequest(service);

        Method builderMethod = clientClass.getMethod("builder");
        Class<?> clientBuilderClass = builderMethod.getReturnType();

        Object builder = builderMethod.invoke(null);
        if (clientBuilderClass.isInstance(builder)) {
            return (T) clientBuilderClass.cast(builder);
        } else {
            throw new UnsupportedOperationException("Cannot cast builder object to AwsClientBuilder.");
        }
    }

    protected void validateServiceRequest(Service service) {
        if (!services.contains(service)) {
            throw new IllegalArgumentException("Requested service is not activated on this container.");
        }
    }
}
