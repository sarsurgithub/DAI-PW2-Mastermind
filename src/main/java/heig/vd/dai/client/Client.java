package heig.vd.dai.client;

import picocli.CommandLine;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@CommandLine.Command(name = "client", version = "1.0", mixinStandardHelpOptions = true)


public class Client implements Runnable {

    private static final int CLIENT_ID = (int) (Math.random() * 1000000);

    @CommandLine.Parameters(paramLabel = "<port>", defaultValue = "4444",
            description = "Port (default: ${DEFAULT-VALUE})")
    private int port;

    @CommandLine.Parameters(paramLabel = "<host>", defaultValue = "localhost",
            description = "Host(default: ${DEFAULT-VALUE})")
    private String host;
    @Override
    public void run() {
        System.out.println("Client");
        try (
                Socket socket = new Socket(host, port);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
                );
                BufferedWriter out = new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)
                );
                Scanner userIn = new Scanner(System.in);
        ) {
            String userCmd;
            String response;
            while(true){
                System.out.print(">> ");
                userCmd = in.readLine();
                out.write(userCmd);
                out.flush();
                while((response = in.readLine()) != null)
                    System.out.println(response);
            }
        } catch (IOException e) {
            System.out.println("[Client " + CLIENT_ID + "] exception: " + e);
        }
    }
}
