package de.hs_niederrhein.chat.hsnrchat.Networking.Streaming;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class StructuredInputStream extends DataInputStream {
    /**
     * Constructs a new StructuredInputStream on the InputStream {@code in}. All
     * reads are then filtered through this stream. Note that data read by this
     * stream is not in a human readable format and was most likely created by a
     * StructuredOutputStream.
     * <p/>
     * <p><strong>Warning:</strong> passing a null source creates an invalid
     * {@code StructuredInputStream}. All operations on such a stream will fail.
     *
     * @param in the source InputStream the filter reads from.
     * @see StructuredInputStream
     * @see DataOutputStream
     * @see RandomAccessFile
     */
    public StructuredInputStream(InputStream in) {
        super(in);
    }

}
