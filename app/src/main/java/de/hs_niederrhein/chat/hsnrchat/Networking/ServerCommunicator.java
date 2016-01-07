package de.hs_niederrhein.chat.hsnrchat.Networking;


import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
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

    private Semaphore reading = new Semaphore(0);
    private Semaphore writing = new Semaphore(0);

    private boolean isWaitingForLoginResponse = false;
    private Semaphore allowLoginResponse = new Semaphore(0);

    private boolean isWaitingForResolveUserResponse = false;
    private Semaphore allowResolveUserResponse = new Semaphore(0);

    private boolean isWaitingForSendAResponse = false;
    private Semaphore allowSendAResponse = new Semaphore(0);

    private boolean isWaitingForSendBResponse = false;
    private Semaphore allowSendBResponse = new Semaphore(0);

    private byte readByte() throws IOException {
        return this.is.readByte();

        /*
        byte[] buffer = new byte[1];
        this.in.read(buffer, 0, 1);

        return buffer[0];
        */
    }

    private short readShort() throws IOException {
        return this.is.readShort();
        /*
        byte[] buffer = new byte[Short.SIZE];
        this.in.read(buffer, 0, Short.SIZE);

        short data = buffer[Short.SIZE - 1];
        for(int i = Short.SIZE - 2; i >= 0; i--) {
            data <<= Byte.SIZE;
            data |= buffer[i];
        }

        return data;
        */
    }

    private int readInt() throws IOException {
        return this.is.readInt();

        /*
        byte[] buffer = new byte[Integer.SIZE];
        this.in.read(buffer, 0, Integer.SIZE);

        int data = buffer[Integer.SIZE - 1];
        for(int i = Integer.SIZE - 2; i >= 0; i--) {
            data <<= Byte.SIZE;
            data |= buffer[i];
        }

        return data;
        */
    }

    private long readLong() throws IOException {
        return this.is.readLong();
        /*
        byte[] buffer = new byte[Long.SIZE];
        this.in.read(buffer, 0, Long.SIZE);

        long data = buffer[Long.SIZE - 1];
        for(int i = Long.SIZE - 2; i >= 0; i--) {
            data <<= Byte.SIZE;
            data |= buffer[i];
        }

        return data;
        */
    }

    private String readString() throws IOException {
        return this.is.readUTF();
        /*
        int bytesCount = this.readInt();
        byte[] buffer = new byte[bytesCount];
        this.is.read(buffer, 0, bytesCount);

        return new String(buffer, Charset.defaultCharset());
        */
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

        boolean conReading = true;
        while (!Thread.currentThread().isInterrupted() && client.isConnected()) {
            try {
                if(!conReading){
                    try {
                        reading.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    conReading = true;
                }

                byte bt = this.readByte();
                if(bt >= 100) {
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
                    }
                } else {
                    ServerFunction fnc = ServerFunction.fromByte(bt);

                    if(fnc != ServerFunction.Undefined) {
                        switch (fnc) {
                            case Login:
                                if(isWaitingForLoginResponse) {
                                    conReading = false;
                                    this.reading.release();
                                    this.allowLoginResponse.release();
                                }
                                break;

                            case ResolveUser:
                                if(isWaitingForResolveUserResponse) {
                                    conReading = false;
                                    this.reading.release();
                                    this.allowResolveUserResponse.release();
                                }
                                break;

                            case SendA:
                                if(isWaitingForSendAResponse) {
                                    conReading = false;
                                    this.reading.release();
                                    this.allowSendAResponse.release();
                                }
                                break;
                            case SendB:
                                if(isWaitingForSendBResponse) {
                                    conReading = false;
                                    this.reading.release();
                                    this.allowSendBResponse.release();
                                }
                                break;
                        }
                    }
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
    public void login(String user, String pass) throws ServerErrorException, UserNotFoundException, ClientErrorException, ConnectionTimeoutException, InvalidResponseStatusException
    {
        if(this.isAuthenticated()) {
            this.logout();
        }

        Request rq = new Request(ServerFunction.Login);
        rq.addArgValue(user);
        rq.addArgValue(pass);


        if(this.isWaitingForLoginResponse)
            throw new ClientErrorException();

        try {
            this.writeData(rq.getBytes());
        } catch (IOException e) {
            //Maybe connection closed
            e.printStackTrace();
        } catch (InterruptedException e) {
            //Something went wrong in multithreading
            e.printStackTrace();
        }

        this.isWaitingForLoginResponse = true;
        try {
            if(!this.allowLoginResponse.tryAcquire(this.ResponseWaitingTime, TimeUnit.SECONDS)) {
                throw new ConnectionTimeoutException();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new ClientErrorException();
        } finally {
            this.isWaitingForLoginResponse = false;
        }

        try {
            this.reading.acquire();

            ResponseStatus status = null;
            try {
                status = this.readStatus();

                switch (status) {
                    case Success:
                        this.ssid = this.readLong();
                        break;
                    case UserNotFound:
                        throw new UserNotFoundException();
                    case ServerError:
                        throw new ServerErrorException();
                    default:
                        throw new InvalidResponseStatusException();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new ClientErrorException();
        } finally {
            this.reading.release();
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
        if(!isAuthenticated()) {
            throw new ClientNotAutheticatedException();
        }

        Request rq = new Request(ServerFunction.SendB);
        rq.addArgValue(this.getSSID());
        rq.addArgValue(userId);
        rq.addArgValue(message);

        while (isWaitingForSendBResponse) {
            try {
                Thread.currentThread().wait(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new ClientErrorException();
            }
        }

        try {
            this.writeData(rq.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        isWaitingForSendBResponse = true;

        try {
            if(!this.allowSendBResponse.tryAcquire(this.ResponseWaitingTime, TimeUnit.SECONDS)) {
                throw new ConnectionTimeoutException();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new ClientErrorException();
        } finally {
            this.isWaitingForSendBResponse = false;
        }

        try {
            this.reading.acquire();

            ResponseStatus status = null;
            try {
                status = this.readStatus();

                switch (status) {
                    case Success:
                        break;
                    case UserNotFound:
                        throw new UserNotFoundException();
                    case InvalidSSID:
                        throw new InvalidSSIDException();
                    case ServerError:
                        throw new ServerErrorException();
                    default:
                        throw new InvalidResponseStatusException();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new ClientErrorException();
        } finally {
            this.reading.release();
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
        if(!isAuthenticated()) {
            throw new ClientNotAutheticatedException();
        }

        Request rq = new Request(ServerFunction.SendA);
        rq.addArgValue(this.getSSID());
        rq.addArgValue(roomId);
        rq.addArgValue(message);

        while (isWaitingForSendAResponse) {
            try {
                Thread.currentThread().wait(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new ClientErrorException();
            }
        }

        try {
            this.writeData(rq.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        isWaitingForSendAResponse = true;

        try {
            if(!this.allowSendAResponse.tryAcquire(this.ResponseWaitingTime, TimeUnit.SECONDS)) {
                throw new ConnectionTimeoutException();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new ClientErrorException();
        } finally {
            this.isWaitingForSendAResponse = false;
        }

        try {
            this.reading.acquire();

            ResponseStatus status = null;
            try {
                status = this.readStatus();

                switch (status) {
                    case Success:
                        break;
                    case RoomNotFound:
                        throw new RoomNotFoundException();
                    case InvalidSSID:
                        throw new InvalidSSIDException();
                    case ServerError:
                        throw new ServerErrorException();
                    default:
                        throw new InvalidResponseStatusException();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new ClientErrorException();
        } finally {
            this.reading.release();
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
        if(!isAuthenticated()) {
            throw new ClientNotAutheticatedException();
        }

        Request rq = new Request(ServerFunction.ResolveUser);
        rq.addArgValue(this.getSSID());
        rq.addArgValue(userId);

        while (isWaitingForResolveUserResponse) {
            try {
                Thread.currentThread().wait(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new ClientErrorException();
            }
        }

        try {
            this.writeData(rq.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        isWaitingForResolveUserResponse = true;

        try {
            if(!this.allowResolveUserResponse.tryAcquire(this.ResponseWaitingTime, TimeUnit.SECONDS)) {
                throw new ConnectionTimeoutException();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new ClientErrorException();
        } finally {
            this.isWaitingForResolveUserResponse = false;
        }

        User u = null;
        try {
            this.reading.acquire();

            ResponseStatus status = null;
            try {
                status = this.readStatus();

                switch (status) {
                    case Success:
                        u = new User(this.in);
                        break;
                    case UserNotFound:
                        throw new UserNotFoundException();
                    case InvalidSSID:
                        throw new InvalidSSIDException();
                    case ServerError:
                        throw new ServerErrorException();
                    default:
                        throw new InvalidResponseStatusException();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new ClientErrorException();
        } finally {
            this.reading.release();
        }

        return u;
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
