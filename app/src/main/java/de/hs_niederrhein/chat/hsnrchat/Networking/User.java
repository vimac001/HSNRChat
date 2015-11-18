package de.hs_niederrhein.chat.hsnrchat.Networking;

public class User {
    protected long Id;
    protected String user;
    protected String nick;

    public User(long id) {
        this.Id = id;
        this.user = "testUser";
        this.nick = "Nick Name";
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
}
