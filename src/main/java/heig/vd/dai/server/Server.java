package heig.vd.dai.server;

import picocli.CommandLine;

@CommandLine.Command(name = "server", version = "1.0", mixinStandardHelpOptions = true)
public class Server implements Runnable{
    @Override
    public void run() {
        System.out.println("Server");
    }
}
