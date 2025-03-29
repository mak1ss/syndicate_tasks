package com.task09;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.ArtifactExtension;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import com.weather_sdk.WeatherClient;
import com.weather_sdk.dto.WeatherResponse;

import java.util.Map;

@LambdaHandler(
        lambdaName = "api_handler",
        layers = "weather_sdk",
        roleName = "api_handler-role",
        isPublishVersion = true,
        aliasName = "${lambdas_alias_name}",
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaLayer(
        layerName = "weather_sdk",
        libraries = {"lib/weather-sdk-1.0-SNAPSHOT.jar"},
        runtime = DeploymentRuntime.JAVA11,
        artifactExtension = ArtifactExtension.ZIP
)
@LambdaUrlConfig(
        authType = AuthType.NONE,
        invokeMode = InvokeMode.BUFFERED
)
public class ApiHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private final String apiUrl = "https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41" +
            "&current=temperature_2m,wind_speed_10m&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m";

    private final ObjectMapper mapper = new ObjectMapper();

    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent request, Context context) {
        String path = request.getRequestContext().getHttp().getPath();
        var logger = context.getLogger();
        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();

        logger.log("Received request: " + path);

        try {
            Map<String, Object> body;
            if (!path.equals("/weather")) {
                String msg = String.format("Bad request syntax or unsupported method. " +
                                "Request path: %s. HTTP method: %s",
                        path, request.getRequestContext().getHttp().getMethod());
                body = Map.of(
                        "statusCode", 400,
                        "message", msg
                );

                response = APIGatewayV2HTTPResponse.builder()
                        .withStatusCode(400)
                        .withBody(mapper.writeValueAsString(body))
                        .build();
            } else {
                WeatherClient client = new WeatherClient(apiUrl);

                WeatherResponse weatherResponse = client.getRecentWeather();

                if(weatherResponse != null) {
                    logger.log("Got recent weather response: " + weatherResponse);
                }

                body = Map.of(
                        "statusCode", 200,
                        "body", weatherResponse
                );

                response = APIGatewayV2HTTPResponse.builder()
                        .withStatusCode(200)
                        .withBody(mapper.writeValueAsString(body))
                        .build();
            }

        } catch (JsonProcessingException e) {
            logger.log("Error occurred during JSON serialization: " + e.getMessage());
        } catch (Exception e) {
            logger.log("Unknown error occurred: " + e);
        }

        logger.log("Returning response: " + response);

        return response;
    }
}
