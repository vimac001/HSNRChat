package de.hs_niederrhein.chat.hsnrchat.Networking;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Listener implements Runnable {

    private InputStream in;
    private InputStreamReader inr;
    private BufferedReader reader;

    public Listener(InputStream stream) {
        in = stream;
        inr = new InputStreamReader(in);
        reader = new BufferedReader(inr);
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        try {
            while (!Thread.currentThread().isInterrupted()) {
                String msg = reader.readLine();
                Log.d("DataRec", msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
