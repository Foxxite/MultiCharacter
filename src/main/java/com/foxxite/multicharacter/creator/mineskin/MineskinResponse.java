package com.foxxite.multicharacter.creator.mineskin;

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
        return this.id;
    }

    public void setId(final float id) {
        this.id = id;
    }

    public String getIdStr() {
        return this.idStr;
    }

    public void setIdStr(final String idStr) {
        this.idStr = idStr;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getModel() {
        return this.model;
    }

    public void setModel(final String model) {
        this.model = model;
    }

    public Data getData() {
        return this.data;
    }

    public void setData(final Data data) {
        this.data = data;
    }

    public float getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(final float timestamp) {
        this.timestamp = timestamp;
    }

    // Setter Methods

    public float getDuration() {
        return this.duration;
    }

    public void setDuration(final float duration) {
        this.duration = duration;
    }

    public float getAccountId() {
        return this.accountId;
    }

    public void setAccountId(final float accountId) {
        this.accountId = accountId;
    }

    public String getServer() {
        return this.server;
    }

    public void setServer(final String server) {
        this.server = server;
    }

    public boolean getPrivate() {
        return this._private;
    }

    public void setPrivate(final boolean _private) {
        this._private = _private;
    }

    public float getViews() {
        return this.views;
    }

    public void setViews(final float views) {
        this.views = views;
    }

    public float getNextRequest() {
        return this.nextRequest;
    }

    public void setNextRequest(final float nextRequest) {
        this.nextRequest = nextRequest;
    }
}





