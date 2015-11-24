package de.hs_niederrhein.chat.hsnrchat.Networking;


import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.InvalidSSIDException;
import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.RoomNotFoundException;
import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.ServerErrorException;
import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.UserNotFoundException;

public abstract class ServerCommunicator implements Runnable {

    public static final String DefaultHost = "192.168.2.113";
    public static final int DefaultPort = 1337;

    private static final int DefaulBufferSize = 256;

    private Thread listener;

    private Socket client;
    private InetAddress addr;

    private OutputStream out;
    private InputStream in;

    private InputStreamReader inReader;
    private BufferedReader reader;

    protected long ssid = 0;

    /**
     * Initialisiert ein neues ServerCommunicator Objekt.
     * Baut auch eine neue Verbindung zum Server auf.
     * @param host Die Host Domain-/IP-Addresse, mit der sich der Communicator verbinden soll.
     * @param port Der TCP/IP Port, auf dem die Verbindung aufgebaut werden soll.
     * @throws UnknownHostException
     * @throws IOException
     */
    public ServerCommunicator(String host, int port) throws UnknownHostException, IOException {
        this.addr = InetAddress.getByName(host);
        client = new Socket(this.addr, port);

        if(this.client.isConnected()) {
            this.out = client.getOutputStream();
            this.in = client.getInputStream();

            this.inReader = new InputStreamReader(this.in);
            this.reader = new BufferedReader(this.inReader);

            this.listener = new Thread(this);
            //this.listener.start();
        }
    }

    @Override
    public void run() {
        byte[] buffer = new byte[DefaulBufferSize];
        while (!Thread.currentThread().isInterrupted()) {
            try {
                this.in.read(buffer, 0, 1);
                if(buffer[0] >= 100) {
                    //TODO: Fnc call
                } else {
                    Response rsp = new Response(ServerFunction.fromByte(buffer[0]), this.in);
                    if(rsp.isValid()) {
                        //TODO: Add response to special mega super duper responses list.
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
     */
    public void login(String user, String pass) throws ServerErrorException, UserNotFoundException, IOException
    {
        if(this.isAuthenticated()) {
            this.logout();
        }

        Request rq = new Request(ServerFunction.Login);
        rq.addArgValue(user);
        rq.addArgValue(pass);

        out.write(rq.getBytes());

    }

    /**
     * Meldet den Benutzer ab.
     */
    public void logout() {
        this.ssid = 0;

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
     */
    public void sendMessage(long userId, String message) throws ServerErrorException, InvalidSSIDException, UserNotFoundException
    {

    }

    /**
     * Sendet eine Nachricht an alle Benutzer im Raum.
     * @param roomId Die Raum Id, an den die Nachricht geht.
     * @param message Die Nachricht.
     * @throws ServerErrorException
     * @throws RoomNotFoundException
     * @throws InvalidSSIDException
     */
    public void sendMessage(short roomId, String message) throws ServerErrorException, RoomNotFoundException, InvalidSSIDException
    {

    }

    /**
     * Ruft die Daten eines Benutzers ab.
     * @param userId Die Benutzer Id des Benutzers, dessen Daten man abrufen möchte.
     * @return Die Benutzerdaten in einem User Objekt, oder null im Fehlerfall.
     * @throws ServerErrorException
     * @throws InvalidSSIDException
     * @throws UserNotFoundException
     */
    public User resolveUser(long userId) throws ServerErrorException, InvalidSSIDException, UserNotFoundException
    {
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
}
