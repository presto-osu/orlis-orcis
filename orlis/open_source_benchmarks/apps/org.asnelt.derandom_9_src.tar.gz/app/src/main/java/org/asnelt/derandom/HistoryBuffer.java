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

import java.nio.BufferUnderflowException;

/**
 * This class implements a ring buffer for storing long numbers.
 */
public class HistoryBuffer {
    /** The maximum number of elements in the buffer. */
    private int capacity;
    /** The array for storing the elements. */
    private long[] numbers;
    /** Index of the first element. */
    private int head;
    /** Index of the last element. */
    private int tail;

    /**
     * Constructs an empty buffer with a given capacity.
     * @param capacity the maximum number of elements the buffer can hold
     */
    public HistoryBuffer(int capacity) {
        clear();
        setCapacity(capacity);
    }

    /**
     * Removes all elements from the buffer.
     */
    public void clear() {
        head = 0;
        tail = -1;
        numbers = new long[0];
    }

    /**
     * Sets the maximum number of elements the buffer can hold. This number must be non-negative.
     * @param capacity the new capacity
     * @throws IllegalArgumentException if the capacity is less than zero
     */
    public void setCapacity(int capacity) throws IllegalArgumentException {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity must not be negative");
        }
        if (this.capacity != capacity) {
            if (capacity < numbers.length) {
                // Shrink numbers
                if (length() >= capacity) {
                    numbers = getLast(capacity);
                    head = 0;
                    tail = capacity - 1;
                } else {
                    rebuildNumbers(capacity);
                }
            }
            this.capacity = capacity;
        }
    }

    /**
     * Puts new elements into the buffer.
     * @param incomingNumbers the numbers to store
     */
    public void put(long[] incomingNumbers) {
        if (incomingNumbers.length == 0) {
            return;
        }
        int currentLength = length();
        if (currentLength + incomingNumbers.length > numbers.length && numbers.length < capacity) {
            grow(incomingNumbers.length);
        }
        if (incomingNumbers.length <= numbers.length) {
            // Incoming numbers fit into buffer
            int endLength = numbers.length - tail - 1;
            if (endLength > incomingNumbers.length) {
                endLength = incomingNumbers.length;
            }
            int startLength = incomingNumbers.length - endLength;
            if (endLength > 0) {
                System.arraycopy(incomingNumbers, 0, numbers, tail+1, endLength);
            }
            if (startLength > 0) {
                System.arraycopy(incomingNumbers, endLength, numbers, 0, startLength);
            }
            if (tail > -1 && (head > tail && head <= tail + endLength || head < startLength)) {
                head = (tail + incomingNumbers.length + 1) % numbers.length;
            }
            tail = (tail + incomingNumbers.length) % numbers.length;
        } else {
            // Incoming numbers do not fit into buffer
            System.arraycopy(incomingNumbers, incomingNumbers.length-numbers.length, numbers, 0,
                    numbers.length);
            head = 0;
            tail = numbers.length-1;
        }
    }

    /**
     * Returns the element that was last put into the buffer.
     * @return the number that was last put into the buffer
     * @throws BufferUnderflowException if the buffer is empty
     */
    public long getLast() throws BufferUnderflowException {
        if (tail < 0) {
            // Empty buffer
            throw new BufferUnderflowException();
        }
        return numbers[tail];
    }

    /**
     * Returns the elements that were last put into the buffer.
     * @param range the number of elements to return
     * @return the numbers that were last put into the buffer
     * @throws BufferUnderflowException if range is greater than the number of buffer elements
     */
    public long[] getLast(int range) throws BufferUnderflowException {
        if (range > length()) {
            throw new BufferUnderflowException();
        }
        long[] rangeNumbers = new long[range];
        if (range > 0) {
            if (tail+1 >= range) {
                System.arraycopy(numbers, tail-range+1, rangeNumbers, 0, range);
            } else {
                System.arraycopy(numbers, numbers.length-(range-tail-1), rangeNumbers, 0,
                        range-tail-1);
                System.arraycopy(numbers, 0, rangeNumbers, range-tail-1, tail+1);
            }
        }
        return rangeNumbers;
    }

    /**
     * Returns all elements that are stored in the buffer
     * @return all elements in the buffer
     */
    public long[] toArray() {
        return getLast(length());
    }

    /**
     * Returns the number of elements the buffer currently stores.
     * @return the number of elements in the buffer
     */
    public int length() {
        if (tail < 0) {
            // Empty buffer
            return 0;
        }
        if (tail >= head) {
            return tail-head+1;
        }
        // tail < head
        return numbers.length-(head-tail-1);
    }

    /**
     * Increases the length of the internal array to make space for more elements.
     * @param size the number of additional elements that need to be stored
     */
    private void grow(int size) {
        int growLength = numbers.length;
        if (growLength <= 0) {
            growLength = 1;
        }
        // Double growLength until we can fit size additional elements
        while (growLength < numbers.length + size && growLength > 0) {
            growLength *= 2;
        }
        if (growLength > capacity || growLength < 0) {
            growLength = capacity;
        }
        try {
            rebuildNumbers(growLength);
        } catch (OutOfMemoryError e) {
            // Abort grows
        }
    }

    /**
     * Allocates a new internal array and copies all elements from the old internal array into the
     * new one.
     * @param newLength the new length of the internal array
     */
    private void rebuildNumbers(int newLength) {
        long[] newNumbers = new long[newLength];
        if (tail >= 0) {
            if (tail >= head) {
                System.arraycopy(numbers, head, newNumbers, 0, tail-head+1);
                tail = tail-head;
            } else {
                System.arraycopy(numbers, head, newNumbers, 0, numbers.length-head);
                System.arraycopy(numbers, 0, newNumbers, numbers.length-head, tail+1);
                tail = numbers.length-(head-tail);
            }
            head = 0;
        }
        numbers = newNumbers;
    }
}
