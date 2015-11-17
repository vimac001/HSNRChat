package de.hs_niederrhein.chat.hsnrchat.types;

/**
 * Created by Jennifer on 17.11.2015.
 */
public class Faculty {
    private int facID;
    private String facName;
    private int facIcon;

    public Faculty(int facID,String facName){
        this.facID = facID;
        this.facName = facName;
        this.facIcon = 0;
    }

    public Faculty(int facID,String facName, int facIcon){
        this.facID = facID;
        this.facName = facName;
        this.facIcon = facIcon;
    }

    public int getFacID() {
        return facID;
    }

    public void setFacID(int facID) {
        this.facID = facID;
    }

    public String getFacName() {
        return facName;
    }

    public void setFacName(String facName) {
        this.facName = facName;
    }
    public int getFacIcon(){
        return this.facIcon;
    }
}
