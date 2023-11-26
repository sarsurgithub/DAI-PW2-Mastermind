package heig.vd.dai.client;

import picocli.CommandLine;

import java.io.*;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;

@CommandLine.Command(name = "client", version = "1.0", mixinStandardHelpOptions = true)


public class Client implements Runnable {

    @CommandLine.Parameters(paramLabel = "<port>", defaultValue = "4444",
            description = "Port (default: ${DEFAULT-VALUE})")
    private int port;

    @CommandLine.Parameters(paramLabel = "<host>", defaultValue = "localhost",
            description = "Host (default: ${DEFAULT-VALUE})")
    private String host;

    Scanner scanner = new Scanner(System.in);
    @Override
    public void run() {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ) {

            String fromServer;
            String fromUser;

            System.out.println("Connected to server. Type 'START' to begin a game, 'RULES' for rules, 'HELP' for commands, 'QUIT' to quit.");

            while ((fromServer = in.readLine()) != null) {
                System.out.println("Server: " + fromServer);

                if (fromServer.startsWith("ANSWER")) {
                    processAnswer(fromServer);
                } else {
                    switch (fromServer) {
                        case "FINISHED WON":
                            System.out.println("You won! Type 'START' to play again or 'QUIT' to exit.");
                            break;
                        case "FINISHED LOST":
                            System.out.println("Game Over, you lost. Type 'START' to play again or 'QUIT' to exit.");
                            break;
                    }
                }
                System.out.print("Enter command: ");
                fromUser = scanner.nextLine();

                if ("QUIT".equalsIgnoreCase(fromUser)) {
                    out.println(fromUser);
                    break;
                }

                String processedInput = processInput(fromUser);
                if (!processedInput.isEmpty()) {
                    out.println(processedInput);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String processInput(String input) {
        // Process the user input here based on the expected command format
        // Return the processed/validated input, or an empty string if invalid
        input = input.toUpperCase();
        //TRY command processing
        if (input.startsWith("TRY")) {
            String[] parts = input.split(" ");
            if (parts.length == 2 && parts[1].matches("[RBGY]{4}")) {
                return input;
            } else {
                System.out.println("Invalid TRY command format. Example: 'TRY RRGB'");
                return "";
            }
        }

        // START command processing
        if (input.equalsIgnoreCase("START")) {
            System.out.println("Do you want to play a default[1] or custom[2] game?");
            try {
                int type = readIntFromUser(scanner);
                switch (type) {
                    case 1:
                        return input;
                    case 2:
                        System.out.println("How many tries do you want?");
                        int tries = readIntFromUser(scanner);
                        System.out.println("How many colors do you want?");
                        int colors = readIntFromUser(scanner);
                        return input + " " + tries + " " + colors;
                    default:
                        System.out.println("This was not a valid answer, please start again.");
                        return "";
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Clear the invalid input
                return "";
            }
        }

        return input.toUpperCase(); // Default processing
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


    private static void processAnswer(String answer) {
        String[] parts = answer.split(" ");
        if (parts.length == 3) {
            try {
                int clue1 = Integer.parseInt(parts[1]);
                int clue2 = Integer.parseInt(parts[2]);
                // Process the clues. For example, display them to the user.
                System.out.println("Number of colors correctly placed: " + clue1 + "\n" +
                        "Numbers of colors present in the answer: " + clue2);
            } catch (NumberFormatException e) {
                System.out.println("Error processing clues: " + e.getMessage());
            }
        } else {
            System.out.println("Invalid answer format received from server.");
        }
    }

}
