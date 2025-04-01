package com.task12;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import com.task12.dto.RouteKey;
import com.task12.handlers.*;
import com.task12.handlers.auth.SignInHandler;
import com.task12.handlers.auth.SignUpHandler;
import com.task12.handlers.reservations.GetReservationsHandler;
import com.task12.handlers.reservations.PostReservationHandler;
import com.task12.handlers.tables.GetTablesByIdHandler;
import com.task12.services.CognitoService;
import com.task12.handlers.tables.GetTablesHandler;
import com.task12.handlers.tables.PostTablesHandler;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

import java.util.Map;
import java.util.regex.Pattern;

import static com.syndicate.deployment.model.environment.ValueTransformer.USER_POOL_NAME_TO_CLIENT_ID;
import static com.syndicate.deployment.model.environment.ValueTransformer.USER_POOL_NAME_TO_USER_POOL_ID;

@LambdaHandler(
        lambdaName = "api_handler",
        runtime = DeploymentRuntime.JAVA17,
        roleName = "api_handler-role",
        isPublishVersion = true,
        aliasName = "${lambdas_alias_name}",
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@DependsOn(resourceType = ResourceType.COGNITO_USER_POOL, name = "${booking_userpool}")
@EnvironmentVariables(value = {
        @EnvironmentVariable(
                key = "REGION", value = "${region}"),
        @EnvironmentVariable(
                key = "COGNITO_ID", value = "${booking_userpool}", valueTransformer = USER_POOL_NAME_TO_USER_POOL_ID),
        @EnvironmentVariable(
                key = "CLIENT_ID", value = "${booking_userpool}", valueTransformer = USER_POOL_NAME_TO_CLIENT_ID),
        @EnvironmentVariable(
                key = "TABLES_NAME", value = "${tables_table}"),
        @EnvironmentVariable(
                key = "RESERVATIONS_NAME", value = "${reservations_table}")
})
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private String region = System.getenv("REGION");
    private Map<RouteKey, RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>> handlers;
    private CognitoIdentityProviderClient cognitoClient;
    private AmazonDynamoDB dynamoDBClient;
    private String dynamicPath = "/tables/{tableId}";
    private Map<String, String> corsHeaders;

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        initCognitoClient();
        initDynamoDbClient();
        initHandlers();
        initHeadersForCORS();

        return getHandler(event)
                .handleRequest(event, context)
                .withHeaders(corsHeaders);
    }

    private RequestHandler<APIGatewayProxyRequestEvent,
            APIGatewayProxyResponseEvent> getHandler(APIGatewayProxyRequestEvent event) {
        RouteKey routeKey = new RouteKey(event.getHttpMethod(), event.getPath());

        String regex = dynamicPath.replaceAll("\\{[^/]+}", "([^/]+)");
        Pattern pattern = Pattern.compile(regex);
        if(pattern.matcher(event.getPath()).matches()) {
            routeKey.setPath(dynamicPath);
        }

        return handlers.getOrDefault(routeKey, new NotImplementedHandler());
    }

    private void initHandlers() {
        CognitoService cognitoService = new CognitoService(cognitoClient);
        handlers = Map.of(
                new RouteKey("POST", "/signup"), new SignUpHandler(cognitoService),
                new RouteKey("POST", "/signin"), new SignInHandler(cognitoService),
                new RouteKey("GET", "/tables"), new GetTablesHandler(dynamoDBClient),
                new RouteKey("POST", "/tables"), new PostTablesHandler(dynamoDBClient),
                new RouteKey("GET", "/tables/{tableId}"), new GetTablesByIdHandler(dynamoDBClient),
                new RouteKey("GET", "/reservations"), new GetReservationsHandler(dynamoDBClient),
                new RouteKey("POST", "/reservations"), new PostReservationHandler(dynamoDBClient)
        );
    }

    private void initHeadersForCORS() {
        corsHeaders = Map.of(
                "Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token",
                "Access-Control-Allow-Origin", "*",
                "Access-Control-Allow-Methods", "*",
                "Accept-Version", "*"
        );
    }

    private void initCognitoClient() {
        cognitoClient = CognitoIdentityProviderClient.builder()
                .region(Region.of(System.getenv("REGION")))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    private void initDynamoDbClient() {
        this.dynamoDBClient = AmazonDynamoDBClientBuilder.standard()
                .withRegion(region)
                .build();
    }
}
