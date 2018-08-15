package de.pinyto.exalteddicer.dicing;

public class Dicer {

    public int targetNumber = 7; // Default
    public int poolSize;

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    // For Special-Charms and Artifacts
    public void setTargetNumber(int targetNumber) {
        this.targetNumber = targetNumber;
    }

    public int[] rollDice() {

        int[] dice = new int[poolSize];
        for (int i = 0; i < dice.length; i++) {
            dice[i] = (int) (Math.random() * 10) + 1;
        }
        return dice;
    }

    public int evaluateDamage() {

        int success = 0;
        int ones = 0;

        int[] dice = rollDice();

        for (int i = 0; i < dice.length; i++) {

            if (dice[i] >= targetNumber) {
                success = success + 1;
            } else {

                if (dice[i] == 1) {
                    ones = ones + 1;
                }
            }
        }
        if (success == 0 && ones > 0) {
            return -1;
        }

        return success;
    }

    // Tens are counted twice
    public int evaluatePool() {

        int[] dice = rollDice();
        int success = 0;
        int ones = 0;

        for (int i = 0; i < dice.length; i++) {
            if (dice[i] >= targetNumber && dice[i] != 10) {
                success = success + 1;
            }
            if (dice[i] == 1) {
                ones = ones + 1;
            }
            if (dice[i] == 10) {
                success = success + 2;
            }
        }

        if (success == 0 && ones > 0) {
            return -1;
        }
        return success;
    }
}
