package net.qxcg.spt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: cgcai
 * Date: 6/6/13
 * Time: 10:28 AM
 */
public class StreamRelay implements Runnable {
    private InputStream is;
    private OutputStream os;
    private String label;
    private RelayManager manager;
    private boolean shouldTerminate;
    private long totalBytes;
    private boolean isRunning;

    /**
     * Creates an uninitialized Stream Relay object.
     *
     * @param manager The `RelayManager` that handles callbacks. Must not be `null`.
     * @param is The `InputStream` from which bytes will be forwarded to `os`.
     * @param os The `OutputStream to which bytes from `is` will be forwarded.
     */
    public StreamRelay(RelayManager manager, InputStream is, OutputStream os) {
        this.manager = manager;
        this.is = is;
        this.os = os;

        shouldTerminate = false;
        totalBytes = 0L;
        isRunning = false;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[Constants.payload_length];
        int length;

        // This algorithm synchronously reads `Constants.payload_length` bytes from one socket and forwards them
        // to another.
        isRunning = true;
        while (!shouldTerminate) {
            try {
                Arrays.fill(buffer, (byte)0x0);
                length = is.read(buffer);
                if (length >= 0) {
                    manager.relayDidForwardBytes(this, length);
                    os.write(buffer, 0, length);
                    totalBytes += length;
                } else {
                    terminate();
                }
            } catch (SocketTimeoutException e) {
                // Catch a timeout so we do not block infinitely.
                // This is perfectly normal. Do nothing.
            } catch (IOException e) {
                e.printStackTrace();
                terminate();
            }
        }

        isRunning = false;
        manager.relayDidTerminate(this, totalBytes);
    }

    /**
     * Returns the human-readable identifier for this Stream Relay.
     *
     * @return The human-readable identifier for this Stream Relay.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the human-readable identifier for this Stream Relay.
     *
     * @param label The new label to set.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Terminates this Stream Relay.
     */
    public void terminate() {
        shouldTerminate = true;
    }

    @Override
    public String toString() {
        return getLabel();
    }

    /**
     * Returns the state of this Stream Relay.
     * @return `true` if and only if the Stream Relay is running.
     */
    public boolean isRunning() {
        return isRunning;
    }
}
