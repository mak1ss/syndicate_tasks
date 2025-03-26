package com.task06;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.Record;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.EventSource;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.EventSourceType;
import com.syndicate.deployment.model.RetentionSetting;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@LambdaHandler(
        lambdaName = "audit_producer",
        roleName = "audit_producer-role",
        isPublishVersion = true,
        aliasName = "${lambdas_alias_name}",
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@EventSource(
        eventType = EventSourceType.DYNAMODB_TRIGGER
)
@DynamoDbTriggerEventSource(
        targetTable = "Configuration",
        batchSize = 1
)
@EnvironmentVariables(value = {
        @EnvironmentVariable(key = "table_name", value = "${target_table}"),
        @EnvironmentVariable(key = "region", value = "${region}") }
)
public class AuditProducer implements RequestHandler<DynamodbEvent, Void> {

    private AmazonDynamoDB dynamoDB;
    private final String auditTable = System.getenv("table_name");
    private final String region = System.getenv("region");
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Void handleRequest(DynamodbEvent event, Context context) {
        var logger = context.getLogger();
        initDynamoDbClient();

        try {
            logger.log("Received event: " + mapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            logger.log("Error occurred while parsing event: " + e.getMessage());
        }

        event.getRecords().forEach(r -> persistData(r, context));

        return null;
    }

    private void persistData(Record record, Context context) {
        Map<String, AttributeValue> attributesMap = new HashMap<>();
        String id = UUID.randomUUID().toString();

        List<String> updatedKeys = record.getDynamodb().getKeys().values()
                .stream()
                .map(com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue::getS)
                .collect(Collectors.toList());

        updatedKeys.forEach(key -> {
            attributesMap.put("id", new AttributeValue().withS(id));

            String modificationTime = DateTimeFormatter.ISO_INSTANT.format(
                    record.getDynamodb().getApproximateCreationDateTime().toInstant().truncatedTo(ChronoUnit.MILLIS)
            );
            attributesMap.put("modificationTime", new AttributeValue().withS(modificationTime));

            attributesMap.put("itemKey", new AttributeValue().withS(key));

            String eventName = record.getEventName();
            if (eventName.equals("INSERT")) {
                handleInsert(attributesMap, record);
            } else if (eventName.equals("MODIFY")) {
                handleModify(attributesMap, record);
            }
        });

        context.getLogger().log("Attribute map: " + attributesMap);
        dynamoDB.putItem(new PutItemRequest(auditTable, attributesMap));
    }

    private void initDynamoDbClient() {
        this.dynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withRegion(region)
                .build();
    }

    private void handleInsert(Map<String, AttributeValue> attributesMap, Record record) {
        Map<String, AttributeValue> insertedItem = new HashMap<>();

        record.getDynamodb().getNewImage().forEach((k, v) ->
                insertedItem.put(k, copyAttributeValue(v)));

        attributesMap.put("newValue", new AttributeValue().withM(insertedItem));
    }

    private void handleModify(Map<String, AttributeValue> attributesMap, Record record) {
        var oldImage = record.getDynamodb().getOldImage();
        var newImage = record.getDynamodb().getNewImage();
        String updatedAttribute = "";
        AttributeValue oldAttributeValue = null;
        AttributeValue newAttributeValue = null;

        for (Map.Entry<String, com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue> entry
                : oldImage.entrySet()) {
            if (!Objects.equals(newImage.get(entry.getKey()), entry.getValue())) {
                updatedAttribute = entry.getKey();
                oldAttributeValue = copyAttributeValue(entry.getValue());
                newAttributeValue = copyAttributeValue(newImage.get(entry.getKey()));
            }
        }
        attributesMap.put("updatedAttribute", new AttributeValue().withS(updatedAttribute));
        attributesMap.put("oldValue", oldAttributeValue);
        attributesMap.put("newValue", newAttributeValue);
    }

    private AttributeValue copyAttributeValue(
            com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue value) {
        if (value == null) return new AttributeValue();
        AttributeValue newValue = new AttributeValue();

        if (value.getS() != null) newValue.setS(value.getS());
        if (value.getN() != null) newValue.setN(value.getN());
        if (value.getBOOL() != null) newValue.setBOOL(value.getBOOL());
        if (value.getB() != null) newValue.setB(value.getB());

        return newValue;
    }
}
