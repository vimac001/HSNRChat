package de.hs_niederrhein.chat.hsnrchat.Networking;

import java.io.IOException;
import java.io.InputStream;

public class User extends NetworkObject {
    protected long Id;
    protected String user;
    protected String nick;

    public User(Response rsp) throws IOException {
        super(rsp);
    }

    public long getId() {
        return this.Id;
    }

    public String getUsername() {
        return this.user;
    }

    public String getDisplayName() {
        if(this.nick.isEmpty()) {
            this.nick = this.user;
        }

        return this.nick;
    }

    @Override
    protected void readSelf() throws IOException {
        this.Id = this.response.pullLong();
        this.user = this.response.pullString();
        this.nick = this.response.pullString();
    }
}
