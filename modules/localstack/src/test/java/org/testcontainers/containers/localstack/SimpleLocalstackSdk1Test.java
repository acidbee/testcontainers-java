package org.testcontainers.containers.localstack;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.Message;
import org.apache.commons.io.IOUtils;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;
import static org.rnorth.visibleassertions.VisibleAssertions.assertTrue;
import static org.testcontainers.containers.localstack.Service.S3;
import static org.testcontainers.containers.localstack.Service.SQS;

import static org.assertj.core.api.Assertions.*;

/**
 * The only tests that should go here are for those Localstack services that require special testcontainers code to work with them.
 * For example, S3 needs a test to ensure special HTTP routing to the S3 container works.
 */
public class SimpleLocalstackSdk1Test {

    private static final String BUCKET_1 = "bucket1";
    private static final String BUCKET_2 = "bucket2";
    private static final String ITEM_KEY = "bar";
    private static final String DUMMY_CONTENT = "baz";

    @ClassRule
    public static LocalStackContainerSdk1 localstack = new LocalStackContainerSdk1()
                                                                .withServices(S3, SQS);

    @Test
    public void simpleS3Test() throws IOException, ReflectiveOperationException {
        AmazonS3 s3 = localstack.getServiceClient(S3, AmazonS3Client.class);

        assertThat(s3).isNotNull();
        assertThat(s3).isInstanceOf(AmazonSQSClient.class);

        s3.createBucket(BUCKET_1);
        s3.putObject(BUCKET_1, ITEM_KEY, DUMMY_CONTENT);

        s3.createBucket(BUCKET_2);
        s3.putObject(BUCKET_2, ITEM_KEY, DUMMY_CONTENT);

        final Set<String> bucketNames = s3.listBuckets().stream()
            .map(Bucket::getName)
            .collect(toSet());
        assertTrue("The created buckets have the right name",
            bucketNames.contains(BUCKET_1) && bucketNames.contains(BUCKET_2));

        assertBucketContentsCorrect(s3, BUCKET_1);
        assertBucketContentsCorrect(s3, BUCKET_2);
    }

    private void assertBucketContentsCorrect(AmazonS3 s3, String bucketName) throws IOException {
        final ObjectListing objectListing1 = s3.listObjects(bucketName);
        assertEquals("The created bucket has 1 item in it", 1, objectListing1.getObjectSummaries().size());

        final S3Object object = s3.getObject(bucketName, ITEM_KEY);
        final String content = IOUtils.toString(object.getObjectContent(), StandardCharsets.UTF_8);
        assertEquals("The object can be retrieved", DUMMY_CONTENT, content);
    }

    @Test
    public void simpleSQSTest() throws ReflectiveOperationException {
        AmazonSQS sqs = localstack.getServiceClient(Service.SQS, AmazonSQSClient.class);

        CreateQueueResult queueResult = sqs.createQueue("baz");
        String queueUrl = queueResult.getQueueUrl();

        sqs.sendMessage(queueUrl, "ping");

        final List<Message> messages = sqs.receiveMessage(queueUrl).getMessages();

        assertTrue("the message queue contains a message", messages.size() > 0);
        assertEquals("the first message is the one we sent", "ping", messages.get(0).getBody());
    }
}
