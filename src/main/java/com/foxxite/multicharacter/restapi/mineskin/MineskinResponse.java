package com.foxxite.multicharacter.restapi.mineskin;

public class MineskinResponse {

    Data data;
    private float id;
    private String idStr;
    private String name;
    private String model;
    private float timestamp;
    private float duration;
    private float accountId;
    private String server;
    private boolean _private;
    private float views;
    private float nextRequest;

    // Getter Methods

    public float getId() {
        return id;
    }

    public void setId(float id) {
        this.id = id;
    }

    public String getIdStr() {
        return idStr;
    }

    public void setIdStr(String idStr) {
        this.idStr = idStr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public float getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(float timestamp) {
        this.timestamp = timestamp;
    }

    // Setter Methods

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public float getAccountId() {
        return accountId;
    }

    public void setAccountId(float accountId) {
        this.accountId = accountId;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public boolean getPrivate() {
        return _private;
    }

    public void setPrivate(boolean _private) {
        this._private = _private;
    }

    public float getViews() {
        return views;
    }

    public void setViews(float views) {
        this.views = views;
    }

    public float getNextRequest() {
        return nextRequest;
    }

    public void setNextRequest(float nextRequest) {
        this.nextRequest = nextRequest;
    }
}





