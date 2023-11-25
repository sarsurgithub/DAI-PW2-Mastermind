package heig.vd.dai.server;

import java.util.HashMap;
import java.util.Map;

public class MastermindGame {
    private static final int NB_COLORS = 4;
    private static final char[] COLORS = {'R', 'B', 'G', 'Y'};
    private int nbTry = 8;
    private int turn = 1;
    private int nbPins = 4;
    private final char[] secretCode = new char[nbPins];

    public MastermindGame() {
        generateSecretCode();
    }

    public MastermindGame(int nbTry, int nbPins) {
        this.nbTry = nbTry;
        this.nbPins = nbPins;
        generateSecretCode();
    }

    private void generateSecretCode() {
        for (int i = 0; i < nbPins; i++) {
            secretCode[i] = COLORS[(int) (Math.random() * NB_COLORS) % NB_COLORS];
        }
    }

    public int getNbTry() {
        return nbTry;
    }

    public int getTurn() {
        return turn;
    }

    public char[] getSecretCode() {
        return secretCode;
    }

    public boolean isCorrect(char[] code) {
        if (nbTry == 0) {
            return false;
        }
        for (int i = 0; i < nbPins; i++) {
            if (code[i] != secretCode[i]) {
                return false;
            }
        }
        return true;
    }


    public int[] getHint(char[] code) {
        if (turn > nbTry) {
            return null;
        }
        int[] hint = new int[2]; // hint[0] = right color right place , hint[1] = right color wrong place not counted in hint[0]


        // Count occurrences of each digit in secretCode
        Map<Character, Integer> counts = new HashMap<>();
        for (char c : secretCode) {
            counts.put(c, counts.getOrDefault(c, 0) + 1);
        }

        // Check for right color and right place
        for (int i = 0; i < secretCode.length; i++) {
            if (code[i] == secretCode[i]) {
                hint[0]++;
                counts.put(secretCode[i], counts.get(secretCode[i]) - 1);
            }
        }

        // Check for right color but wrong place
        for (int i = 0; i < secretCode.length; i++) {
            if (code[i] != secretCode[i] && counts.containsKey(code[i]) && counts.get(code[i]) > 0) {
                hint[1]++;
                counts.put(code[i], counts.get(code[i]) - 1);
            }
        }
        turn++;
        return hint;
    }
}
