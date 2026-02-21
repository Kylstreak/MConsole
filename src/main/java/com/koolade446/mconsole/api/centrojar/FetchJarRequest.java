package com.koolade446.mconsole.api.centrojar;

import com.koolade446.mconsole.api.APIAsync;
import com.koolade446.mconsole.api.Endpoint;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class FetchJarRequest {
    private final String endpoint;

    public FetchJarRequest(Endpoint endpoint, String version) {
        this.endpoint = Endpoint.parseEndpoint("fetchJar", endpoint) + "/" + version;
    }

    public CompletableFuture<Response> send() {
        return APIAsync.sendFileRequest(endpoint).thenApply(Response::new);
    }

    public static class Response {
        private final byte[] fileBytes;

        public Response(byte[] fileBytes) {
            this.fileBytes = fileBytes;
        }

        public boolean writeToFile(String location) {
            try {
                Files.write(Paths.get(location), fileBytes);
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }
}
