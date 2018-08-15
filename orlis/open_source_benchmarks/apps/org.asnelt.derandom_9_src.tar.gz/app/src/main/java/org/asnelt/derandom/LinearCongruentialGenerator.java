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

/**
 * This class implements a linear congruential random number generator.
 */
public class LinearCongruentialGenerator extends RandomNumberGenerator {
    /** Human readable name of the generator. */
    private final String name;
    /** Multiplier parameter. */
    private final long multiplier;
    /** Human readable name of multiplier parameter. */
    private static final String MULTIPLIER_NAME = "Multiplier";
    /** Increment parameter. */
    private final long increment;
    /** Human readable name of increment parameter. */
    private static final String INCREMENT_NAME = "Increment";
    /** Modulus parameter. */
    private final long modulus;
    /** Human readable name of modulus parameter. */
    private static final String MODULUS_NAME = "Modulus";
    /** Index of start bit for output. */
    private final int bitRangeStart;
    /** Human readable name of bit range start parameter. */
    private static final String BIT_RANGE_START_NAME = "Bit range start";
    /** Index of stop bit for output. */
    private final int bitRangeStop;
    /** Human readable name of bit range stop parameter. */
    private static final String BIT_RANGE_STOP_NAME = "Bit range stop";
    /** Human readable name of state. */
    private static final String STATE_NAME = "State";
    /** Human readable names of all free parameters. */
    private static final String[] PARAMETER_NAMES = {
            MULTIPLIER_NAME, INCREMENT_NAME, MODULUS_NAME, BIT_RANGE_START_NAME,
            BIT_RANGE_STOP_NAME, STATE_NAME
    };
    /** The parameter names as a list. */
    private static final List PARAMETER_NAMES_LIST = Arrays.asList(PARAMETER_NAMES);
    /** Two complement bit extension for negative integers. */
    private static final long COMPLEMENT_INTEGER_EXTENSION = 0xFFFFFFFF00000000L;
    /** Internal state. */
    private volatile long state;
    /** Index of most significant modulus bit. */
    private final int modulusBitRangeStop;
    /** Initial seed of the generator. */
    private final long initialSeed;
    /** Bit mask based on bit range. */
    private volatile long mask;

    /**
     * Constructor initializing all parameters.
     * @param name name of the generator
     * @param multiplier multiplier parameter of the generator
     * @param increment increment parameter of the generator
     * @param modulus modulus parameter of the generator
     * @param seed initial seed of the generator
     * @param bitRangeStart start index of output bits
     * @param bitRangeStop stop index of output bits
     */
    public LinearCongruentialGenerator(String name, long multiplier, long increment, long modulus,
                                       long seed, int bitRangeStart, int bitRangeStop) {
        this.name = name;
        this.multiplier = multiplier;
        this.increment = increment;
        if (modulus == 0L) {
            throw new IllegalArgumentException("modulus must not be zero");
        }
        this.modulus = modulus;
        modulusBitRangeStop = Long.SIZE - Long.numberOfLeadingZeros(modulus) - 1;
        this.initialSeed = seed;
        this.state = seed;
        // Check index range
        if (bitRangeStart < 0) {
            throw new IllegalArgumentException("bitRangeStart must not be negative");
        }
        if (bitRangeStop > Long.SIZE - 1) {
            throw new IllegalArgumentException(
                    "bitRangeStop must not exceed number of long bit indices");
        }
        if (bitRangeStart > bitRangeStop) {
            throw new IllegalArgumentException(
                    "bitRangeStart must not be greater than bitRangeStop");
        }
        this.bitRangeStart = bitRangeStart;
        this.bitRangeStop = bitRangeStop;
        // Construct bit mask
        mask = 0L;
        for (int i = bitRangeStart; i <= bitRangeStop; i++) {
            // Set bit i
            mask |= (1L << i);
        }
    }

    /**
     * Resets the generator to its initial seed.
     */
    @Override
    public void reset() {
        setState(initialSeed);
    }

    /**
     * Sets the state of the generator.
     * @param state the complete state of the generator
     */
    public synchronized void setState(long state) {
        this.state = state;
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
        return PARAMETER_NAMES;
    }

    /**
     * Returns all parameters of the generator.
     * @return all parameters of the generator
     */
    @Override
    public long[] getParameters() {
        long[] parameters = new long[PARAMETER_NAMES_LIST.size()];
        parameters[PARAMETER_NAMES_LIST.indexOf(MULTIPLIER_NAME)] = multiplier;
        parameters[PARAMETER_NAMES_LIST.indexOf(INCREMENT_NAME)] = increment;
        parameters[PARAMETER_NAMES_LIST.indexOf(MODULUS_NAME)] = modulus;
        parameters[PARAMETER_NAMES_LIST.indexOf(BIT_RANGE_START_NAME)] = (long) bitRangeStart;
        parameters[PARAMETER_NAMES_LIST.indexOf(BIT_RANGE_STOP_NAME)] = (long) bitRangeStop;
        parameters[PARAMETER_NAMES_LIST.indexOf(STATE_NAME)] = state;
        return parameters;
    }

    /**
     * Returns the following predictions without updating the state of the generator.
     * @param number numbers of values to predict
     * @return predicted values
     * @throws IllegalArgumentException if number is less than zero
     */
    @Override
    public long[] peekNext(int number) throws IllegalArgumentException {
        if (number < 0) {
            throw new IllegalArgumentException();
        }
        long[] randomNumbers = new long[number];
        long peekState = state;
        for (int i = 0; i < number; i++) {
            peekState = nextState(peekState);
            // Set output bits
            randomNumbers[i] = calculateOutput(peekState);
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
        long[] predicted = new long[incomingNumbers.length];
        if (incomingNumbers.length == 0) {
            // Empty input
            return predicted;
        }
        // Make prediction based on current state
        predicted[0] = next();
        if (predicted[0] != incomingNumbers[0]) {
            if (historyBuffer == null || historyBuffer.length() == 0) {
                // No history present; just guess incoming number as new state
                setState(incomingNumbers[0]);
            } else {
                // We have a pair to work with
                setState(findState(historyBuffer.getLast(), incomingNumbers[0]));
            }
        }
        for (int i = 1; i < incomingNumbers.length; i++) {
            predicted[i] = next();
            if (predicted[i] != incomingNumbers[i]) {
                setState(findState(incomingNumbers[i-1], incomingNumbers[i]));
            }
        }
        return predicted;
    }

    /**
     * Generates the next prediction and updates the state accordingly.
     * @return next prediction
     */
    @Override
    public synchronized long next() {
        state = nextState(state);
        return calculateOutput(state);
    }

    /**
     * Calculate the state of the generator based on two consecutive values.
     * @param number one output of the generator
     * @param successor next output of the generator
     * @return the state of the generator after the successor value
     */
    private long findState(long number, long successor) {
        // Undo output shift
        number <<= bitRangeStart;
        // Number of leading bits that are hidden
        int leadingBits = modulusBitRangeStop - bitRangeStop;
        if (leadingBits < 0) {
            leadingBits = 0;
        }
        // Try all possible states
        for (long j = 0; j < (1 << leadingBits); j++) {
            long leadingState = (j << (bitRangeStop + 1)) | number;
            for (long i = 0; i < (1 << bitRangeStart); i++) {
                long state = leadingState | i;
                state = nextState(state);
                if (calculateOutput(state) == successor) {
                    return state;
                }
            }
        }
        // No option found, so just return successor as state
        return successor;
    }

    /**
     * Calculates the output of the generator based on the state.
     * @param state the state for calculating the output
     * @return the output of the generator
     */
    private long calculateOutput(long state) {
        long output = (state & mask) >> bitRangeStart;
        // For integers add two complement bit extension for negative numbers
        if (bitRangeStop - bitRangeStart + 1 == Integer.SIZE && output >> Integer.SIZE-1 > 0) {
            output |= COMPLEMENT_INTEGER_EXTENSION;
        }
        return output;
    }

    /**
     * Calculates the next state of the generator based on the argument.
     * @param state the base state for calculating the successor state
     * @return the next state of the generator
     */
    private long nextState(long state) {
        return (multiplier * state + increment) % modulus;
    }
}
