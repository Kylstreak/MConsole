package com.koolade446.mconsole.api;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class API {
    private final Map<SoftwareType, List<String>> versions;

    public API(){
        try {
            Map<SoftwareType, List<String>> options = new HashMap<>();
            for (SoftwareType st : SoftwareType.values()) {
                URL url = new URL("https://serverjars.com/api/fetchAll/" + st.getEndpoint());
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.connect();

                JSONObject response = new JSONObject(new BufferedReader(new InputStreamReader(con.getInputStream())).lines().collect(Collectors.joining()));

                con.disconnect();

                List<String> versions = new ArrayList<>();
                for (Object obj : response.getJSONArray("response")) {
                    JSONObject jsonObj = (JSONObject) obj;
                    versions.add(jsonObj.getString("version"));
                }

                options.put(st, versions);
            }
            this.versions = options;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getVersions(SoftwareType type) {
        return versions.get(type);
    }
}
