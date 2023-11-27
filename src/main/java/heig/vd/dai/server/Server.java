package heig.vd.dai.server;

import picocli.CommandLine;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@CommandLine.Command(name = "server", version = "1.0", mixinStandardHelpOptions = true)
public class Server implements Runnable{
    @CommandLine.Parameters(paramLabel = "<port>", defaultValue = "4444",
            description = "Port of the server (default: ${DEFAULT-VALUE})")
    private int port;
    private final ExecutorService pool; // Thread pool

    public Server() {
        this.pool = Executors.newFixedThreadPool(3);
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
                 PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true)) {

                printWriter.println("OK");

                String clientMessage;
                while ((clientMessage = reader.readLine()) != null) {
                    switch (clientMessage.split(" ")[0]) {
                        case "START":
                            startGame(printWriter);
                            break;
                        case "RULES":
                            sendText(printWriter, "SEND RULES");
                            break;
                        case "HELP":
                            sendText(printWriter, "COMMANDS");
                            break;
                        case "TRY":
                            if (gameInProgress) {
                                handleTry(printWriter, clientMessage.substring(4).toCharArray());
                            } else {
                                printWriter.println("ERROR 403");
                            }
                            break;
                        case "QUIT":
                            printWriter.println("FINISHED LOST");
                            socket.close();
                            return;
                        default:
                            printWriter.println("ERROR 400 Please send a correct command");
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

        private void sendText(PrintWriter writer, String type) {
            String filepath;
            if (Objects.equals(type, "SEND RULES")){
                filepath = "ressources/rules.txt";
            } else if (Objects.equals(type, "COMMANDS")){
                filepath = "ressources/help.txt";
            } else {
                return;
            }

            writer.println(type);

            File file = new File(filepath);

            if (file.exists()) {
                try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = fileReader.readLine()) != null) {
                        writer.println(line);
                    }
                } catch (IOException e) {
                    writer.println("ERROR: Unable to read file " + type + ".");
                    System.out.println("Error reading file " + type + ": " + e.getMessage());
                }
            } else {
                writer.println("ERROR:" + type + " file not found.");
            }
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
