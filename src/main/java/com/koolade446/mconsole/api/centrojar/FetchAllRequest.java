package com.koolade446.mconsole.api.centrojar;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koolade446.mconsole.api.APIAsync;
import com.koolade446.mconsole.api.Endpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FetchAllRequest {
    private final String endpoint = "fetchJar/fetchAllTypes.php";
    private Response resp;

    public CompletableFuture<Response> send() {
        return APIAsync.sendJsonRequest(endpoint, Response.class).whenComplete((response, error) -> resp = response);
    }

    public Response getResp() {
        return resp;
    }

    public static class Response {
        public String status;
        public JsonObject response;

        public JsonArray get(String category) {
            return response.getAsJsonArray(category);
        }

        public List<Endpoint> getEndpoints() {
            List<Endpoint> endpoints = new ArrayList<>();
            for (String category : response.keySet()) {
                JsonArray types = response.getAsJsonArray(category);
                for (JsonElement type : types) {
                    endpoints.add(new Endpoint(category, type.getAsString()));
                }
            }
            return endpoints;
        }
    }
}
