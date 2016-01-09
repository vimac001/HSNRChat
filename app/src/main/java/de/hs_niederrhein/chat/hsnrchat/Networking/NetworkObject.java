package de.hs_niederrhein.chat.hsnrchat.Networking;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import de.hs_niederrhein.chat.hsnrchat.Networking.Streaming.StructuredInputStream;

public abstract class NetworkObject {
    protected InputStream in;
    protected StructuredInputStream is;

    protected NetworkObject(InputStream in) throws IOException {
        this.in = in;
        this.is = new StructuredInputStream(this.in);
        this.readSelf();
    }

    protected byte readByte() throws IOException {
        return this.is.readByte();
    }

    protected short readShort() throws IOException {
        return this.is.readShort();
    }

    protected int readInt() throws IOException {
        return this.is.readInt();
    }

    protected long readLong() throws IOException {
        return this.is.readLong();
    }

    protected String readString() throws IOException {
        return this.is.readUTF();
    }

    protected abstract void readSelf() throws IOException;
}
