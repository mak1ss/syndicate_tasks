package com.task08;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.RuleEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.Dependencies;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@LambdaHandler(
        lambdaName = "uuid_generator",
        roleName = "uuid_generator-role",
        isPublishVersion = true,
        aliasName = "${lambdas_alias_name}",
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@RuleEventSource(
        targetRule = "uuid_trigger"
)
@EnvironmentVariables(value = {
        @EnvironmentVariable(key = "region", value = "${region}"),
        @EnvironmentVariable(key = "bucket_name", value = "${target_bucket}")}
)
@Dependencies(value = {
        @DependsOn(
                name = "${target_bucket}",
                resourceType = ResourceType.S3_BUCKET
        ),
        @DependsOn(
                name = "uuid_trigger",
                resourceType = ResourceType.CLOUDWATCH_RULE
        )
})

public class UuidGenerator implements RequestHandler<Object, Void> {

    private AmazonS3 s3;

    private String region = System.getenv("region");

    private String bucketName = System.getenv("bucket_name");

    public Void handleRequest(Object request, Context context) {
        var logger = context.getLogger();
        logger.log("Lambda handler invoked.");

        List<String> ids = IntStream.range(0, 10)
                .mapToObj(i -> UUID.randomUUID().toString())
                .collect(Collectors.toList());

        String timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now());

        Map<String, List<String>> jsonMap = new HashMap<>();
        jsonMap.put("ids", ids);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonContent = objectMapper.writeValueAsString(jsonMap);

            s3.putObject(bucketName, timestamp, jsonContent);
            logger.log("File uploaded to S3: " + timestamp + "\n");

        } catch (JsonProcessingException e) {
            logger.log("Error: " + e.getMessage());
        }

        initS3Storage();
        return null;
    }

    private void initS3Storage() {
        s3 = AmazonS3ClientBuilder
                .standard()
                .withRegion(region)
                .build();
    }
}
