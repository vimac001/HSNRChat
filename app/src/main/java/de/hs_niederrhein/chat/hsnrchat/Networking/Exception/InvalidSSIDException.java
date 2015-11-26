package de.hs_niederrhein.chat.hsnrchat.Networking.Exception;

/**
 * Tritt ein, wenn die aktuelle SSID nicht im Server registriert ist.
 * Wenn diese Exception auftritt, dann ist es ein Hinweis auf einen Man in the Middle Angriff oder schlechten Server-Neustart.
 */
public class InvalidSSIDException extends Exception {
}
