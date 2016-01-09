package de.hs_niederrhein.chat.hsnrchat.types;

/**
 * Created by Jennifer on 05.01.2016.
 */
public class Message {
    private long userID;
    private String message;
    private boolean isRight = false;


    public Message(long userID, String message, boolean isRight){
        this.userID = userID;
        this.message = message;
        this.isRight = isRight;
    }

    public long getUserID(){return this.userID;}
    public String getMessage(){return this.message;}
    public boolean getIsRight(){return this.isRight;}

    public void setUserID(long userID){this.userID = userID;}
    public void setMessage(String message){this.message = message;}
    public void setRight(boolean isRight){this.isRight = isRight;}

}
