package de.hs_niederrhein.chat.hsnrchat.types;

/**
 * Created by Jennifer on 05.01.2016.
 */
public class Message {
    private long roomID;
    private long userID;
    private String message;
    private boolean isRight = false;


    public Message(long roomID, long userID, String message, boolean isRight){
        this.roomID = roomID;
        this.userID = userID;
        this.message = message;
        this.isRight = isRight;
    }

    public long getUserID(){return this.userID;}
    public long getRoomID(){return this.roomID;}
    public String getMessage(){return this.message;}
    public boolean getIsRight(){return this.isRight;}

    public void setUserID(long userID){this.userID = userID;}
    public void setRoomID(long roomID){this.roomID = roomID;}
    public void setMessage(String message){this.message = message;}
    public void setRight(boolean isRight){this.isRight = isRight;}

}
