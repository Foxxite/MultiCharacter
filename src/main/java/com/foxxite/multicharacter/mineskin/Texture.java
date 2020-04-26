package com.foxxite.multicharacter.mineskin;

public class Texture {
    Urls UrlsObject;
    private String value;
    private String signature;
    private String url;

    // Getter Methods

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

    // Setter Methods

    public String getUrl() {
        return this.url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public Urls getUrls() {
        return this.UrlsObject;
    }

    public void setUrls(final Urls urlsObject) {
        this.UrlsObject = urlsObject;
    }
}
