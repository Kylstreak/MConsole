package com.koolade446.mconsole.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EndpointsList extends CopyOnWriteArrayList<Endpoint> {

    public Endpoint getByType(String type) {
        for (Endpoint endpoint : this) {
            if (endpoint.type().equals(type)) {
                return endpoint;
            }
        }
        return null;
    }
}
