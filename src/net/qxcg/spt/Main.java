package net.qxcg.spt;

/**
 * Source Port Translation Daemon
 *
 * This daemon proxies a TCP connection through a fixed outbound port.
 */
public class Main {
    private static SourcePortTranslation spt;

    public static void main(String[] args) {
        if (args.length != 4) {
            printUsage();
            System.exit(1);
        }

        // Note: Format/parse errors are *not* handled.
        int lport = Integer.parseInt(args[0]);
        int outport = Integer.parseInt(args[1]);
        String raddr = args[2];
        int rport = Integer.parseInt(args[3]);

        // The program ends when all `StreamRelay`s started by `spt` terminate.
        spt = new SourcePortTranslation(lport, outport, raddr, rport);
        spt.begin();
    }

    /**
     * Prints usage instructions to the console.
     */
    private static void printUsage() {
        System.out.println("Source Port Translation proxies a TCP connection through a fixed outbound port.");
        System.out.println("Parameters: <listen_port> <outbound_port> <remote_addr> <remote_port>");
    }
}
