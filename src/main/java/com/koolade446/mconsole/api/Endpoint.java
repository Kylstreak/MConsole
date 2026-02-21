package com.koolade446.mconsole.api;

public record Endpoint(String category, String type) {

    public static String parseEndpoint(String endpoint) {
        return "https://centrojars.com/api/" + endpoint;
    }

    public static String parseEndpoint(String method, Endpoint endpoint) {
        return method + "/" +  endpoint.category + "/" + endpoint.type + "/";
    }
}
