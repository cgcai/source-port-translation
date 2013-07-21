package net.qxcg.spt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created with IntelliJ IDEA.
 * User: cgcai
 * Date: 6/6/13
 * Time: 10:05 AM
 */
public class SourcePortTranslation implements RelayManager {
    private int inboundPort;
    private int outboundPort;
    private String remoteAddress;
    private int remotePort;
    private Socket remoteSend;

    /**
     * Creates an unconnected Source Port Translation object.
     *
     * @param inboundPort The port on `localhost` on which to accept connections.
     * @param outboundPort The outbound port to use when connecting to `remoteAddress`.
     * @param remoteAddress The address of the remote host to connect to.
     * @param remotePort The port on the remote host to connect to.
     */
    public SourcePortTranslation(int inboundPort, int outboundPort, String remoteAddress, int remotePort) {
        this.inboundPort = inboundPort;
        this.outboundPort = outboundPort;
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
    }

    /**
     * Negotiates the TCP connection to the remote end point.
     *
     * @return `true` if and only if the remote connection was successful.
     */
    private boolean connectRemote() {
        try {
            // The remote socket needs to be bound to a specific source address.
            remoteSend = new Socket(InetAddress.getByName(remoteAddress), remotePort, InetAddress.getLocalHost(), outboundPort);
            remoteSend.setSoTimeout(Constants.so_timeout);
            System.out.println("Remote connection established.");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Connects the Source Port Translation object.
     *
     * Tries to establish a remote connection, and if successful, starts listening on the local port.
     * If the remote connection could not be established, this method has no side effects.
     */
    public void begin() {
        boolean remoteConnected = connectRemote();
        if (remoteConnected) {
            try {
                // The server socket needs no special configuration.
                ServerSocket server = new ServerSocket(inboundPort);
                Socket localListen = server.accept();
                localListen.setSoTimeout(Constants.so_timeout);
                System.out.println("Local client connected.");

                establishRelay("Local->Remote", localListen.getInputStream(), remoteSend.getOutputStream());
                System.out.println("Forward relay active.");

                establishRelay("Remote->Local", remoteSend.getInputStream(), localListen.getOutputStream());
                System.out.println("Backward relay active.");
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Initializes and starts a relay that forwards bytes from `src` to `dst` on a new thread.
     *
     * @param label A human-readable string to identify the relay. Pass in an empty string if irrelevant.
     * @param src The source `InputStream`.
     * @param dst The sink `OutputStream`.
     * @return A reference to the relay that was constructed.
     */
    private StreamRelay establishRelay(String label, InputStream src, OutputStream dst) {
        StreamRelay relay = new StreamRelay(this, src, dst);
        relay.setLabel(label);
        new Thread(relay).start();
        return relay;
    }

    @Override
    public void relayDidForwardBytes(StreamRelay relay, int forwardedBytes) {
        System.out.format("Relay: %s forwarded %d bytes.\n", relay.getLabel(), forwardedBytes);
    }

    @Override
    public void relayDidTerminate(StreamRelay relay, long forwardedBytes) {
        System.out.format("Relay: %s terminated after forwarding %d bytes.\n", relay.getLabel(), forwardedBytes);
    }
}
