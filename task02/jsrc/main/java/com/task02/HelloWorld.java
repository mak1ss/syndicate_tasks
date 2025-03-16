package com.task02;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

import java.util.HashMap;
import java.util.Map;

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
public class HelloWorld implements RequestHandler<Request, Map<String, Object>> {

	public Map<String, Object> handleRequest(Request request, Context context) {
		System.out.println("Hello from lambda");

		Map<String, Object> resultMap = new HashMap();
		String path = request.getRequestContext().getHttp().getPath();
		String method = request.getRequestContext().getHttp().getMethod();

		if(method.equals("GET") && path.equals("/hello")) {
			resultMap.put("statusCode", 200);
			resultMap.put("body", "Hello from Lambda");
		} else {
			resultMap.put("statusCode", 400);
			resultMap.put("statusMessage", String.format(
					"Bad request syntax or unsupported method. Request path: %s. HTTP method: %s",
					path, method));
		}

		return resultMap;
	}
}
