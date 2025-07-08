package com.example.sonar;

import okhttp3.*;
import java.io.IOException;

public class SonarApiClient {
    private final OkHttpClient client = new OkHttpClient();
    private final String sonarUrl;
    private final String token;

    public SonarApiClient(String sonarUrl, String token) {
        this.sonarUrl = sonarUrl;
        this.token = token;
    }

    public String fetchVulnerabilities(String projectKey) throws IOException {
        HttpUrl url = HttpUrl.parse(sonarUrl + "/api/issues/search").newBuilder()
                .addQueryParameter("componentKeys", projectKey)
                .addQueryParameter("types", "VULNERABILITY")
                .addQueryParameter("ps", "100") // page size
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", Credentials.basic(token, ""))
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return response.body().string();
        }
    }
}
