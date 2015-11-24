package de.hs_niederrhein.chat.hsnrchat.Networking;


import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.InvalidSSIDException;
import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.RoomNotFoundException;
import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.ServerErrorException;
import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.UserNotFoundException;

public abstract class ServerCommunicator {

    protected long ssid = 0;
    protected Listener listener;

    public ServerCommunicator(Listener listener) {
        this.listener = listener;
    }

    /**
     * Authentifiziert den Benutzer und speichert seine SSID.
     * @param user Benutzername
     * @param pass Kennwort
     * @throws ServerErrorException
     * @throws UserNotFoundException
     */
    public void login(String user, String pass) throws ServerErrorException, UserNotFoundException
    {

    }

    /**
     * Meldet den Benutzer ab.
     */
    public void logout() {
        this.ssid = 0;

    }

    /**
     * Gibt die aktuelle Session Id zurück. (Erst nach der Authetifizierung aufrufbar.)
     * @return Aktuelle SSID
     * @throws InvalidSSIDException
     */
    public long getSSID() throws InvalidSSIDException {
        if(this.ssid == 0)
            throw new InvalidSSIDException();

        return this.ssid;
    }

    /**
     * Sendet eine private Nachricht an den Benutzer.
     * @param userId Die Id des Empfängers.
     * @param message Die Nachricht.
     * @throws ServerErrorException
     * @throws InvalidSSIDException
     * @throws UserNotFoundException
     */
    public void sendMessage(long userId, String message) throws ServerErrorException, InvalidSSIDException, UserNotFoundException
    {

    }

    /**
     * Sendet eine Nachricht an alle Benutzer im Raum.
     * @param roomId Die Raum Id, an den die Nachricht geht.
     * @param message Die Nachricht.
     * @throws ServerErrorException
     * @throws RoomNotFoundException
     * @throws InvalidSSIDException
     */
    public void sendMessage(short roomId, String message) throws ServerErrorException, RoomNotFoundException, InvalidSSIDException
    {

    }

    /**
     * Ruft die Daten eines Benutzers ab.
     * @param userId Die Benutzer Id des Benutzers, dessen Daten man abrufen möchte.
     * @return Die Benutzerdaten in einem User Objekt, oder null im Fehlerfall.
     * @throws ServerErrorException
     * @throws InvalidSSIDException
     * @throws UserNotFoundException
     */
    public User resolveUser(long userId) throws ServerErrorException, InvalidSSIDException, UserNotFoundException
    {
        return null;
    }

    /**
     * Wird bei einer neuen Privatnachricht aufgerufen.
     * @param userId Die Id des Absenders.
     * @param message Die Nachricht.
     */
    public abstract void onNewMessage(long userId, String message);

    /**
     * Wird bei einer neuen Nachricht aufgerufen.
     * @param userId Die Id des Absenders.
     * @param roomId Die Id des Raums, an den die Nachricht gesendet wurde.
     * @param message Die Nachricht.
     */
    public abstract void onNewMessage(long userId, short roomId, String message);
}
