package com.foxxite.multicharacter.restapi.mineskin;

public class Data {
    Texture texture;
    private String uuid;

    // Getter Methods

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    // Setter Methods

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }
}
