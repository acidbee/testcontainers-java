package org.testcontainers.containers.localstack;

public class LocalStackContainerProvider {

    public static LocalStackContainer newInstance(AwsSdkVersion sdkVersion) {
        return newInstance(sdkVersion, null);
    }

    public static LocalStackContainer newInstance(AwsSdkVersion sdkVersion, String containerVersion) {
        if (sdkVersion == null) {
            throw new IllegalArgumentException("AWS SDK version cannot be null. Accepted values [V1, V2]");
        }
        switch (sdkVersion) {
            case V1:
                if (isNullOrEmpty(containerVersion)) {
                    return new LocalStackContainerSdk1();
                } else {
                    return new LocalStackContainerSdk1(containerVersion);
                }
            case V2:
                if (isNullOrEmpty(containerVersion)) {
                    return new LocalStackContainerSdk2();
                } else {
                    return new LocalStackContainerSdk2(containerVersion);
                }
            default:
                throw new IllegalArgumentException("SDK value received is invalid.");
        }
    }

    private static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }

    public enum AwsSdkVersion {
        V1,
        V2;
    }
}
