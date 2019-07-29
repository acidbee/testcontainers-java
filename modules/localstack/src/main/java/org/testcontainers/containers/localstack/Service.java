package org.testcontainers.containers.localstack;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@Getter
@FieldDefaults(makeFinal = true)
public enum Service {
    API_GATEWAY("apigateway", 4567),
    KINESIS("kinesis", 4568),
    DYNAMODB("dynamodb", 4569),
    DYNAMODB_STREAMS("dynamodbstreams", 4570),
    // TODO: Clarify usage for ELASTICSEARCH and ELASTICSEARCH_SERVICE
//        ELASTICSEARCH("es",           4571),
    S3("s3", 4572),
    FIREHOSE("firehose", 4573),
    LAMBDA("lambda", 4574),
    SNS("sns", 4575),
    SQS("sqs", 4576),
    REDSHIFT("redshift", 4577),
    //        ELASTICSEARCH_SERVICE("",   4578),
    SES("ses", 4579),
    ROUTE53("route53", 4580),
    CLOUDFORMATION("cloudformation", 4581),
    CLOUDWATCH("cloudwatch", 4582),
    SSM("ssm", 4583),
    SECRETSMANAGER("secretsmanager", 4584),
    STEPFUNCTIONS("stepsfunctions", 4585),
    CLOUDWATCHLOGS("cloudwatchlogs", 4586),
    STS("sts", 4592),
    IAM("iam", 4593);

    String localStackName;

    int port;
}
