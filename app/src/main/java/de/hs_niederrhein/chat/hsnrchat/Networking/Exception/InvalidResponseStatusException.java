package de.hs_niederrhein.chat.hsnrchat.Networking.Exception;

/**
 * Tritt ein, wenn das Response Byte zu keinem der erwarteten Response Status Ids passt.
 * Ein Hinweis darauf, dass entweder die Protokoll-Versionen nicht übereinstimmen oder das jemand am Netzwerkverkehr der App rum frickelt.
 */
public class InvalidResponseStatusException extends Exception {
}
