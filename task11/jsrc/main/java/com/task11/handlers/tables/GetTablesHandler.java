package com.task11.handlers.tables;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.task11.dto.Table;
import org.json.JSONObject;

import java.util.List;

public class GetTablesHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private String tableName = System.getenv("TABLES_NAME");
    private AmazonDynamoDB dynamoDBClient;

    public GetTablesHandler(AmazonDynamoDB dynamoDBClient) {
        this.dynamoDBClient = dynamoDBClient;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            ScanRequest request = new ScanRequest(tableName);
            ScanResult scanResult = dynamoDBClient.scan(request);
            List<Table> tables = scanResult.getItems().stream().map(Table::new).toList();

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(new JSONObject().put("tables", tables).toString());
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody(new JSONObject().put("error", e.getMessage()).toString());
        }
    }
}

