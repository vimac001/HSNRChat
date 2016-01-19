package de.hs_niederrhein.chat.hsnrchat.Networking;


import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.ClientErrorException;
import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.ClientNotAutheticatedException;
import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.ConnectionTimeoutException;
import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.InvalidResponseStatusException;
import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.InvalidSSIDException;
import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.RoomNotFoundException;
import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.ServerErrorException;
import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.UserNotFoundException;
import de.hs_niederrhein.chat.hsnrchat.Networking.Streaming.StructuredInputStream;
import de.hs_niederrhein.chat.hsnrchat.Networking.Streaming.StructuredOutputStream;

public abstract class ServerCommunicator {

    public static final String DefaultHost = "studwow.eu";//"192.168.2.128";
    public static final int DefaultPort = 1338;

    private String host;
    private int port;

    private Thread tListener;
    private ServerHandle listener;

    private Map<ServerFunction, Response> responses;
    private Map<ServerFunction, Semaphore> rwaitings;
    private Map<Long, User> users;

    private long ssid = 0;
    private long userId = 0;

    /**
     * Initialisiert ein neues ServerCommunicator Objekt.
     * Baut auch eine neue Verbindung zum Server auf.
     * @param host Die Host Domain-/IP-Addresse, mit der sich der Communicator verbinden soll.
     * @param port Der TCP/IP Port, auf dem die Verbindung aufgebaut werden soll.
     * @throws UnknownHostException
     * @throws IOException
     */
    public ServerCommunicator(String host, int port) throws UnknownHostException {
        this.host = host;
        this.port = port;

        this.responses = new TreeMap<>();
        this.rwaitings = new TreeMap<>();
        this.users = new TreeMap<>();

        for(ServerFunction fnc : ServerFunction.values()) {
            this.rwaitings.put(fnc, new Semaphore(0));
        }


        this.listener = new ServerHandle(this);
        this.tListener = new Thread(this.listener);
        this.tListener.start();
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    /**
     * Return true, if the SSID is set.
     * @return
     */
    public boolean isAuthenticated() {
        return (this.ssid != 0);
    }

    /**
     * Authentifiziert den Benutzer und speichert seine SSID.
     * Sollte der Benutzer bereits authentifiziert sein, wird er abgemeldet und neu angemeldet.
     * @param user Benutzername
     * @param pass Kennwort
     * @throws ServerErrorException
     * @throws UserNotFoundException
     * @throws ClientErrorException
     * @throws ConnectionTimeoutException
     * @throws InvalidResponseStatusException
     */
    public void login(String user, String pass) throws ConnectionTimeoutException, UserNotFoundException, ServerErrorException, InvalidResponseStatusException, ClientErrorException {
        if(this.isAuthenticated()) {
            this.logout();
        }

        Request rq = new Request(ServerFunction.Login);
        rq.addArgValue(user);
        rq.addArgValue(pass);

        try {
            this.listener.writeData(rq.getBytes());
            this.rwaitings.get(ServerFunction.Login).acquire(); //Wait for response
            //Response is arrived
            Response rsp = this.responses.put(ServerFunction.Login, null);
            if(rsp == null)
                throw new ClientErrorException();

            switch (rsp.getStatus()) {
                case Success:
                    this.ssid = rsp.pullLong();
                    this.userId = rsp.pullLong();
                    break;
                case UserNotFound:
                    throw new UserNotFoundException();

                case ServerError:
                    throw new ServerErrorException();

                default:
                    throw new InvalidResponseStatusException();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            this.onConnectionClosed();
        }
    }

    /**
     * Meldet den Benutzer ab.
     */
    public void logout() {
        if(!isAuthenticated())
            return;

        this.ssid = 0;

        Request rq = new Request(ServerFunction.Logout);
        try {
            rq.addArgValue(this.getSSID());

            try {
                this.listener.writeData(rq.getBytes());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ConnectionTimeoutException e) {
                e.printStackTrace();
            }
        } catch (ClientNotAutheticatedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gibt die aktuelle Session Id zurück. (Erst nach der Authetifizierung aufrufbar.)
     * @return Aktuelle SSID
     * @throws ClientNotAutheticatedException
     */
    public long getSSID() throws ClientNotAutheticatedException {
        if(this.ssid == 0)
            throw new ClientNotAutheticatedException();

        return this.ssid;
    }

    /**
     * Gibt die User Id des aktuell am Gerät angemeldeten Benutzers zurück. (Erst nach der Authetifizierung aufrufbar.)
     * @return Aktuelle UserId
     * @throws ClientNotAutheticatedException
     */
    public long getUserId() throws ClientNotAutheticatedException {
        if(this.userId == 0)
            throw new ClientNotAutheticatedException();

        return this.userId;
    }

    /**
     * Sendet eine private Nachricht an den Benutzer.
     * @param userId Die Id des Empfängers.
     * @param message Die Nachricht.
     * @throws ServerErrorException
     * @throws InvalidSSIDException
     * @throws UserNotFoundException
     * @throws InvalidResponseStatusException
     * @throws ClientErrorException
     * @throws ConnectionTimeoutException
     * @throws ClientNotAutheticatedException
     */
    public void sendMessage(long userId, String message) throws ServerErrorException, InvalidSSIDException, UserNotFoundException, InvalidResponseStatusException, ClientErrorException, ConnectionTimeoutException, ClientNotAutheticatedException {
        if(!this.isAuthenticated()) {
            throw new ClientNotAutheticatedException();
        }

        Request rq = new Request(ServerFunction.SendB);
        rq.addArgValue(this.getSSID());
        rq.addArgValue(userId);
        rq.addArgValue(message);

        try {
            this.listener.writeData(rq.getBytes());
            this.rwaitings.get(ServerFunction.SendB).acquire(); //Wait for response
            //Response is arrived
            Response rsp = this.responses.put(ServerFunction.SendB, null);
            if(rsp == null)
                throw new ClientErrorException();

            switch (rsp.getStatus()) {
                case Success:
                    break;
                case UserNotFound:
                    throw new UserNotFoundException();

                case ServerError:
                    throw new ServerErrorException();

                case InvalidSSID:
                    throw new InvalidSSIDException();

                default:
                    throw new InvalidResponseStatusException();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sendet eine Nachricht an alle Benutzer im Raum.
     * @param roomId Die Raum Id, an den die Nachricht geht.
     * @param message Die Nachricht.
     * @throws ServerErrorException
     * @throws RoomNotFoundException
     * @throws InvalidSSIDException
     * @throws ClientNotAutheticatedException
     * @throws ClientErrorException
     * @throws ConnectionTimeoutException
     * @throws InvalidResponseStatusException
     */
    public void sendMessage(short roomId, String message) throws ServerErrorException, RoomNotFoundException, InvalidSSIDException, ClientNotAutheticatedException, ClientErrorException, ConnectionTimeoutException, InvalidResponseStatusException {
        if(!this.isAuthenticated()) {
            throw new ClientNotAutheticatedException();
        }

        Request rq = new Request(ServerFunction.SendA);
        rq.addArgValue(this.getSSID());
        rq.addArgValue(roomId);
        rq.addArgValue(message);

        try {
            this.listener.writeData(rq.getBytes());
            this.rwaitings.get(ServerFunction.SendA).acquire(); //Wait for response
            //Response is arrived
            Response rsp = this.responses.put(ServerFunction.SendA, null);
            if(rsp == null)
                throw new ClientErrorException();

            switch (rsp.getStatus()) {
                case Success:
                    break;
                case RoomNotFound:
                    throw new RoomNotFoundException();

                case ServerError:
                    throw new ServerErrorException();

                case InvalidSSID:
                    throw new InvalidSSIDException();

                default:
                    throw new InvalidResponseStatusException();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ruft die Daten eines Benutzers ab.
     * @param userId Die Benutzer Id des Benutzers, dessen Daten man abrufen möchte.
     * @return Die Benutzerdaten in einem User Objekt, oder null im Fehlerfall.
     * @throws ServerErrorException
     * @throws InvalidSSIDException
     * @throws UserNotFoundException
     * @throws ClientNotAutheticatedException
     * @throws ClientErrorException
     * @throws ConnectionTimeoutException
     * @throws InvalidResponseStatusException
     */
    public User resolveUser(long userId) throws ServerErrorException, InvalidSSIDException, UserNotFoundException, ClientNotAutheticatedException, ClientErrorException, ConnectionTimeoutException, InvalidResponseStatusException {
        if(this.users.containsKey(userId))
            return this.users.get(userId);

        if(!this.isAuthenticated()) {
            throw new ClientNotAutheticatedException();
        }

        User u = null;

        Request rq = new Request(ServerFunction.ResolveUser);
        rq.addArgValue(this.getSSID());
        rq.addArgValue(userId);

        try {
            this.listener.writeData(rq.getBytes());
            this.rwaitings.get(ServerFunction.ResolveUser).acquire(); //Wait for response
            //Response is arrived
            Response rsp = this.responses.put(ServerFunction.ResolveUser, null);
            if(rsp == null)
                throw new ClientErrorException();

            switch (rsp.getStatus()) {
                case Success:
                    u = new User(rsp);
                    this.users.put(u.getId(), u);
                    break;
                case UserNotFound:
                    throw new UserNotFoundException();

                case ServerError:
                    throw new ServerErrorException();

                case InvalidSSID:
                    throw new InvalidSSIDException();

                default:
                    throw new InvalidResponseStatusException();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return u;
    }

    public void onNewResponse(ServerFunction fnc, Response rsp) {
        this.responses.put(fnc, rsp);
        this.rwaitings.get(fnc).release();
    }


    /**
     * Wird bei einer neuen Privatnachricht aufgerufen.
     * @param userId Die Id des Absenders.
     * @param message Die Nachricht.
     */
    public abstract void onNewMessage(long userId, String message);

    /**
     * Wird bei einer neuen Nachricht aufgerufen.
     * @param userId Die Id des Absenders.
     * @param roomId Die Id des Raums, an den die Nachricht gesendet wurde.
     * @param message Die Nachricht.
     */
    public abstract void onNewMessage(long userId, short roomId, String message);

    /**
     * Wir aufgerufen, sobald die Verbindung zum Server unterbrochen wurde.
     */
    public abstract void onConnectionClosed();
}
