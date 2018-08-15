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

/**
 * This abstract class implements a random number generator.
 */
public abstract class RandomNumberGenerator {
    /**
     * Resets the generator to its initial state.
     */
    public abstract void reset();

    /**
     * Returns the name of the generator.
     * @return name of the generator
     */
    public abstract String getName();

    /**
     * Returns human readable names of all parameters.
     * @return string array of parameter names
     */
    public abstract String[] getParameterNames();

    /**
     * Returns all parameters of the generator.
     * @return all parameters of the generator
     */
    public abstract long[] getParameters();

    /**
     * Returns the following predictions without updating the state of the generator.
     * @param number number of values to predict
     * @return predicted values
     * @throws IllegalArgumentException if number is less than zero
     */
    public abstract long[] peekNext(int number) throws IllegalArgumentException;

    /**
     * Find prediction numbers that match the input series and update the state accordingly.
     * @param incomingNumbers new input numbers
     * @param historyBuffer previous input numbers
     * @return predicted numbers that best match input series
     */
    public abstract long[] findSeries(long[] incomingNumbers, HistoryBuffer historyBuffer);

    /**
     * Generates the next prediction and updates the state accordingly.
     * @return next prediction
     */
    public abstract long next();

    /**
     * Generates the following predictions and updates the state accordingly.
     * @param number number of values to predict
     * @return predicted values
     * @throws IllegalArgumentException if number is less than zero
     */
    public long[] next(int number) throws IllegalArgumentException {
        if (number < 0) {
            throw new IllegalArgumentException();
        }
        long[] predictions = new long[number];
        for (int i = 0; i < number; i++) {
            predictions[i] = next();
        }
        return predictions;
    }
}
