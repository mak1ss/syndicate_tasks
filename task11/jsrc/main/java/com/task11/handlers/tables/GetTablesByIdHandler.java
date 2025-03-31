package com.task11.handlers.tables;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.task11.dto.Table;
import org.json.JSONObject;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetTablesByIdHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private String tableName = System.getenv("TABLES_NAME");
    private AmazonDynamoDB dynamoDB;
    private static final Pattern ID_PATTERN = Pattern.compile(".*/(\\d+)$");

    public GetTablesByIdHandler(AmazonDynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        Integer id = extractIdFromPath(event.getPath());
        try {
            GetItemRequest request = new GetItemRequest()
                    .withTableName(tableName)
                    .withKey(Map.of(
                            "id", new AttributeValue().withS(id.toString())
                    ));
            GetItemResult result = dynamoDB.getItem(request);
            if(result == null) {
                throw new IllegalArgumentException("Table with id " + id + " not found");
            }

            Table found = new Table(result.getItem());

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(new JSONObject().put("table", new JSONObject(found)).toString());
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody(new JSONObject().put("error", e.getMessage()).toString());
        }
    }

    private Integer extractIdFromPath(String path) {
        Matcher matcher = ID_PATTERN.matcher(path);
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }
}
