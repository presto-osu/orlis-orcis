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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * This class implements a Mersenne Twister random number generator.
 */
public class MersenneTwister extends RandomNumberGenerator {
    /** Human readable name of the generator. */
    private final String name;
    /** Word size of the generator. */
    private final int wordSize;
    /** Human readable name of word size parameter. */
    private static final String WORD_SIZE_NAME = "Word size";
    /** Human readable name of state size parameter. */
    private static final String STATE_SIZE_NAME = "State size";
    /** The shift size parameter. */
    private final int shiftSize;
    /** Human readable name of shift size parameter. */
    private static final String SHIFT_SIZE_NAME = "Shift size";
    /** The number of bits in the lower mask of the state twist transformation. */
    private final int maskBits;
    /** Human readable name of mask bits parameter. */
    private static final String MASK_BITS_NAME = "Mask bits";
    /** Bit mask for the state twist transformation. */
    private final long twistMask;
    /** Human readable name of twist mask parameter. */
    private static final String TWIST_MASK_NAME = "Twist mask";
    /** The u parameter of the tempering transformation. */
    private final int temperingU;
    /** Human readable name of tempering u parameter. */
    private static final String TEMPERING_U_NAME = "Tempering u";
    /** The d parameter of the tempering transformation. */
    private final long temperingD;
    /** Human readable name of tempering d parameter. */
    private static final String TEMPERING_D_NAME = "Tempering d";
    /** The s parameter of the tempering transformation. */
    private final int temperingS;
    /** Human readable name of tempering s parameter. */
    private static final String TEMPERING_S_NAME = "Tempering s";
    /** The b parameter of the tempering transformation. */
    private final long temperingB;
    /** Human readable name of tempering b parameter. */
    private static final String TEMPERING_B_NAME = "Tempering b";
    /** The t parameter of the tempering transformation. */
    private final int temperingT;
    /** Human readable name of tempering t parameter. */
    private static final String TEMPERING_T_NAME = "Tempering t";
    /** The c parameter of the tempering transformation. */
    private final long temperingC;
    /** Human readable name of tempering c parameter. */
    private static final String TEMPERING_C_NAME = "Tempering c";
    /** The l parameter of the tempering transformation. */
    private final int temperingL;
    /** Human readable name of tempering l parameter. */
    private static final String TEMPERING_L_NAME = "Tempering l";
    /** The multiplier parameter of the state initialization. */
    private final long initializationMultiplier;
    /** Human readable name of initialization multiplier parameter. */
    private static final String INITIALIZATION_MULTIPLIER_NAME = "Initialization multiplier";
    /** Human readable names of all free parameters. */
    private static final String[] PARAMETER_NAMES = {
            WORD_SIZE_NAME, STATE_SIZE_NAME, SHIFT_SIZE_NAME, MASK_BITS_NAME, TWIST_MASK_NAME,
            TEMPERING_U_NAME, TEMPERING_D_NAME, TEMPERING_S_NAME, TEMPERING_B_NAME,
            TEMPERING_T_NAME, TEMPERING_C_NAME, TEMPERING_L_NAME, INITIALIZATION_MULTIPLIER_NAME
    };
    /** The parameter names as a list. */
    private static final List PARAMETER_NAMES_LIST = Arrays.asList(PARAMETER_NAMES);
    /** Human readable name of index. */
    private static final String INDEX_NAME = "State index";
    /** Current state element index. */
    private volatile int index;
    /** Human readable name of state. */
    private static final String STATE_NAME = "State";
    /** Internal state. */
    private volatile AtomicLongArray state;
    /** Initial seed of the generator. */
    private final long initialSeed;
    /** Helper mask for selecting the word bits. */
    private final long wordMask;
    /** Lower mask for state twist transformation. */
    private final long lowerMask;
    /** Upper mask for state twist transformation. */
    private final long upperMask;
    /** Two complement bit extension for negative integers. */
    private static final long COMPLEMENT_INTEGER_EXTENSION = 0xFFFFFFFF00000000L;

    /**
     * Constructor initializing all parameters.
     * @param name name of the generator
     * @param wordSize word size of the generator
     * @param stateSize the number of state elements
     * @param shiftSize the shift size parameter
     * @param maskBits the number of bits in the lower mask of the state twist transformation
     * @param twistMask bit mask for the state twist transformation
     * @param temperingU the u parameter of the tempering transformation
     * @param temperingD the d parameter of the tempering transformation
     * @param temperingS the s parameter of the tempering transformation
     * @param temperingB the b parameter of the tempering transformation
     * @param temperingT the t parameter of the tempering transformation
     * @param temperingC the c parameter of the tempering transformation
     * @param temperingL the l parameter of the tempering transformation
     * @param initializationMultiplier the multiplier parameter of the state initialization
     * @param seed initial seed of the generator
     */
    public MersenneTwister(String name, int wordSize, int stateSize, int shiftSize, int maskBits,
                           long twistMask, int temperingU, long temperingD, int temperingS,
                           long temperingB, int temperingT, long temperingC, int temperingL,
                           long initializationMultiplier, long seed) {
        this.name = name;
        this.wordSize = wordSize;
        this.shiftSize = shiftSize;
        this.maskBits = maskBits;
        this.twistMask = twistMask;
        this.temperingU = temperingU;
        this.temperingD = temperingD;
        this.temperingS = temperingS;
        this.temperingB = temperingB;
        this.temperingT = temperingT;
        this.temperingC = temperingC;
        this.temperingL = temperingL;
        this.initializationMultiplier = initializationMultiplier;

        // Check parameters
        if (wordSize < 1 || wordSize > Long.SIZE) {
            throw new IllegalArgumentException(
                    "wordSize must be positive and not exceed size of long");
        }
        if (stateSize < 1) {
            throw new IllegalArgumentException("stateSize must be positive");
        }
        if (shiftSize < 0) {
            throw new IllegalArgumentException("shiftSize must not be negative");
        }
        if (maskBits < 0 || maskBits > Long.SIZE) {
            throw new IllegalArgumentException(
                    "maskBits must not be negative and not exceed size of long");
        }

        // Initialize internal state
        state = new AtomicLongArray(stateSize);
        if (wordSize == Long.SIZE) {
            wordMask = (Long.MAX_VALUE << 1) | 1L;
        } else {
            wordMask = (1L << wordSize) - 1L;
        }
        if (maskBits == Long.SIZE) {
            lowerMask = (Long.MAX_VALUE << 1) | 1L;
        } else {
            lowerMask = (1L << maskBits) - 1L;
        }
        upperMask = (~lowerMask) & wordMask;
        initialize(seed);
        this.initialSeed = seed;
    }

    /**
     * Resets the generator to its initial seed.
     */
    @Override
    public synchronized void reset() {
        initialize(initialSeed);
    }

    /**
     * Returns the name of the generator.
     * @return name of the generator
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns human readable names of all parameters.
     * @return a string array of parameter names
     */
    @Override
    public String[] getParameterNames() {
        String[] names;
        try {
            names = new String[PARAMETER_NAMES.length + 1 + state.length()];
            System.arraycopy(PARAMETER_NAMES, 0, names, 0, PARAMETER_NAMES.length);
            names[PARAMETER_NAMES.length] = INDEX_NAME;
            for (int i = 0; i < state.length(); i++) {
                names[PARAMETER_NAMES.length + 1 + i] = STATE_NAME + " " + Integer.toString(i);
            }
        } catch (OutOfMemoryError e) {
            names = PARAMETER_NAMES;
        }
        return names;
    }

    /**
     * Returns all parameters of the generator.
     * @return all parameters of the generator
     */
    @Override
    public long[] getParameters() {
        long[] parameters;
        try {
            parameters = new long[PARAMETER_NAMES_LIST.size() + 1 + state.length()];
            parameters[PARAMETER_NAMES_LIST.size()] = (long) index;
            for (int i = 0; i < state.length(); i++) {
                parameters[PARAMETER_NAMES_LIST.size() + 1 + i] = state.get(i);
            }
        } catch (OutOfMemoryError e) {
            parameters = new long[PARAMETER_NAMES_LIST.size()];
        }
        parameters[PARAMETER_NAMES_LIST.indexOf(WORD_SIZE_NAME)] = (long) wordSize;
        parameters[PARAMETER_NAMES_LIST.indexOf(STATE_SIZE_NAME)] = (long) state.length();
        parameters[PARAMETER_NAMES_LIST.indexOf(SHIFT_SIZE_NAME)] = (long) shiftSize;
        parameters[PARAMETER_NAMES_LIST.indexOf(MASK_BITS_NAME)] = (long) maskBits;
        parameters[PARAMETER_NAMES_LIST.indexOf(TWIST_MASK_NAME)] = twistMask;
        parameters[PARAMETER_NAMES_LIST.indexOf(TEMPERING_U_NAME)] = (long) temperingU;
        parameters[PARAMETER_NAMES_LIST.indexOf(TEMPERING_D_NAME)] = temperingD;
        parameters[PARAMETER_NAMES_LIST.indexOf(TEMPERING_S_NAME)] = (long) temperingS;
        parameters[PARAMETER_NAMES_LIST.indexOf(TEMPERING_B_NAME)] = temperingB;
        parameters[PARAMETER_NAMES_LIST.indexOf(TEMPERING_T_NAME)] = (long) temperingT;
        parameters[PARAMETER_NAMES_LIST.indexOf(TEMPERING_C_NAME)] = temperingC;
        parameters[PARAMETER_NAMES_LIST.indexOf(TEMPERING_L_NAME)] = (long) temperingL;
        parameters[PARAMETER_NAMES_LIST.indexOf(INITIALIZATION_MULTIPLIER_NAME)] = initializationMultiplier;
        return parameters;
    }

    /**
     * Returns the following predictions without updating the state of the generator.
     * @param number numbers of values to predict
     * @return predicted values
     * @throws IllegalArgumentException if number is less than zero
     */
    @Override
    public synchronized long[] peekNext(int number) throws IllegalArgumentException {
        if (number < 0) {
            throw new IllegalArgumentException();
        }
        long[] randomNumbers = new long[number];
        int peekIndex;
        // First set the numbers for which we do not need to twist the state elements
        for (peekIndex = 0; peekIndex < number && index + peekIndex < state.length(); peekIndex++) {
            randomNumbers[peekIndex] = emitState(index+peekIndex);
        }
        if (peekIndex < number) {
            // Backup state
            int nextTwistSize = number - peekIndex;
            if (nextTwistSize > state.length()) {
                nextTwistSize = state.length();
            }
            long[] stateBackup = new long[nextTwistSize];
            for (int i = 0; i < stateBackup.length; i++) {
                stateBackup[i] = state.get(i);
            }
            do {
                nextTwistSize = number - peekIndex;
                if (nextTwistSize > state.length()) {
                    nextTwistSize = state.length();
                }
                twistState(nextTwistSize);
                for (int i = 0; i < nextTwistSize; i++) {
                    randomNumbers[peekIndex+i] = emitState(i);
                }
                peekIndex += nextTwistSize;
            } while (peekIndex < number);
            // Recover state
            for (int i = 0; i < stateBackup.length; i++) {
                state.set(i, stateBackup[i]);
            }
        }
        return randomNumbers;
    }

    /**
     * Find prediction numbers that match the input series and update the state accordingly.
     * @param incomingNumbers new input numbers
     * @param historyBuffer previous input numbers
     * @return predicted numbers that best match input series
     */
    @Override
    public synchronized long[] findSeries(long[] incomingNumbers, HistoryBuffer historyBuffer) {
        // Make prediction based on current state
        long[] predicted = peekNext(incomingNumbers.length);
        for (long number : incomingNumbers) {
            if (index >= state.length()) {
                twistState(state.length());
                index = 0;
            }
            state.set(index, reverseTemper(number & wordMask));
            index++;
        }
        return predicted;
    }

    /**
     * Generates the next prediction and updates the state accordingly.
     * @return next prediction
     */
    @Override
    public synchronized long next() {
        if (index >= state.length()) {
            twistState(state.length());
            index = 0;
        }
        return emitState(index++);
    }

    /**
     * Initializes the state elements from a seed value..
     * @param seed the seed value for initialization of the state
     */
    private void initialize(long seed) {
        index = state.length();
        state.set(0, seed);
        for (int i = 1; i < state.length(); i++) {
            state.set(i, (initializationMultiplier * (state.get(i-1) ^ (state.get(i-1)
                    >>> (wordSize -2))) + i) & wordMask);
        }
    }

    /**
     * Transforms the state elements by means of a linear transformation.
     * @param twistSize the number of state elements to transform
     */
    private void twistState(int twistSize) {
        for (int i = 0; i < twistSize; i++) {
            long mixedState = (state.get(i) & upperMask) + (state.get((i+1) % state.length())
                    & lowerMask);
            long stateMask = mixedState >>> 1;
            if (mixedState % 2 != 0) {
                stateMask ^= twistMask;
            }
            state.set(i, (state.get((i + shiftSize) % state.length()) ^ stateMask) & wordMask);
        }
    }

    /**
     * Calculates the output of the generator based on a state element.
     * @param stateIndex the state element index for calculating the output
     * @return the output of the generator
     */
    private long emitState(int stateIndex) {
        long number = temper(state.get(stateIndex));
        // For integers add two complement bit extension for negative numbers
        if (wordSize == Integer.SIZE && number >> Integer.SIZE-1 > 0) {
            number |= COMPLEMENT_INTEGER_EXTENSION;
        }
        return number;
    }

    /**
     * Transforms a number with a tempering transformation.
     * @param number the input number
     * @return the transformed number
     */
    private long temper(long number) {
        number ^= ((number >>> temperingU) & temperingD);
        number ^= ((number << temperingS) & temperingB);
        number ^= ((number << temperingT) & temperingC);
        number ^= (number >>> temperingL);
        return number;
    }

    /**
     * Reverses the tempering transformation.
     * @param number the output of the tempering transformation
     * @return the original input to the tempering transformation
     */
    private long reverseTemper(long number) {
        number = reverseTemperStep(number, temperingL, wordMask, false);
        number = reverseTemperStep(number, temperingT, temperingC, true);
        number = reverseTemperStep(number, temperingS, temperingB, true);
        number = reverseTemperStep(number, temperingU, temperingD, false);
        return number;
    }

    /**
     * Reverses a single tempering step of the tempering transformation.
     * @param number the output of the tempering step
     * @param length the length of the tempering shift
     * @param mask the tempering mask
     * @param left flag for whether the tempering shift was leftwards
     * @return the original input to the tempering step
     */
    private long reverseTemperStep(long number, int length, long mask, boolean left) {
        long shifter = number;
        for (int i = 0; i < wordSize / (length * 2) + 2; i++) {
            if (left) {
                shifter = number ^ ((shifter << length) & mask);
            } else {
                shifter = number ^ ((shifter >>> length) & mask);
            }
        }
        return shifter;
    }
}