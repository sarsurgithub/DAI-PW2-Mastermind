package heig.vd.dai.client;

import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

@CommandLine.Command(name = "client", version = "1.0", mixinStandardHelpOptions = true)


public class Client implements Runnable {

    @CommandLine.Parameters(paramLabel = "<port>", defaultValue = "4444",
            description = "Port (default: ${DEFAULT-VALUE})")
    private int port;

    @CommandLine.Parameters(paramLabel = "<host>", defaultValue = "localhost",
            description = "Host (default: ${DEFAULT-VALUE})")
    private String host;

    private final Scanner scanner = new Scanner(System.in);
    private boolean gameInProgress = false;
    private int nbPins = 4;

    @Override
    public void run() {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {

            String fromServer;
            String fromUser;

            System.out.println("Connected to server. Type 'START' to begin a game, 'RULES' for rules, 'HELP' for commands, 'QUIT' to quit.");

            while (true) {
                try {
                    fromServer = in.readLine();
                } catch (IOException e) {
                    System.out.println("Server disconnected.");
                    break;
                }

                if (fromServer == null){
                    System.out.println("Server disconnected.");
                    break;
                }

                String[] serverParts = fromServer.split(" ");
                processServerMessage(serverParts);

                boolean correctInput = false;

                do {
                    System.out.print("Enter command: ");
                    fromUser = scanner.nextLine().toUpperCase();
                    String[] userParts = fromUser.split(" ");

                    switch (userParts[0]) {
                        case "START":
                            fromUser = processStartCommand(userParts);
                            if (fromUser != null) {
                                correctInput = true;
                            }
                            break;
                        case "TRY":
                            if (gameInProgress) {
                                correctInput = processTryInput(userParts);
                            } else {
                                System.out.println("No game in progress. Type 'START' to start a new game.");
                            }
                            break;
                        case "RULES", "HELP", "QUIT":
                            if (userParts.length == 1) {
                                correctInput = true;
                            } else {
                                System.out.println("Invalid command. Type 'HELP' for a list of commands.");
                            }
                            break;
                        default:
                            System.out.println("Invalid command. Type 'HELP' for a list of commands.");
                    }
                } while (!correctInput);

                if (!fromUser.isEmpty()) {
                    out.println(fromUser);
                }
            }

        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
        }
    }

    private void processServerMessage(String[] serverParts) {
        switch (serverParts[0]) {
            case "OK":
                break;
            case "ANSWER":
                processAnswer(serverParts);
                break;
            case "STARTED":
                System.out.println("Game started. Type 'TRY' to make a guess or 'QUIT' to exit.");
                gameInProgress = true;
                break;
            case "SEND":
                if (serverParts.length > 1) {
                    String s = String.join(" ", serverParts);
                    System.out.println(s);
                } else {
                    System.out.println("Invalid SEND from server");
                }
                break;
            case "FINISHED":
                switch (serverParts[1]) {
                    case "WON":
                        System.out.println("You won! Type 'START' to play again or 'QUIT' to exit.");
                        gameInProgress = false;
                        break;
                    case "LOST":
                        System.out.println("Game Over, you lost. Type 'START' to play again or 'QUIT' to exit.");
                        gameInProgress = false;
                        break;
                    default:
                        System.out.println("Invalid game result received from server.");
                }
                break;
            case "ERROR":
                System.out.println("Error received from server: " + serverParts[1]);
                break;
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
                    this.nbPins = readIntFromUser(scanner);
                    return userParts[0] + " " + tries + " " + nbPins;
                default:
                    System.out.println("This was not a valid answer, please start again.");
                    return null;
            }
        }
        System.out.println("Invalid START command format. It should be 'START'");
        return null;
    }

    private boolean processTryInput(String[] parts) {
        //TRY command processing has to be length this.pins
        if (parts.length == 2 && parts[1].length() == this.nbPins && parts[1].matches("[RGBY]+")) {
            return true;
        }
        System.out.println("Invalid TRY command format. It should be " + this.nbPins + " characters long and only contain the letters RGBY.");
        return false;

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
