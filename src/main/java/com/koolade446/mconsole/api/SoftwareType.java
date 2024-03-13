package com.koolade446.mconsole.api;

public enum SoftwareType {
    PUR_PUR("servers/purpur"),
    PAPER("servers/paper"),
    VANILLA("vanilla/vanilla"),
    VANILLA_SNAPSHOT("vanilla/snapshot"),
    FORGE("modded/forge"),
    FABRIC("modded/fabric");

    final String endpoint;
    SoftwareType(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getEndpoint() {
        return endpoint;
    }

}