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

public abstract class ServerCommunicator implements Runnable {

    public static final String DefaultHost = "192.168.2.113";
    public static final int DefaultPort = 1337;

    /**
     * Die Wartezeit auf eine Antwort vom Server (in Sekunden).
     */
    protected int ResponseWaitingTime = 1500;

    private Thread listener;

    private Socket client = null;
    private InetAddress addr = null;
    private int port;

    private boolean conFailed = false;

    private OutputStream out;
    private InputStream in;

    private StructuredInputStream is;
    private StructuredOutputStream os;

    private Semaphore writing = new Semaphore(0);

    private Map<ServerFunction, Response> responses;
    private Map<ServerFunction, Semaphore> rwaitings;

    private byte readByte() throws IOException {
        return this.is.readByte();
    }

    private short readShort() throws IOException {
        return this.is.readShort();
    }

    private int readInt() throws IOException {
        return this.is.readInt();
    }

    private long readLong() throws IOException {
        return this.is.readLong();
    }

    private String readString() throws IOException {
        return this.is.readUTF();
    }

    private ResponseStatus readStatus() throws IOException {
        return ResponseStatus.fromByte(this.readByte());
    }

    private long ssid = 0;

    protected void writeData(byte[] data) throws IOException, InterruptedException, ConnectionTimeoutException {
        if(this.conFailed)
            return;

        if(this.client == null || !this.client.isConnected())
            if(!this.writing.tryAcquire(this.ResponseWaitingTime, TimeUnit.SECONDS))
                throw new ConnectionTimeoutException();
        else
                this.writing.acquire();

        this.os.write(data);
        this.os.flush();
        this.writing.release();
    }

    /**
     * Initialisiert ein neues ServerCommunicator Objekt.
     * Baut auch eine neue Verbindung zum Server auf.
     * @param host Die Host Domain-/IP-Addresse, mit der sich der Communicator verbinden soll.
     * @param port Der TCP/IP Port, auf dem die Verbindung aufgebaut werden soll.
     * @throws UnknownHostException
     * @throws IOException
     */
    public ServerCommunicator(String host, int port) throws UnknownHostException {
        this.addr = InetAddress.getByName(host);
        this.port = port;

        this.responses = new TreeMap<>();
        this.rwaitings = new TreeMap<>();
        for(ServerFunction fnc : ServerFunction.values()) {
            this.rwaitings.put(fnc, new Semaphore(0));
        }


        this.listener = new Thread(this);
        this.listener.start();
    }

    @Override
    public void run() {
        if(client == null || !client.isConnected()) {
            try {
                this.client = new Socket(this.addr, this.port);

                if(this.client.isConnected()) {
                    this.out = client.getOutputStream();
                    this.in = client.getInputStream();

                    this.is = new StructuredInputStream(this.in);
                    this.os = new StructuredOutputStream(this.out);

                    this.writing.release();
                }
            } catch (IOException e) {
                //Connectiong failed
                e.printStackTrace();
                this.conFailed = true;
                return;
            }
        }

        while (!Thread.currentThread().isInterrupted() && client.isConnected()) {
            try {
                byte bt = this.readByte();

                if(ServerFunction.fromByte(bt) == ServerFunction.Undefined) {
                    ClientFunction fnc = ClientFunction.fromByte(bt);

                    if(fnc != ClientFunction.Undefined) {
                        long uid;
                        short rid;
                        String msg;

                        switch (fnc) {
                            case ReceiveA:
                                uid = this.readLong();
                                rid = this.readShort();
                                msg = this.readString();
                                this.onNewMessage(uid, rid, msg);
                                break;
                            case ReceiveB:
                                uid = this.readLong();
                                msg = this.readString();
                                this.onNewMessage(uid, msg);
                                break;
                        }
                    } else {
                        //Bad Function Id
                        Log.e("UnexpectedError", "Unknown functionId call.");
                    }
                } else {
                    ServerFunction fnc = ServerFunction.fromByte(bt);

                    Response tmp = new Response(this.is, fnc);
                    responses.put(fnc, tmp);
                    rwaitings.get(fnc).release();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(!client.isConnected())
            this.onConnectionClosed();
    }

    /**
     * Return true, if the SSID is set.
     * @return
     */
    public boolean isAuthenticated() {
        return (this.ssid > 0);
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
            this.writeData(rq.getBytes());
            this.rwaitings.get(ServerFunction.Login).acquire(); //Wait for response
            //Response is arrived
            Response rsp = this.responses.put(ServerFunction.Login, null);
            if(rsp == null)
                throw new ClientErrorException();

            switch (rsp.getStatus()) {
                case Success:
                    this.ssid = rsp.pullLong();
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
            e.printStackTrace();
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
                this.writeData(rq.getBytes());
            } catch (IOException e) {
                this.onConnectionClosed();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ConnectionTimeoutException e) {
                e.printStackTrace();
            }
        } catch (InvalidSSIDException e) {
            e.printStackTrace();
            //Ich fresse einen Besen, wenn dieser Fall jemals eintritt.
        }
    }

    /**
     * Gibt die aktuelle Session Id zurück. (Erst nach der Authetifizierung aufrufbar.)
     * @return Aktuelle SSID
     * @throws InvalidSSIDException
     */
    public long getSSID() throws InvalidSSIDException {
        if(this.ssid == 0)
            throw new InvalidSSIDException();

        return this.ssid;
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

        return null;
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
