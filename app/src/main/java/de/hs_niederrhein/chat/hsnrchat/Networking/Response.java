package de.hs_niederrhein.chat.hsnrchat.Networking;

import android.provider.ContactsContract;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import de.hs_niederrhein.chat.hsnrchat.Networking.Streaming.StructuredInputStream;
import de.hs_niederrhein.chat.hsnrchat.Networking.Streaming.StructuredOutputStream;

public class Response {

    private List<Byte> data = new ArrayList<>();
    private OutputStream out = new OutputStream() {
        @Override
        public void write(int oneByte) throws IOException {
            data.add((byte)oneByte);
        }
    };
    private InputStream in = new InputStream() {
        @Override
        public int read() throws IOException {
            int idx = data.size() - 1;
            byte bt = data.get(idx);
            data.remove(idx);
            return bt;
        }
    };

    private StructuredOutputStream os;
    private StructuredInputStream is;

    protected StructuredInputStream gis;
    protected ServerFunction fnc;
    protected ResponseStatus status;

    protected void init() throws IOException {
        this.os = new StructuredOutputStream(this.out);
        this.is = new StructuredInputStream(this.in);

        this.status = this.gis.readStatus();

        if(this.status == ResponseStatus.Success) {
            switch (this.fnc) {
                case Login:
                    this.os.writeLong(this.gis.readLong()); //Store SSID
                    break;
            }
        }
    }

    public Response(StructuredInputStream is, ServerFunction fnc) throws IOException {
        this.gis = is;
        this.fnc = fnc;

        this.init();
    }

    public ResponseStatus getStatus() {
        return this.status;
    }

    public byte pullByte() throws IOException {
        return this.is.readByte();
    }

    public short pullShort() throws IOException {
        return this.is.readShort();
    }

    public int pullInt() throws IOException {
        return this.is.readInt();
    }

    public long pullLong() throws IOException {
        return this.is.readLong();
    }

    public String pullString() throws IOException {
        return this.is.readUTF();
    }
}
