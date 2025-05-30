package com.task02;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

@LambdaHandler(
        lambdaName = "hello_world",
        roleName = "hello_world-role",
        isPublishVersion = true,
        aliasName = "${lambdas_alias_name}",
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
        authType = AuthType.NONE,
        invokeMode = InvokeMode.BUFFERED
)
public class HelloWorld implements RequestHandler<Request, LambdaResponse> {

    private final System.Logger log = System.getLogger(HelloWorld.class.getName());

    public LambdaResponse handleRequest(Request request, Context context) {
        log.log(System.Logger.Level.INFO, "Hello from Lambda!");

        String path = request.getRequestContext().getHttp().getPath();
        String method = request.getRequestContext().getHttp().getMethod();
        int status;
        String msg;

        if (method.equals("GET") && path.equals("/hello")) {
            status = 200;
            msg = "Hello from Lambda";
        } else {
            status = 400;
            msg = String.format("Bad request syntax or unsupported method. Request path: %s. HTTP method: %s",
                    path, method);
        }

        Response rsp = new Response(status, msg);

        try {
            return new LambdaResponse(status, rsp.toJson());
        } catch (JsonProcessingException e) {
            log.log(System.Logger.Level.ERROR, "Error occurred during JSON serialization", e);
            return new LambdaResponse(500, "Error occurred during JSON serialization");
        }
    }
}
