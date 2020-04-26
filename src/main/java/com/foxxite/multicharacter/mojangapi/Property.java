package com.foxxite.multicharacter.mojangapi;

import java.util.HashMap;
import java.util.Map;

public class Property {
    private String name;
    private String value;
    private String signature;
    private final Map<String, Object> additionalProperties = new HashMap<>();

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public String getSignature() {
        return this.signature;
    }

    public void setSignature(final String signature) {
        this.signature = signature;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(final String name, final Object value) {
        this.additionalProperties.put(name, value);
    }
}
