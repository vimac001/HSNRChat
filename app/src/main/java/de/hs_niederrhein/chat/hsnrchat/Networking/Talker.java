package de.hs_niederrhein.chat.hsnrchat.Networking;

import android.util.Log;

import java.lang.Thread;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Talker {

    private Runnable listener;
    private Thread tListener;

    private Socket client;
    private OutputStream out;
    private InputStream in;


    public static final String Host = "192.168.2.113";
    public static final int Port = 1337;

    public Talker() {
        try {
            InetAddress addr = InetAddress.getByName(Host);

            client = new Socket(addr, Port);
            if(client.isConnected())
            {
                Log.d("Talking", "Connected");

                out = client.getOutputStream();
                in = client.getInputStream();

                listener = new Listener(in);
                tListener = new Thread(listener);
                tListener.start();
            }

        }  catch (UnknownHostException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
