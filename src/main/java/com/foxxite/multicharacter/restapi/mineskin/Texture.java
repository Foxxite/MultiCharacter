package com.foxxite.multicharacter.restapi.mineskin;

public class Texture {
    Urls UrlsObject;
    private String value;
    private String signature;
    private String url;

    // Getter Methods

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

    // Setter Methods

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Urls getUrls() {
        return UrlsObject;
    }

    public void setUrls(Urls urlsObject) {
        UrlsObject = urlsObject;
    }
}
