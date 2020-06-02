package com.foxxite.multicharacter.restapi.foxxite;

public class FoxxiteResponse {
    private Data data;
    private float status;
    private boolean isNew;

    // Getter Methods

    public float getStatus() {
        return status;
    }

    public void setStatus(float status) {
        this.status = status;
    }

    public Data getData() {
        return data;
    }

    // Setter Methods

    public void setData(Data data) {
        this.data = data;
    }

    public boolean getIsNew() {
        return isNew;
    }

    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }
}
