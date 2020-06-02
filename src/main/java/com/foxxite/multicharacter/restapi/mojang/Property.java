package com.foxxite.multicharacter.restapi.mojang;

import java.util.HashMap;
import java.util.Map;

public class Property {
    private final Map<String, Object> additionalProperties = new HashMap<>();
    private String name;
    private String value;
    private String signature;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        additionalProperties.put(name, value);
    }
}
