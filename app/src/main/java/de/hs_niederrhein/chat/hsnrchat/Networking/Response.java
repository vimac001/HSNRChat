package de.hs_niederrhein.chat.hsnrchat.Networking;

import android.provider.ContactsContract;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
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
                    long ssid = this.gis.readLong();
                    long usid = this.gis.readLong();

                    this.os.writeLong(usid);
                    this.os.writeLong(ssid);
                    break;


                case ResolveUser:
                    long uid = this.gis.readLong();
                    String uname = this.gis.readUTF();
                    String unick = this.gis.readUTF();

                    this.os.writeUTF(unick);
                    this.os.writeUTF(uname);
                    this.os.writeLong(uid);
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

    protected static short switchBytes(short nw_short) {
        //byte[] bytes = ByteBuffer.allocate(Short.SIZE / Byte.SIZE).putShort(nw_short).array();
        int ret = (nw_short & 0xff);
        ret <<= Byte.SIZE;
        ret |= ((nw_short >> Byte.SIZE) & 0xff);
        return (short)ret;
    }

    public byte pullByte() throws IOException {
        return this.is.readByte();
    }

    public short pullShort() throws IOException {
        short tmp = this.is.readShort();
        return switchBytes(tmp);
    }

    public int pullInt() throws IOException {
        int tmp = this.is.readInt();
        int ret = 0;

        for(int i = 0; i < (Integer.SIZE / Short.SIZE); i++) {
            int shift = Short.SIZE * i;
            short st = (short)((tmp >> shift) & 0xffff);
            st = switchBytes(st);
            ret <<= Short.SIZE;
            ret |= (st & 0xffff);
        }

        return ret;
    }

    public long pullLong() throws IOException {
        long tmp = this.is.readLong();
        long ret = 0;

        for(int i = 0; i < (Long.SIZE / Short.SIZE); i++) {
            int shift = Short.SIZE * i;
            short st = (short)((tmp >> shift) & 0xffff);
            st = switchBytes(st);
            ret <<= Short.SIZE;
            ret |= (st & 0xffff);
        }

        return ret;
    }

    public String pullString() throws IOException {
        return this.is.readUTF();
    }
}
