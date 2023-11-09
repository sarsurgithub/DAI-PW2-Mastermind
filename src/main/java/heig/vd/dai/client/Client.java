package heig.vd.dai.client;

import picocli.CommandLine;

@CommandLine.Command(name = "client", version = "1.0", mixinStandardHelpOptions = true)

public class Client implements Runnable {
    @Override
    public void run() {
        System.out.println("Client");
    }
}
