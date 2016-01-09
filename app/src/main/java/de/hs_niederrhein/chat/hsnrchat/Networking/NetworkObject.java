package de.hs_niederrhein.chat.hsnrchat.Networking;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import de.hs_niederrhein.chat.hsnrchat.Networking.Streaming.StructuredInputStream;

public abstract class NetworkObject {
    protected Response response;

    protected NetworkObject(Response response) throws IOException {
        this.response = response;
        this.readSelf();
    }

    protected abstract void readSelf() throws IOException;
}
