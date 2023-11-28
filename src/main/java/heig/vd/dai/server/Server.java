package heig.vd.dai.server;

import picocli.CommandLine;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@CommandLine.Command(name = "server", version = "1.0", mixinStandardHelpOptions = true)
public class Server implements Runnable {
    @CommandLine.Option(names = "-p", defaultValue = "4444",
            description = "Port of the server (default: ${DEFAULT-VALUE})")
    private int port;

    @CommandLine.Option(names = "-t", defaultValue = "2",
            description = "Number of threads (default: ${DEFAULT-VALUE})")
    private int nb_thread = 2;
    private final ExecutorService pool; // Thread pool
    public Server() {
        this.pool = Executors.newFixedThreadPool(nb_thread);
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                ClientHandler clientHandler = new ClientHandler(socket);
                pool.submit(clientHandler); // Submit to the thread pool
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            if (pool != null) {
                pool.shutdown();
            }
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private MastermindGame game;
        private boolean gameInProgress;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.gameInProgress = false;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(),StandardCharsets.UTF_8));
                 PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)) {

                printWriter.println("OK");

                String clientMessage;


                while (true) {
                    try {
                        clientMessage = reader.readLine();
                        if (clientMessage == null) {
                            System.out.println("Client disconnected.");
                            return;
                        }
                    } catch (IOException e) {
                        System.out.println("Client disconnected.");
                        return;
                    }
                    String[] serverParts = clientMessage.split(" ");
                    switch (clientMessage.split(" ")[0]) {
                        case "START":
                            startGame(printWriter, serverParts);
                            break;
                        case "RULES":
                            if (serverParts.length == 1) {
                                sendText(printWriter, "SEND RULES");
                            } else {
                                printWriter.println("ERROR Invalid command. Type 'HELP' for a list of commands.");
                            }
                            break;
                        case "HELP":
                            if (serverParts.length == 1) {
                                sendText(printWriter, "SEND COMMANDS");
                            } else {
                                printWriter.println("ERROR Invalid command. Type 'HELP' for a list of commands.");
                            }
                            break;
                        case "TRY":
                            if (gameInProgress) {
                                handleTry(printWriter, clientMessage.substring(4));
                            } else {
                                printWriter.println("ERROR No game started");
                            }
                            break;
                        case "QUIT":
                            if (gameInProgress) printWriter.println("FINISHED LOST");
                            socket.close();
                            System.out.println("Client disconnected.");
                            return;
                        default:
                            printWriter.println("ERROR Invalid command. Type 'HELP' for a list of commands.");
                            break;
                    }
                }
            } catch (IOException ex) {
                System.out.println("Server exception: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        private void startGame(PrintWriter writer, String[] serverParts) {
            if (serverParts.length == 1) {
                game = new MastermindGame();
            } else {
                game = new MastermindGame(Integer.parseInt(serverParts[1]), Integer.parseInt(serverParts[2]));
            }
            gameInProgress = true;
            writer.println("STARTED with " + game.getNbPins() + " pins and " + game.getNbTry() + " tries.");

        }

        private void sendText(PrintWriter writer, String type) {
            String filepath;
            if (Objects.equals(type, "SEND RULES")) {
                filepath = "ressources/rules.txt";
            } else if (Objects.equals(type, "SEND COMMANDS")) {
                filepath = "ressources/help.txt";
            } else {
                return;
            }


            File file = new File(filepath);

            if (file.exists()) {
                try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(filepath), StandardCharsets.UTF_8))) {
                    StringBuilder wholeText = new StringBuilder();
                    wholeText.append(type).append(" : ");
                    String line;
                    while ((line = fileReader.readLine()) != null) {
                        wholeText.append("\n").append(line);
                    }
                    writer.println(wholeText);
                } catch (IOException e) {
                    writer.println("ERROR Unable to read file " + type + ".");
                    System.out.println("Error reading file " + type + ": " + e.getMessage());
                }
            } else {
                writer.println("ERROR:" + type + " file not found.");
            }
        }

        private void handleTry(PrintWriter writer, String propositionString) {

            char[] proposition = propositionString.toCharArray();
            // Check length
            boolean error = proposition.length != game.getNbPins();
            // Check if all characters are valid symbols
            String validSymbolsRegex = "[RBGY]+";
            error = error || !propositionString.matches(validSymbolsRegex);

            if (error) {
                writer.println("ERROR Invalid TRY command format. It should be " + game.getNbPins() + " characters long and only contain the letters RGBY.");
                return;
            }

            //if command okay
            int[] clues = game.getHint(proposition);
            if (game.isCorrect(proposition)) {
                writer.println("FINISHED WON");
                gameInProgress = false;
            } else if (game.getTurnLeft() == 0) {
                writer.println("FINISHED LOST");
                gameInProgress = false;
            } else {
                writer.println("ANSWER " + clues[0] + " " + clues[1] + " " + game.getTurnLeft());
            }
        }
    }
}
