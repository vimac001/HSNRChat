package de.hs_niederrhein.chat.hsnrchat.Networking;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public abstract class NetworkObject {
    protected InputStream in;

    protected NetworkObject(InputStream in) throws IOException {
        this.in = in;
        this.readSelf();
    }

    protected byte readByte() throws IOException {
        byte[] buffer = new byte[1];
        this.in.read(buffer, 0, 1);

        return buffer[0];
    }

    protected short readShort() throws IOException {
        byte[] buffer = new byte[Short.SIZE];
        this.in.read(buffer, 0, Short.SIZE);

        short data = buffer[Short.SIZE - 1];
        for(int i = Short.SIZE - 2; i >= 0; i--) {
            data <<= Byte.SIZE;
            data |= buffer[i];
        }

        return data;
    }

    protected int readInt() throws IOException {
        byte[] buffer = new byte[Integer.SIZE];
        this.in.read(buffer, 0, Integer.SIZE);

        int data = buffer[Integer.SIZE - 1];
        for(int i = Integer.SIZE - 2; i >= 0; i--) {
            data <<= Byte.SIZE;
            data |= buffer[i];
        }

        return data;
    }

    protected long readLong() throws IOException {
        byte[] buffer = new byte[Long.SIZE];
        this.in.read(buffer, 0, Long.SIZE);

        long data = buffer[Long.SIZE - 1];
        for(int i = Long.SIZE - 2; i >= 0; i--) {
            data <<= Byte.SIZE;
            data |= buffer[i];
        }

        return data;
    }

    protected String readString() throws IOException {
        int bytesCount = this.readInt();
        byte[] buffer = new byte[bytesCount];
        this.in.read(buffer, 0, bytesCount);

        return new String(buffer, Charset.defaultCharset());
    }

    protected abstract void readSelf() throws IOException;
}
