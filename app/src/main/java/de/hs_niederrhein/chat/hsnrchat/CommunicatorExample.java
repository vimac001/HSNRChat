package de.hs_niederrhein.chat.hsnrchat;

import java.io.IOException;
import java.net.UnknownHostException;

import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.ClientErrorException;
import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.ClientNotAutheticatedException;
import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.ConnectionTimeoutException;
import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.InvalidResponseStatusException;
import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.InvalidSSIDException;
import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.ServerErrorException;
import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.UserNotFoundException;
import de.hs_niederrhein.chat.hsnrchat.Networking.ServerCommunicator;
import de.hs_niederrhein.chat.hsnrchat.Networking.User;


public class CommunicatorExample extends ServerCommunicator {


    public CommunicatorExample(String host, int port) throws UnknownHostException, IOException {
        super(host, port);

        /*
        try {
            this.login("root", "toor");
        } catch (ServerErrorException e) {
            e.printStackTrace();
        } catch (UserNotFoundException e) {
            e.printStackTrace();
        } catch (ConnectionTimeoutException e) {
            e.printStackTrace();
        } catch (InvalidResponseStatusException e) {
            e.printStackTrace();
        } catch (ClientErrorException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void onNewMessage(long userId, String message) {
        try {
            User user = this.resolveUser(userId);
            //Work with User

        } catch (ServerErrorException e) {
            e.printStackTrace();
        } catch (InvalidSSIDException e) {
            e.printStackTrace();
        } catch (UserNotFoundException e) {
            e.printStackTrace();
        } catch (ConnectionTimeoutException e) {
            e.printStackTrace();
        } catch (InvalidResponseStatusException e) {
            e.printStackTrace();
        } catch (ClientErrorException e) {
            e.printStackTrace();
        } catch (ClientNotAutheticatedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNewMessage(long userId, short roomId, String message) {

    }

    @Override
    public void onConnectionClosed() {

    }

}
