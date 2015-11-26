package de.hs_niederrhein.chat.hsnrchat.Networking;

import java.io.IOException;
import java.io.InputStream;

public class User extends NetworkObject {
    protected long Id;
    protected String user;
    protected String nick;

    public User(InputStream in) throws IOException {
        super(in);
    }

    public long getId() {
        return this.Id;
    }

    public String getUsername() {
        return this.user;
    }

    public String getDisplayName() {
        return this.nick;
    }

    @Override
    protected void readSelf() throws IOException {
        this.Id = this.readLong();
        this.user = this.readString();
        this.nick = this.readString();
    }
}
