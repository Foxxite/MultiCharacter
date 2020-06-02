package com.foxxite.multicharacter.restapi.mojang;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MojangResponse {
    private final Map<String, Object> additionalProperties = new HashMap<>();
    private String id;
    private String name;
    private List<Property> properties = null;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        additionalProperties.put(name, value);
    }
}
