package heig.vd.dai.server;

import picocli.CommandLine;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

@CommandLine.Command(name = "server", version = "1.0", mixinStandardHelpOptions = true)
public class Server implements Runnable{
    @CommandLine.Parameters(paramLabel = "<port>", defaultValue = "4444",
            description = "Port of the server (default: ${DEFAULT-VALUE})")
    private int port;
    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                new Thread(new ClientHandler(socket)).start();
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    class ClientHandler implements Runnable {
        private final Socket socket;
        private MastermindGame game;
        private boolean gameInProgress;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.gameInProgress = false;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

                writer.println("OK");

                String clientMessage;
                while ((clientMessage = reader.readLine()) != null) {
                    switch (clientMessage.split(" ")[0]) {
                        case "START":
                            startGame(writer);
                            break;
                        case "RULES":
                            sendRules(writer);
                            break;
                        case "HELP":
                            sendHelp(writer);
                            break;
                        case "TRY":
                            if (gameInProgress) {
                                handleTry(writer, clientMessage.substring(4).toCharArray());
                            } else {
                                writer.println("ERROR 403");
                            }
                            break;
                        case "QUIT":
                            writer.println("FINISHED LOST");
                            socket.close();
                            return;
                        default:
                            writer.println("ERROR 400");
                            socket.close();
                            return;
                    }
                }
            } catch (IOException ex) {
                System.out.println("Server exception: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        private void startGame(PrintWriter writer) {
            game = new MastermindGame();
            gameInProgress = true;
            writer.println("GAME STARTED");
        }

        private void sendRules(PrintWriter writer) {
            writer.println("SEND RULES");
            // Add rules text here
        }

        private void sendHelp(PrintWriter writer) {
            writer.println("COMMANDS");
            // Add commands text here
        }

        private void handleTry(PrintWriter writer, char[] proposition) {
            // Check length
            if (proposition.length != 4) {
                writer.println("ERROR 418");
                return;
            }

            // Check if all characters are valid symbols
            String validSymbolsRegex = "[RBGY]";
            for (char c : proposition) {
                if (!String.valueOf(c).matches(validSymbolsRegex)) {
                    writer.println("ERROR 418");
                    return;
                }
            }

            // Convert char array to String
            String propositionStr = new String(proposition);

            // Use the string for further operations
            int[] clues = game.getHint(proposition);
            if (game.isCorrect(proposition)) {
                writer.println("FINISHED WON");
                gameInProgress = false;
            } else {
                writer.println("ANSWER " + clues[0] + " " + clues[1]);
            }
        }
    }}
