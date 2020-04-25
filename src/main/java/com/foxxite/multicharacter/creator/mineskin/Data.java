package com.foxxite.multicharacter.creator.mineskin;

public class Data {
    Texture texture;
    private String uuid;


    // Getter Methods

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    // Setter Methods

    public Texture getTexture() {
        return this.texture;
    }

    public void setTexture(final Texture texture) {
        this.texture = texture;
    }
}
