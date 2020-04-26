package com.foxxite.multicharacter.mojangapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MojangResponse {
    private String id;
    private String name;
    private List<Property> properties = null;
    private final Map<String, Object> additionalProperties = new HashMap<>();

    public String getId() {
        return this.id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public List<Property> getProperties() {
        return this.properties;
    }

    public void setProperties(final List<Property> properties) {
        this.properties = properties;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(final String name, final Object value) {
        this.additionalProperties.put(name, value);
    }
}
