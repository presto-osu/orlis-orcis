/*
 * Copyright (C) 2015 Arno Onken
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.asnelt.derandom;

import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Manages all random number generators.
 */
public class RandomManager {
    /** Random number generators. */
    private volatile AtomicReferenceArray<RandomNumberGenerator> generators;
    /** Names of all linear congruential generators. */
    private static final String[] LCG_NAMES = {
            "LCG: ANSI C",
            "LCG: Borland C++ lrand()",
            "LCG: Borland C++ rand()",
            "LCG: C99/C11",
            "LCG: glibc",
            "LCG: glibc revised",
            "LCG: Java",
            "LCG: Microsoft Visual Basic",
            "LCG: Microsoft Visual C++",
            "LCG: MINSTD",
            "LCG: MINSTD revised",
            "LCG: Native API",
            "LCG: Numerical Recipes",
            "LCG: RANDU",
            "LCG: RANF",
            "LCG: Sinclair ZX81"
    };
    /** Multipliers of all linear congruential generators. */
    private static final long[] LCG_MULTIPLIERS = {
            1103515245L,
            22695477L,
            22695477L,
            1103515245L,
            69069L,
            1103515245L,
            25214903917L,
            1140671485L,
            214013L,
            16807L,
            48271L,
            2147483629L,
            1664525L,
            65539L,
            44485709377909L,
            75L
    };
    /** Increments of all linear congruential generators. */
    private static final long[] LCG_INCREMENTS = {
            12345L,
            1L,
            1L,
            12345L,
            1L,
            12345L,
            11L,
            12820163L,
            2531011L,
            0L,
            0L,
            2147483587L,
            1013904223L,
            0L,
            0L,
            0L
    };
    /** Moduli of all linear congruential generators. */
    private static final long[] LCG_MODULI = {
            2147483648L,
            4294967296L,
            4294967296L,
            4294967296L,
            4294967296L,
            2147483648L,
            281474976710656L,
            16777216L,
            4294967296L,
            2147483647L,
            2147483647L,
            2147483647L,
            4294967296L,
            2147483648L,
            281474976710656L,
            65537L
    };
    /** Seeds of all linear congruential generators. */
    private static final long[] LCG_SEEDS = {
            0L,
            0L,
            0L,
            0L,
            0L,
            0L,
            0L,
            0L,
            0L,
            0L,
            0L,
            0L,
            0L,
            0L,
            0L,
            0L
    };
    /** Indices of start bits for output of all linear congruential generators. */
    private static final int[] LCG_BIT_RANGE_STARTS = {
            16,
            0,
            16,
            16,
            0,
            0,
            16,
            0,
            16,
            0,
            0,
            0,
            0,
            0,
            0,
            0
    };
    /** Indices of stop bits for output of all linear congruential generators. */
    private static final int[] LCG_BIT_RANGE_STOPS = {
            30,
            30,
            30,
            30,
            31,
            30,
            47,
            23,
            30,
            30,
            30,
            30,
            31,
            30,
            47,
            16
    };
    /** Names of all Mersenne Twisters. */
    private static final String[] MT_NAMES = {
            "MT: MT19937",
            "MT: MT19937-64"
    };
    /** Word sizes of all Mersenne Twisters. */
    private static final int[] MT_WORD_SIZES = {
            32,
            64
    };
    /** Numbers of state elements of all Mersenne Twisters. */
    private static final int[] MT_STATE_SIZES = {
            624,
            312
    };
    /** Shift size parameters of all Mersenne Twisters. */
    private static final int[] MT_SHIFT_SIZES = {
            397,
            156
    };
    /** Numbers of lower mask bits of all Mersenne Twisters. */
    private static final int[] MT_MASK_BITS = {
            31,
            31
    };
    /** Twist masks of all Mersenne Twisters. */
    private static final long[] MT_TWIST_MASKS = {
            0x9908B0DFL,
            0xB5026F5AA96619E9L
    };
    /** Tempering u parameters of all Mersenne Twisters. */
    private static final int[] MT_TEMPERING_US = {
            11,
            29
    };
    /** Tempering d parameters of all Mersenne Twisters. */
    private static final long[] MT_TEMPERING_DS = {
            0xFFFFFFFFL,
            0x5555555555555555L
    };
    /** Tempering s parameters of all Mersenne Twisters. */
    private static final int[] MT_TEMPERING_SS = {
            7,
            17
    };
    /** Tempering b parameters of all Mersenne Twisters. */
    private static final long[] MT_TEMPERING_BS = {
            0x9D2C5680L,
            0x71D67FFFEDA60000L
    };
    /** Tempering t parameters of all Mersenne Twisters. */
    private static final int[] MT_TEMPERING_TS = {
            15,
            37
    };
    /** Tempering c parameters of all Mersenne Twisters. */
    private static final long[] MT_TEMPERING_CS = {
            0xEFC60000L,
            0xFFF7EEE000000000L
    };
    /** Tempering l parameters of all Mersenne Twisters. */
    private static final int[] MT_TEMPERING_LS = {
            18,
            43
    };
    /** Initialization multipliers of all Mersenne Twisters. */
    private static final long[] MT_INITIALIZATION_MULTIPLIERS = {
            1812433253L,
            6364136223846793005L
    };
    /** Default seeds of all Mersenne Twisters. */
    private static final long[] MT_DEFAULT_SEEDS = {
            5489L,
            5489L
    };
    /** Index of currently active generator. */
    private volatile int currentGenerator;
    /** Best prediction for the latest incoming numbers. */
    private volatile long[] incomingPredictionNumbers;

    /**
     * Constructor initializing all random numbers generators.
     */
    public RandomManager() {
        this.generators = new AtomicReferenceArray<>(0);
        initializeLinearCongruentialGenerators();
        initializeMersenneTwisters();
        this.currentGenerator = 0;
        incomingPredictionNumbers = new long[0];
    }

    /**
     * Returns human readable names of all generators.
     * @return all generator names
     */
    public String[] getGeneratorNames() {
        String[] names = new String[generators.length()];

        for (int i = 0; i < generators.length(); i++) {
            names[i] = generators.get(i).getName();
        }

        return names;
    }

    /**
     * Resets the state of the current generator.
     */
    public void resetCurrentGenerator() {
        generators.get(currentGenerator).reset();
    }

    /**
     * Resets the random manager including the states of all generators.
     */
    public void reset() {
        for (int i = 0; i < generators.length(); i++) {
            generators.get(i).reset();
        }
        currentGenerator = 0;
        incomingPredictionNumbers = new long[0];
    }

    /**
     * Sets the currently active generator.
     * @param number index of the currently active generator
     */
    public void setCurrentGenerator(int number) {
        if (number >= 0 && number < generators.length()) {
            currentGenerator = number;
        }
    }

    /**
     * Returns the index of the currently active generator.
     * @return index of the currently active generator
     */
    public int getCurrentGenerator() {
        return currentGenerator;
    }

    /**
     * Returns the name of the currently active generator.
     * @return name of the currently active generator
     */
    public String getCurrentGeneratorName() {
        return generators.get(currentGenerator).getName();
    }

    /**
     * Returns the parameter names of the currently active generator.
     * @return all parameter names of the currently active generator
     */
    public String[] getCurrentParameterNames() {
        return generators.get(currentGenerator).getParameterNames();
    }

    /**
     * Returns all parameter values of the currently active generator.
     * @return parameter values of the currently active generator
     */
    public long[] getCurrentParameters() {
        return generators.get(currentGenerator).getParameters();
    }

    /**
     * Generates a prediction for the currently active generator without updating its state.
     * @param number number of values to predict
     * @return predictions
     */
    public long[] predict(int number) {
        return generators.get(currentGenerator).peekNext(number);
    }

    /**
     * Find prediction numbers of the currently active generator that match the input series and
     * update the state and incomingPredictionNumbers accordingly.
     * @param incomingNumbers new input numbers
     * @param historyBuffer previous input numbers
     */
    public void findCurrentSeries(long[] incomingNumbers, HistoryBuffer historyBuffer) {
        incomingPredictionNumbers =
                generators.get(currentGenerator).findSeries(incomingNumbers, historyBuffer);
    }

    /**
     * Detect best matching random number generator from input numbers, update the state and
     * update incomingPredictionNumbers with the current prediction.
     * @param incomingNumbers new input numbers
     * @param historyBuffer previous input numbers
     * @return index of the best matching generator
     */
    public int detectGenerator(long[] incomingNumbers, HistoryBuffer historyBuffer) {
        // Check whether the current generator predicts the incoming numbers
        long[] prediction = predict(incomingNumbers.length);
        boolean anyFailure = false;
        for (int i = 0; i < prediction.length; i++) {
            if (prediction[i] != incomingNumbers[i]) {
                anyFailure = true;
                break;
            }
        }
        if (!anyFailure) {
            // Keep current generator
            incomingPredictionNumbers = generators.get(currentGenerator).next(
                    incomingNumbers.length);
            return currentGenerator;
        }
        // Evaluate prediction quality for all generators
        int bestScore = 0;
        int bestGenerator = currentGenerator;
        for (int i = 0; i < generators.length(); i++) {
            prediction = generators.get(i).findSeries(incomingNumbers, historyBuffer);
            int score = 0;
            for (int j = 0; j < prediction.length; j++) {
                if (prediction[j] == incomingNumbers[j]) {
                    score++;
                }
            }
            if (score > bestScore) {
                bestScore = score;
                bestGenerator = i;
            }
            if (i == currentGenerator) {
                if (score == bestScore) {
                    // For equal score current generator is the default generator
                    bestGenerator = currentGenerator;
                }
                incomingPredictionNumbers = prediction;
            }
        }
        return bestGenerator;
    }

    /**
     * Returns the best prediction for the latest incoming numbers.
     * @return prediction for latest incoming numbers
     */
    public long[] getIncomingPredictionNumbers() {
        return incomingPredictionNumbers;
    }

    /**
     * Initializes all linear congruential generators.
     */
    private void initializeLinearCongruentialGenerators() {
        AtomicReferenceArray<RandomNumberGenerator> generators;

        generators = new AtomicReferenceArray<>(this.generators.length() + LCG_NAMES.length);
        // Copy previous generators into new array
        for (int i = 0; i < this.generators.length(); i++) {
            generators.set(i, this.generators.get(i));
        }
        // Construct new generators
        for (int i = 0; i < LCG_NAMES.length; i++) {
            generators.set(this.generators.length() + i, new LinearCongruentialGenerator(
                    LCG_NAMES[i], LCG_MULTIPLIERS[i], LCG_INCREMENTS[i], LCG_MODULI[i],
                    LCG_SEEDS[i], LCG_BIT_RANGE_STARTS[i], LCG_BIT_RANGE_STOPS[i]));
        }

        this.generators = generators;
    }

    /**
     * Initializes all Mersenne Twisters.
     */
    private void initializeMersenneTwisters() {
        AtomicReferenceArray<RandomNumberGenerator> generators;

        generators = new AtomicReferenceArray<>(this.generators.length() + MT_NAMES.length);
        // Copy previous generators into new array
        for (int i = 0; i < this.generators.length(); i++) {
            generators.set(i, this.generators.get(i));
        }
        // Construct new generators
        for (int i = 0; i < MT_NAMES.length; i++) {
                try {
                    generators.set(this.generators.length() + i, new MersenneTwister(
                            MT_NAMES[i], MT_WORD_SIZES[i], MT_STATE_SIZES[i], MT_SHIFT_SIZES[i],
                            MT_MASK_BITS[i], MT_TWIST_MASKS[i], MT_TEMPERING_US[i], MT_TEMPERING_DS[i],
                            MT_TEMPERING_SS[i], MT_TEMPERING_BS[i], MT_TEMPERING_TS[i], MT_TEMPERING_CS[i],
                            MT_TEMPERING_LS[i], MT_INITIALIZATION_MULTIPLIERS[i], MT_DEFAULT_SEEDS[i]));
                } catch (OutOfMemoryError e) {
                    // Not enough memory for Mersenne Twisters
                    return;
                }
        }

        this.generators = generators;
    }
}
