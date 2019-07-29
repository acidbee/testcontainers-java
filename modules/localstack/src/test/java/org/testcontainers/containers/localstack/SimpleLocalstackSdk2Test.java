package org.testcontainers.containers.localstack;

import org.junit.ClassRule;
import org.junit.Test;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;
import static org.rnorth.visibleassertions.VisibleAssertions.assertTrue;
import static org.testcontainers.containers.localstack.Service.S3;
import static org.testcontainers.containers.localstack.Service.SQS;

public class SimpleLocalstackSdk2Test {

    private static final String BUCKET_1 = "bucket1";
    private static final String BUCKET_2 = "bucket2";
    private static final String ITEM_KEY = "bar";
    private static final String DUMMY_CONTENT = "baz";

    private static final Region region = Region.US_EAST_1;
    private static final AwsCredentialsProvider credProvider = AnonymousCredentialsProvider.create();

    @ClassRule
    public static LocalStackContainerSdk2 localstack = new LocalStackContainerSdk2()
                                                                .withCredentialsProvider(credProvider)
                                                                .withRegion(region)
                                                                .withServices(S3, SQS);

    @Test
    public void simpleS3Test() throws Exception {
        S3Client s3 = localstack.getServiceClient(S3, S3Client.class);

        assertThat(s3).isNotNull();
        assertThat(s3).isInstanceOf(S3Client.class);

        s3.createBucket(CreateBucketRequest.builder().bucket(BUCKET_1).acl("public-read").build());
        s3.putObject(PutObjectRequest.builder().bucket(BUCKET_1).key(ITEM_KEY).build(), RequestBody.fromString(DUMMY_CONTENT));

        s3.createBucket(CreateBucketRequest.builder().bucket(BUCKET_2).acl("public-read").build());
        s3.putObject(PutObjectRequest.builder().bucket(BUCKET_2).key(ITEM_KEY).build(), RequestBody.fromString(DUMMY_CONTENT));

        final Set<String> bucketNames = s3.listBuckets().buckets().stream()
            .map(Bucket::name)
            .collect(toSet());
        assertTrue("The created buckets have the right name",
            bucketNames.contains(BUCKET_1) && bucketNames.contains(BUCKET_2));

        assertBucketContentsCorrect(s3, BUCKET_1);
        assertBucketContentsCorrect(s3, BUCKET_2);
    }

    private void assertBucketContentsCorrect(S3Client s3, String bucketName) throws IOException {
        final ListObjectsResponse objectListing1 = s3.listObjects(ListObjectsRequest.builder().bucket(bucketName).build());
        assertEquals("The created bucket has 1 item in it", 1, objectListing1.contents().size());

        final ResponseBytes response = s3.getObject(GetObjectRequest.builder().bucket(bucketName).key(ITEM_KEY).build(), ResponseTransformer.toBytes());
        String content = response.asString(StandardCharsets.UTF_8);
        assertEquals("The object can be retrieved", DUMMY_CONTENT, content);
    }

    @Test
    public void simpleSQSTest() throws ReflectiveOperationException {
        SqsClient sqs = localstack.getServiceClient(Service.SQS, SqsClient.class);

        CreateQueueResponse queueResult = sqs.createQueue(CreateQueueRequest.builder().queueName("baz").build());
        String queueUrl = queueResult.queueUrl();

        sqs.sendMessage(SendMessageRequest.builder().queueUrl(queueUrl).messageBody("ping").build());

        final List<Message> messages = sqs.receiveMessage(ReceiveMessageRequest.builder().queueUrl(queueUrl).build()).messages();

        assertTrue("the message queue contains a message", messages.size() > 0);
        assertEquals("the first message is the one we sent", "ping", messages.get(0).body());
    }
}
