package heig.vd.dai.server;

import picocli.CommandLine;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@CommandLine.Command(name = "server", version = "1.0", mixinStandardHelpOptions = true)
public class Server implements Runnable{
    private static final int SERVER_ID = (int) (Math.random() * 1000000);
    private static final String TEXTUAL_DATA = "👋 from Server " + SERVER_ID;



    @CommandLine.Parameters(paramLabel = "<port>", defaultValue = "4444",
            description = "File to be read as an input. (default: ${DEFAULT-VALUE})")
    private int port;
    @Override
    public void run() {
        System.out.println("Server");
        try (ServerSocket serverSocket = new ServerSocket(port);) {
            System.out.println(
                    "[Server " + SERVER_ID + "] starting with id " + SERVER_ID
            );
            System.out.println(
                    "[Server " + SERVER_ID + "] listening on port " + port
            );

            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread clientThread = new Thread(new ClientHandler(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            System.out.println("[Server " + SERVER_ID + "] exception: " + e);
        }


    }

    static class ClientHandler implements Runnable {

        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                    socket; // This allow to use try-with-resources with the socket
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
                    );
                    BufferedWriter out = new BufferedWriter(
                            new OutputStreamWriter(
                                    socket.getOutputStream(),
                                    StandardCharsets.UTF_8
                            )
                    )
            ) {
                System.out.println(
                        "[Server " +
                                SERVER_ID +
                                "] new client connected from " +
                                socket.getInetAddress().getHostAddress() +
                                ":" +
                                socket.getPort()
                );

                System.out.println(
                        "[Server " +
                                SERVER_ID +
                                "] received textual data from client: " +
                                in.readLine()
                );

                try {
                    System.out.println(
                            "[Server " +
                                    SERVER_ID +
                                    "] sleeping for 10 seconds to simulate a long operation"
                    );
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                System.out.println(
                        "[Server " +
                                SERVER_ID +
                                "] sending response to client: " +
                                TEXTUAL_DATA
                );

                out.write(TEXTUAL_DATA + "\n");
                out.flush();

                System.out.println("[Server " + SERVER_ID + "] closing connection");
            } catch (IOException e) {
                System.out.println("[Server " + SERVER_ID + "] exception: " + e);
            }
        }
    }
}
