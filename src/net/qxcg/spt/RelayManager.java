package net.qxcg.spt;

/**
 * Created with IntelliJ IDEA.
 * User: cgcai
 * Date: 6/6/13
 * Time: 11:08 AM
 *
 * Implementation Note:
 * We assume that no single packet can be larger than 2 GiB. The informational relayDidForwardBytes(StreamRelay, int) is left as a 32-bit signed integer.
 */
public interface RelayManager {
    /**
     * A callback executed when a `StreamRelay` forwards data.
     *
     * @param relay The `StreamRelay` that executed the callback.
     * @param forwardedBytes The number of bytes forwarded.
     */
    public void relayDidForwardBytes(StreamRelay relay, int forwardedBytes);

    /**
     * A callback executed when a `StreamRelay` is terminated.
     *
     * @param relay The `StreamRelay` that terminated.
     * @param forwardedBytes The total number of bytes forwarded.
     */
    public void relayDidTerminate(StreamRelay relay, long forwardedBytes);
}
