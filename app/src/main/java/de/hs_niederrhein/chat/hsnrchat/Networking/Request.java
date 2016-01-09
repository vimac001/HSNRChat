package de.hs_niederrhein.chat.hsnrchat.Networking;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import de.hs_niederrhein.chat.hsnrchat.Networking.Streaming.StructuredOutputStream;

public class Request {

    OutputStream out;
    StructuredOutputStream os;

    private List<Byte> data;

    public Request(ServerFunction fnc) {
        data = new ArrayList<>();
        this.out = new OutputStream() {
            @Override
            public void write(int oneByte) throws IOException {
                data.add((byte)oneByte);
            }
        };

        this.os = new StructuredOutputStream(this.out);

        try {
            this.os.writeFunction(fnc);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addArgValue(byte[] data) {
        try {
            this.os.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addArgValue(String str) {
        try {
            this.os.writeUTF(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addArgValue(byte bt) {
        try {
            this.os.writeByte(bt);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addArgValue(short st) {
        try {
            this.os.writeShort(st);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addArgValue(int st) {
        try {
            this.os.writeInt(st);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addArgValue(long st) {
        try {
            this.os.writeLong(st);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] getBytes() {
        byte[] retValue = new byte[this.data.size()];
        int i = 0;
        for (byte bt: this.data) {
            retValue[i++] = bt;
        }

        return retValue;
    }
}
