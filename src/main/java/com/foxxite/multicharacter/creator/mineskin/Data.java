package com.foxxite.multicharacter.creator.mineskin;

public class Data {
    Texture TextureObject;
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
        return this.TextureObject;
    }

    public void setTexture(final Texture textureObject) {
        this.TextureObject = textureObject;
    }
}
