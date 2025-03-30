package com.task10;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.ArtifactExtension;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.TracingMode;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import com.weather_sdk.WeatherClient;
import com.weather_sdk.dto.WeatherResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@LambdaHandler(
        lambdaName = "processor",
        layers = "weather_sdk",
        roleName = "processor-role",
        isPublishVersion = true,
        aliasName = "${lambdas_alias_name}",
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED,
        tracingMode = TracingMode.Active
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
@EnvironmentVariables(value = {
        @EnvironmentVariable(key = "table_name", value = "${target_table}"),
        @EnvironmentVariable(key = "region", value = "${region}") }
)
public class Processor implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private AmazonDynamoDB client;
    private String tableName = System.getenv("table_name");
    private String region = System.getenv("region");

    private final String weatherApiUrl = "https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41" +
            "&current=temperature_2m,wind_speed_10m&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m";
    private final WeatherClient weatherClient = new WeatherClient(weatherApiUrl);

    private LambdaLogger logger;

    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        logger = context.getLogger();
        logger.log("Lambda was triggered by URL");

        initDynamoDbClient();

        var internalServerError = APIGatewayV2HTTPResponse.builder()
                .withStatusCode(500)
                .withBody("Internal Server Error")
                .build();

        WeatherResponse forecast = retrieveRecentForecast();
        if (forecast == null) {
            return internalServerError;
        }

        var putItemResult = persistForecast(forecast);
        if (putItemResult == null) {
            return internalServerError;
        }

        return APIGatewayV2HTTPResponse.builder()
                .withStatusCode(201)
                .withBody("Successfully persisted recent forecast")
                .build();
    }

    private void initDynamoDbClient() {
        this.client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(region)
                .build();
    }

    private WeatherResponse retrieveRecentForecast() {
        WeatherResponse recentForecast = null;
        try {
            recentForecast = weatherClient.getRecentWeather();
        } catch (Exception e) {
            logger.log("Error occurred during forecast retrieving: " + e);
        }

        return recentForecast;
    }

    private PutItemResult persistForecast(WeatherResponse forecast) {
        var valueMap = mapToDynamoDBItem(forecast);
        PutItemRequest request = new PutItemRequest(tableName, valueMap);

        return client.putItem(request);
    }

    public static Map<String, AttributeValue> mapToDynamoDBItem(WeatherResponse weatherResponse) {
        Map<String, AttributeValue> item = new HashMap<>();

        item.put("id", new AttributeValue().withS(UUID.randomUUID().toString()));

        Map<String, AttributeValue> forecast = new HashMap<>();

        forecast.put("latitude", new AttributeValue().withN(String.valueOf(weatherResponse.getLatitude())));
        forecast.put("longitude", new AttributeValue().withN(String.valueOf(weatherResponse.getLongitude())));
        forecast.put("elevation", new AttributeValue().withN(String.valueOf(weatherResponse.getElevation())));
        forecast.put("generationtime_ms", new AttributeValue()
                .withN(String.valueOf(weatherResponse.getGenerationTimeMs())));
        forecast.put("utc_offset_seconds", new AttributeValue()
                .withN(String.valueOf(weatherResponse.getUtcOffsetSeconds())));
        forecast.put("timezone", new AttributeValue().withS(weatherResponse.getTimezone()));
        forecast.put("timezone_abbreviation", new AttributeValue().withS(weatherResponse.getTimezoneAbbreviation()));

        Map<String, AttributeValue> hourly = new HashMap<>();
        hourly.put("time", new AttributeValue()
                .withL(convertListToAttributeValues(weatherResponse.getHourly().getTime(), String.class)));
        hourly.put("temperature_2m", new AttributeValue()
                .withL(convertListToAttributeValues(weatherResponse.getHourly().getTemperature2m(), Double.class)));

        forecast.put("hourly", new AttributeValue().withM(hourly));

        Map<String, AttributeValue> hourlyUnits = weatherResponse.getHourlyUnits().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new AttributeValue().withS(e.getValue())));
        forecast.put("hourly_units", new AttributeValue().withM(hourlyUnits));

        item.put("forecast", new AttributeValue().withM(forecast));

        return item;
    }

    private static List<AttributeValue> convertListToAttributeValues(List<?> list, Class<?> itemType) {
        return list.stream()
                .map(value -> {
                    var attributeValue = new AttributeValue();
                    if (itemType.isAssignableFrom(Number.class)) {
                        attributeValue.withN(value.toString());
                    } else {
                        attributeValue.withS(value.toString());
                    }
                    return attributeValue;
                })
                .collect(Collectors.toList());
    }
}
