package heig.vd.dai.client;

import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Scanner;

@CommandLine.Command(name = "client", version = "1.0", mixinStandardHelpOptions = true)


public class Client implements Runnable {

    @CommandLine.Option(names = "-p", paramLabel = "<port>", defaultValue = "4444",
            description = "Port (default: ${DEFAULT-VALUE})")
    private int port;

    @CommandLine.Option(names = "-h", paramLabel = "<host>", defaultValue = "localhost",
            description = "Host (default: ${DEFAULT-VALUE})")
    private String host;

    private final Scanner scanner = new Scanner(System.in);


    @Override
    public void run() {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(),StandardCharsets.UTF_8))
        ) {

            String fromServer;
            String fromUser;


            while (true) {
                try {
                    fromServer = in.readLine();
                } catch (IOException e) {
                    System.out.println("Server disconnected.");
                    break;
                }

                if (fromServer == null) {
                    System.out.println("Server disconnected.");
                    break;
                }

                processServerMessage(fromServer, in);

                System.out.print("Enter command: ");
                fromUser = scanner.nextLine().toUpperCase();
                String[] userParts = fromUser.split(" ");
                if (!fromUser.isEmpty()) {
                    if (Objects.equals(userParts[0], "START")) {
                        fromUser = processStartCommand(userParts);
                    }

                    out.println(fromUser);
                }
            }

        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
        }
    }

    private void processServerMessage(String fromServer, BufferedReader in) throws IOException {
        String[] serverParts = fromServer.split(" ");
        switch (serverParts[0]) {
            case "OK":
                System.out.println("Connected to server. Type 'START' to begin a game, 'RULES' for rules, 'HELP' for commands, 'QUIT' to quit.");
                break;
            case "ANSWER":
                processAnswer(serverParts);
                break;
            case "SEND":
                StringBuilder everything = new StringBuilder();
                String line;
                while (!Objects.equals(line = in.readLine(), "EOF")) {
                    everything.append(line).append("\n");
                }
                System.out.println(everything);
                break;
            case "FINISHED":
                switch (serverParts[1]) {
                    case "WON":
                        System.out.println("You won! Type 'START' to play again or 'QUIT' to exit.");
                        break;
                    case "LOST":
                        System.out.println("Game Over, you lost. Type 'START' to play again or 'QUIT' to exit.");
                        break;
                    default:
                        System.out.println("Invalid game result received from server.");
                }
            case "ERROR", "STARTED":
                System.out.println(fromServer);
                break;
            default:
                System.out.println("Invalid message received from server.");
        }
    }

    private String processStartCommand(String[] userParts) {
        if (userParts.length == 1) {
            System.out.println("Do you want to play a default[1] (10 tries & 4 pins) or custom[2] game?");

            int type = readIntFromUser(scanner);
            switch (type) {
                case 1:
                    return userParts[0];
                case 2:
                    System.out.println("How many tries do you want?");
                    int tries = readIntFromUser(scanner);
                    System.out.println("How many pins do you want?");
                    int nbPins = readIntFromUser(scanner);
                    return userParts[0] + " " + tries + " " + nbPins;
                default:
                    System.out.println("This was not a valid answer, please start again.");
                    return null;
            }
        }
        System.out.println("Invalid START command format. It should be 'START'");
        return null;
    }


    private static int readIntFromUser(Scanner scanner) {
        while (true) {
            try {
                String line = scanner.nextLine();
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }


    private static void processAnswer(String[] answerParts) {

        if (answerParts.length == 4) {
            try {
                int clue1 = Integer.parseInt(answerParts[1]);
                int clue2 = Integer.parseInt(answerParts[2]);
                // Process the clues. For example, display them to the user.
                System.out.println("Number of colors correctly placed: " + clue1 + "\n" +
                        "Numbers of colors present in the answer: " + clue2 + "\n" +
                        "Number of tries left: " + answerParts[3]);
            } catch (NumberFormatException e) {
                System.out.println("Error processing clues: " + e.getMessage());
            }
        } else {
            System.out.println("Invalid answer format received from server.");
        }
    }

}
