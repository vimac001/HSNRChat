package de.hs_niederrhein.chat.hsnrchat.Networking;


import java.io.IOException;
import java.io.InputStream;

public class Response {

    private ServerFunction fnc;
    private InputStream in;
    private byte status;

    private boolean ignore;

    public Response(ServerFunction toFnc, InputStream inStream) throws IOException{
        this.fnc = toFnc;

        if(toFnc != ServerFunction.Undefined) {

            this.in = inStream;

            this.ignore = false;

            this.parseFunctionResponse();
        } else {
            this.ignore = true;
        }
    }

    protected void readStatusByte() throws IOException{
        byte[] buffer = new byte[1];
        this.in.read(buffer, 0, 1);

        this.status = buffer[0];
    }

    protected void parseLogin() throws IOException {
        this.readStatusByte();


    }

    protected void parseSendToRoom() throws IOException {
        this.readStatusByte();

    }

    protected void parseSendToUser() throws IOException {
        this.readStatusByte();

    }

    protected void parseResolveUser() throws IOException {
        this.readStatusByte();

    }

    protected void parseFunctionResponse() throws IOException {
        switch (fnc) {
            case Login:
                this.parseLogin();
                break;
            case SendToRoom:
                this.parseSendToRoom();
                break;
            case SendToUser:
                this.parseSendToUser();
                break;
            case ResolveUser:
                this.parseResolveUser();
                break;

            default:
                this.ignore = true;
                break;
        }
    }

    public boolean isValid() {
        return !this.ignore;
    }
}
