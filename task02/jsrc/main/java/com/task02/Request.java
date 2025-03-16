package com.task02;

public class Request {

    private RequestContext requestContext;

    public RequestContext getRequestContext() {
        return requestContext;
    }

    public void setRequestContext(RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    public static class RequestContext {
        private Http http;

        public Http getHttp() {
            return http;
        }

        public void setHttp(Http http) {
            this.http = http;
        }
    }

    public static class Http {
        private String method;
        private String path;

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }
}
