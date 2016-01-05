package de.hs_niederrhein.chat.hsnrchat.types;

import android.content.Context;

import java.io.IOException;
import java.net.UnknownHostException;

import de.hs_niederrhein.chat.hsnrchat.Database.DatabaseOpenHelper;
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
    private DatabaseOpenHelper db ;
    private Context context;

    public static ClientServerCommunciator connect(Context contex,String host, int port){
        if(obj == null) {
            try {
                obj = new ClientServerCommunciator(contex, host,port);
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        return obj;
    }

    private ClientServerCommunciator(Context contex,String host, int port) throws UnknownHostException, IOException {
        super(host, port);
        this.context = contex;
        this.db = new DatabaseOpenHelper(contex);
    }

    public static void LoginUser(String uname, String passwd) throws UserNotFoundException, ConnectionTimeoutException, InvalidResponseStatusException, ServerErrorException, ClientErrorException {

        obj.login(uname, passwd);

    }

    @Override //für Privat Chats
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

    @Override //Für Chaträume
    public void onNewMessage(long userId, short roomId, String message) {
        db.insertMessage(roomId,message,(int)userId);
    }

    @Override
    public void onConnectionClosed() {

    }

}
