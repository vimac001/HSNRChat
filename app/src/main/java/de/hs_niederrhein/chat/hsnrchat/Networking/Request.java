package de.hs_niederrhein.chat.hsnrchat.Networking;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Request {

    private List<Byte> data;

    public Request(ServerFunction fnc) {
        data = new ArrayList<>();
        data.add(fnc.getId());
    }

    public void addArgValue(byte[] data) {
        for (byte bt: data) {
            this.data.add(bt);
        }
    }

    public void addArgValue(String str) {
        byte[] data = str.getBytes(Charset.defaultCharset());
        this.addArgValue(data.length);
        this.addArgValue(data);
    }

    public void addArgValue(byte bt) {
        this.data.add(bt);
    }

    public void addArgValue(short st) {
        this.data.add((byte)(st & 0xff));
        this.data.add((byte)((st >> 8) & 0xff));
    }

    public void addArgValue(int st) {
        this.data.add((byte)(st & 0xff));
        this.data.add((byte)((st >> 8) & 0xff));
        this.data.add((byte)((st >> 8) & 0xff));
        this.data.add((byte)((st >> 8) & 0xff));
    }

    public void addArgValue(long st) {
        this.data.add((byte)(st & 0xff));
        this.data.add((byte)((st >> 8) & 0xff));
        this.data.add((byte)((st >> 8) & 0xff));
        this.data.add((byte)((st >> 8) & 0xff));
        this.data.add((byte)((st >> 8) & 0xff));
        this.data.add((byte)((st >> 8) & 0xff));
        this.data.add((byte)((st >> 8) & 0xff));
        this.data.add((byte)((st >> 8) & 0xff));
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
