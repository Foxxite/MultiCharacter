package com.foxxite.multicharacter.restapi.foxxite;

public class Data {
    private String ID;
    private String UserID;
    private String ResourceID;
    private String Platform;
    private String IP;
    private String ActivationDate;
    private String Active;

    // Getter Methods

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String UserID) {
        this.UserID = UserID;
    }

    public String getResourceID() {
        return ResourceID;
    }

    public void setResourceID(String ResourceID) {
        this.ResourceID = ResourceID;
    }

    public String getPlatform() {
        return Platform;
    }

    // Setter Methods

    public void setPlatform(String Platform) {
        this.Platform = Platform;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String getActivationDate() {
        return ActivationDate;
    }

    public void setActivationDate(String ActivationDate) {
        this.ActivationDate = ActivationDate;
    }

    public String getActive() {
        return Active;
    }

    public void setActive(String Active) {
        this.Active = Active;
    }
}
