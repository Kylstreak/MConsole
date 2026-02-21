package com.koolade446.mconsole.api;

import com.google.gson.Gson;
import com.koolade446.mconsole.api.centrojar.FetchAllRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.*;

public class APIAsync {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();

    public static final EndpointsList ENDPOINTS = new EndpointsList();


    public static <T> CompletableFuture<T> sendJsonRequest(String endpoint, Class<T> clazz) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = URI.create(Endpoint.parseEndpoint(endpoint));

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(uri)
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) throw new RuntimeException("Failed to send request to " + uri);

                return GsonHelper.GSON.fromJson(response.body(), clazz);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

        }, executor);
    }

    public static CompletableFuture<byte[]> sendFileRequest(String endpoint) {
        requireCollected();
        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = URI.create(Endpoint.parseEndpoint(endpoint));

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(uri)
                        .GET()
                        .build();

                HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

                if (response.statusCode() != 200) throw new RuntimeException("Failed to send request to " + uri);

                return response.body();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    public static void collectEndpoints() {
        FetchAllRequest request = new FetchAllRequest();
        request.send().thenApply(FetchAllRequest.Response::getEndpoints)
                .thenAccept(ENDPOINTS::addAll)
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    public static void shutdown() {
        executor.shutdown();
    }

    private static void requireCollected() {
        if (ENDPOINTS.isEmpty()) throw new IllegalStateException("Endpoints not collected yet");
    }
}
