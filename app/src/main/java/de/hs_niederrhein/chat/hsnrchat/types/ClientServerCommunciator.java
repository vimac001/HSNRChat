package de.hs_niederrhein.chat.hsnrchat.types;

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

/**
 * Created by Jennifer on 05.01.2016.
 */
public class ClientServerCommunciator extends ServerCommunicator {

    private static ClientServerCommunciator obj = null;

    public static ClientServerCommunciator connect(String host, int port){
        if(obj == null) {
            try {
                obj = new ClientServerCommunciator(host,port);
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        return obj;
    }

    private ClientServerCommunciator(String host, int port) throws UnknownHostException, IOException {
        super(host, port);
    }

    public static void LoginUser(String uname, String passwd) throws UserNotFoundException, ConnectionTimeoutException, InvalidResponseStatusException, ServerErrorException, ClientErrorException {

        obj.login(uname, passwd);

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
