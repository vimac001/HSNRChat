package de.hs_niederrhein.chat.hsnrchat.Networking;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.ConnectionTimeoutException;
import de.hs_niederrhein.chat.hsnrchat.Networking.Streaming.StructuredInputStream;
import de.hs_niederrhein.chat.hsnrchat.Networking.Streaming.StructuredOutputStream;

public class ServerHandle implements Runnable {

    /**
     * Die Wartezeit auf eine Antwort vom Server (in Sekunden).
     */
    public static int ResponseWaitingTime = 15;

    protected ServerCommunicator com;
    protected ServerWriter writer;
    protected Thread tWriter;

    protected InetAddress addr;
    protected int port;

    protected boolean conFailed = false;

    protected InputStream in;
    protected OutputStream out;

    protected StructuredInputStream is;
    protected StructuredOutputStream os;

    protected Semaphore writing = new Semaphore(0);

    protected Socket client = null;

    public ServerHandle(ServerCommunicator com) {
        this.com = com;
        this.port = com.getPort();

        this.writer = new ServerWriter(this);
        this.tWriter = new Thread(this.writer);
    }

    public boolean isConnected() {
        return this.client.isConnected();
    }

    public void sendData(byte[] data) throws IOException {
        this.os.write(data);
        this.os.flush();
    }

    public void writeData(byte[] data) throws InterruptedException, ConnectionTimeoutException {
        if(this.conFailed)
            return;

        if(this.client == null || !this.client.isConnected())
            if(!this.writing.tryAcquire(ServerHandle.ResponseWaitingTime, TimeUnit.SECONDS))
                throw new ConnectionTimeoutException();
            else
                this.writing.acquire();

        this.writer.write(data);

        this.writing.release();
    }

    @Override
    public void run() {
        if(this.client == null || !this.client.isConnected()) {
            try {
                this.addr = InetAddress.getByName(this.com.getHost());

                this.client = new Socket(this.addr, this.port);

                if(this.client.isConnected()) {
                    this.out = client.getOutputStream();
                    this.in = client.getInputStream();

                    this.is = new StructuredInputStream(this.in);
                    this.os = new StructuredOutputStream(this.out);

                    this.tWriter.start();

                    this.writing.release();
                }
            } catch (IOException e) {
                //Connectiong failed
                e.printStackTrace();
                this.conFailed = true;
                return;
            }
        }

        while (!Thread.currentThread().isInterrupted() && this.client.isConnected()) {
            try {
                byte bt = this.is.readByte();

                if(ServerFunction.fromByte(bt) == ServerFunction.Undefined) {
                    ClientFunction fnc = ClientFunction.fromByte(bt);

                    if(fnc != ClientFunction.Undefined) {
                        long uid;
                        short rid;
                        String msg;

                        switch (fnc) {
                            case ReceiveA:
                                uid = this.is.readLong();
                                rid = this.is.readShort();
                                msg = this.is.readUTF();
                                this.com.onNewMessage(uid, rid, msg);
                                break;
                            case ReceiveB:
                                uid = this.is.readLong();
                                msg = this.is.readUTF();
                                this.com.onNewMessage(uid, msg);
                                break;
                        }
                    } else {
                        //Bad Function Id
                        Log.e("UnexpectedError", "Unknown functionId call.");
                    }
                } else {
                    ServerFunction fnc = ServerFunction.fromByte(bt);

                    Response tmp = new Response(this.is, fnc);
                    this.com.onNewResponse(fnc, tmp);
                }
            } catch (IOException e) {
                this.onConnectionClosed();
            }
        }
    }

    public void onConnectionClosed() {
        this.com.onConnectionClosed();
    }
}
