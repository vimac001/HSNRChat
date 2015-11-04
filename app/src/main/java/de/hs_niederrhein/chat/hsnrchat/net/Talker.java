package de.hs_niederrhein.chat.hsnrchat.net;

import android.util.Log;

import java.lang.Thread;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Talker {

    private Runnable listener;
    private Thread tListener;

    private Socket client;
    private OutputStream out;
    private InputStream in;


    public static final String Host = "127.0.0.1";
    public static final int Port = 1337;

    public Talker() {
        try {
            client = new Socket(Host, Port);
            if(client.isConnected())
            {
                Log.d("Talking", "Connected");

                out = client.getOutputStream();
                in = client.getInputStream();

                listener = new Listener(in);
                tListener = new Thread(listener);
                tListener.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
