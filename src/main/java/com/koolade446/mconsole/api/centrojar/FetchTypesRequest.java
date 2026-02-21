package com.koolade446.mconsole.api.centrojar;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koolade446.mconsole.api.APIAsync;
import com.koolade446.mconsole.api.Endpoint;
import com.koolade446.mconsole.api.GsonHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FetchTypesRequest {
    private final String endpoint;
    private Response resp;

    public FetchTypesRequest(String category, String type) {
        endpoint = "fetchAll/" + category + "/" + type;
    }

    public CompletableFuture<Response> send() {
        return APIAsync.sendJsonRequest(endpoint, Response.class).whenComplete((response, error) -> resp = response);
    }

    public List<Response.JarType> getTypes() {
        return resp.getTypes();
    }



    public static class Response {
        public String status;
        public JsonArray response;

        public List<JarType> getTypes() {
            List<JarType> types = new ArrayList<>();

            for (JsonElement type : response) {
                JsonObject obj = type.getAsJsonObject();

                JarType instance = GsonHelper.GSON.fromJson(obj, JarType.class);
                types.add(instance);
            }

            return types;
        }

        public static class JarType {
            public String version;
            public String file;
            public JsonObject size;
            public String md5;
            public String stability;
        }
    }
}
