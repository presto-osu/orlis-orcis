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

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class implements a fragment for doing generator related processing. The fragment is retained
 * across configuration changes.
 */
public class ProcessingFragment extends Fragment {
    /**
     * Interface for listening to processing changes.
     */
    public interface ProcessingFragmentListener {
        /**
         * Called when the history prediction was completely replaced.
         * @param historyNumbers previously entered numbers
         * @param historyPredictionNumbers predictions for previous numbers
         */
        void onHistoryPredictionReplaced(long[] historyNumbers, long[] historyPredictionNumbers);

        /**
         * Called when the random number generator selection changed.
         * @param generatorIndex index of new generator
         */
        void onGeneratorChanged(int generatorIndex);

        /**
         * Called when the input history changed.
         * @param inputNumbers the entered numbers
         * @param predictionNumbers predictions for entered numbers
         */
        void onHistoryChanged(long[] inputNumbers, long[] predictionNumbers);

        /**
         * Called when the predictions for upcoming numbers changed.
         * @param predictionNumbers predictions of upcoming numbers
         */
        void onPredictionChanged(long[] predictionNumbers);

        /**
         * Called when setting the input method to an input file is aborted and sets the input
         * method back to direct input.
         */
        void onFileInputAborted();

        /**
         * Called when setting the input method to an input socket is aborted and sets the input
         * method back to direct input.
         */
        void onSocketInputAborted();

        /**
         * Called when invalid numbers where entered.
         */
        void onInvalidInputNumber();

        /**
         * Called when the input was cleared.
         */
        void onClear();

        /**
         * Called when the progress status changed.
         */
        void onProgressUpdate();

        /**
         * Called when the status of the network socket changed.
         * @param newStatus a description of the new status
         */
        void onSocketStatusChanged(String newStatus);
    }

    /** Random manager for generating predictions. */
    private final RandomManager randomManager;
    /** Handler for updating the user interface. */
    private final Handler handler;
    /** Circular buffer for storing input numbers. */
    private final HistoryBuffer historyBuffer;
    /** Object for synchronizing the main thread and the processing thread. */
    private final Object synchronizationObject;
    /** Executor service for all processing tasks. */
    private final ExecutorService processingExecutor;
    /** Executor service for server task. */
    private final ExecutorService serverExecutor;
    /** Lock for the disconnected condition. */
    private final Lock connectionLock;
    /** Condition that is triggered when the client socket is disconnected. */
    private final Condition disconnected;
    /** Number of numbers to forecast. */
    private volatile int predictionLength;
    /** Flag for whether the generator should be detected automatically. */
    private volatile boolean autoDetect;
    /** Current input file for reading numbers. */
    private volatile Uri inputUri;
    /** Reader for reading input numbers. */
    private volatile BufferedReader inputReader;
    /** Writer for writing predictions to the client socket. */
    private volatile BufferedWriter outputWriter;
    /** Flag for whether a user interface update was missed during a configuration change. */
    private volatile boolean missingUpdate;
    /** Number of process input tasks. */
    private volatile int inputTaskLength;
    /** Flag for whether processing should continue. */
    private volatile boolean processingDesirable;
    /** Server socket port. */
    private volatile int serverPort;
    /** Server socket. */
    private ServerSocket serverSocket;
    /** Client socket. */
    private volatile Socket clientSocket;
    /** Listener for processing changes. */
    private ProcessingFragmentListener listener;
    /** Future for cancelling the server task. */
    private Future<?> serverFuture;
    /** Index of selected input method. */
    private int inputSelection;

    /**
     * Constructor for initializing the processing fragment. Generates a HistoryBuffer, a
     * RandomManager and an ExecutorService.
     */
    public ProcessingFragment() {
        super();
        predictionLength = 0;
        autoDetect = false;
        inputUri = null;
        inputReader = null;
        outputWriter = null;
        missingUpdate = false;
        inputSelection = 0;
        inputTaskLength = 0;
        processingDesirable = true;
        serverPort = 0;
        clientSocket = null;
        synchronizationObject = this;
        historyBuffer = new HistoryBuffer(0);
        randomManager = new RandomManager();
        // Handler for processing user interface updates
        handler = new Handler(Looper.getMainLooper());
        processingExecutor = Executors.newSingleThreadExecutor();
        serverExecutor = Executors.newSingleThreadExecutor();
        connectionLock = new ReentrantLock();
        disconnected = connectionLock.newCondition();
        serverFuture = null;
    }

    /**
     * Initializes this activity. Called only once since the fragment is retained across
     * configuration changes.
     * @param savedInstanceState Bundle with saved state
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain fragment across configuration changes
        setRetainInstance(true);
    }

    /**
     * Called when the fragment is associated with an activity.
     * @param context the context the fragment is associated with
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            listener = (ProcessingFragmentListener) context;
        }
    }

    /**
     * Called before the fragment is no longer associated with an activity.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    /**
     * Sets the currently active generator.
     * @param number index of the currently active generator
     */
    public void setCurrentGenerator(int number) {
        if (number != randomManager.getCurrentGenerator()) {
            prepareInputProcessing();
            processingExecutor.execute(new UpdateAllTask(number));
        }
    }

    /**
     * Returns human readable names of all generators.
     * @return all generator names
     */
    public String[] getGeneratorNames() {
        return randomManager.getGeneratorNames();
    }

    /**
     * Returns the name of the currently active generator.
     * @return name of the currently active generator
     */
    public String getCurrentGeneratorName() {
        return randomManager.getCurrentGeneratorName();
    }

    /**
     * Returns the parameter names of the currently active generator.
     * @return all parameter names of the currently active generator
     */
    public String[] getCurrentParameterNames() {
        return randomManager.getCurrentParameterNames();
    }

    /**
     * Returns all parameter values of the currently active generator.
     * @return parameter values of the currently active generator
     */
    public long[] getCurrentParameters() {
        return randomManager.getCurrentParameters();
    }

    /**
     * Sets the current input selection index.
     * @param inputSelection the new input selection index
     */
    public void setInputSelection(int inputSelection) {
        this.inputSelection = inputSelection;
    }

    /**
     * Returns the current input selection index.
     * @return the current input selection index
     */
    public int getInputSelection() {
        return inputSelection;
    }

    /**
     * Returns the current input URI or null if no input URI is set.
     * @return the current input URI
     */
    public Uri getInputUri() {
        return inputUri;
    }

    /**
     * Sets the input URI to null.
     */
    public void resetInputUri() {
        inputUri = null;
    }

    /**
     * Sets the number of numbers to predict.
     * @param predictionLength the number of numbers to predict
     */
    public void setPredictionLength(int predictionLength) {
        if (this.predictionLength != predictionLength) {
            this.predictionLength = predictionLength;
            updatePrediction();
        }
    }

    /**
     * Sets the flag that determines whether the generator is detected automatically..
     * @param autoDetect automatically detect generator if true
     */
    public void setAutoDetect(boolean autoDetect) {
        this.autoDetect = autoDetect;
    }

    /**
     * Sets the server port. If a server task is running, then it is restarted.
     * @param serverPort the new server port
     */
    public void setServerPort(int serverPort) {
        if (this.serverPort != serverPort) {
            this.serverPort = serverPort;
            if (serverFuture != null) {
                stopServerTask();
                startServerTask();
            }
        }
    }

    /**
     * Determines whether input is currently processed.
     * @return true if input is currently processed
     */
    public boolean isProcessingInput() {
        return inputTaskLength > 0;
    }

    /**
     * Determines whether a user interface update was missed during a configuration change.
     * @return true if an update was missed
     */
    public boolean isMissingUpdate() {
        return missingUpdate;
    }

    /**
     * Executes a clear task.
     */
    public void clear() {
        processingDesirable = false;
        processingExecutor.execute(new ClearTask());
    }

    /**
     * Executes a change capacity task.
     * @param capacity the new input history capacity
     */
    public void setCapacity(int capacity) {
        processingExecutor.execute(new ChangeCapacityTask(capacity));
    }

    /**
     * Executes an update task.
     */
    public void updateAll() {
        prepareInputProcessing();
        processingExecutor.execute(new UpdateAllTask());
    }

    /**
     * Processes an input string of newline separated integers and calculates a prediction.
     * @param input the input string to be processed
     */
    public void processInputString(String input) {
        // Process input in separate thread
        prepareInputProcessing();
        processingExecutor.execute(new ProcessInputTask(input));
    }

    /**
     * Executes a process input task with an input reader.
     */
    public void processInputSocket() {
        // Process input in separate thread
        prepareInputProcessing();
        processingExecutor.execute(new ProcessInputTask());
    }

    /**
     * Opens and processes the input file pointed to by fileUri. Disables direct input.
     * @param fileUri the URI of the file to be processed
     */
    public void processInputFile(Uri fileUri) {
        // Process input in separate thread
        prepareInputProcessing();
        processingExecutor.execute(new ProcessInputTask(fileUri));
    }

    /**
     * Starts the server task and sets the server future.
     */
    public void startServerTask() {
        serverFuture = serverExecutor.submit(new ServerTask());
    }

    /**
     * Stops the server task and sets the server future to null. Closes all sockets.
     */
    public void stopServerTask() {
        if (serverFuture != null) {
            serverFuture.cancel(true);
            serverFuture = null;
        }
        closeSockets();
    }

    /**
     * Closes the client socket and the reader and writer.
     */
    private void closeClient() {
        synchronized (synchronizationObject) {
            if (inputReader != null) {
                try {
                    inputReader.close();
                } catch (IOException e) {
                    // We do not need to do anything more with the reader
                }
            }
            if (outputWriter != null) {
                try {
                    outputWriter.close();
                } catch (IOException e) {
                    // We do not need to do anything more with the writer
                }
            }
            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    // We do not need to do anything more with the client socket
                }
                clientSocket = null;
            }
        }
    }

    /**
     * Closes all sockets.
     */
    private void closeSockets() {
        synchronized (synchronizationObject) {
            closeClient();
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    // We do not need to do anything more with the server socket
                }
                serverSocket = null;
            }
        }
    }

    /**
     * Called when the activity is destroyed.
     */
    @Override
    public void onDestroy() {
        processingDesirable = false;
        // Shutdown server thread
        serverExecutor.shutdownNow();
        // Shutdown processing thread
        processingExecutor.shutdownNow();
        // Close all sockets
        closeSockets();
        super.onDestroy();
    }

    /**
     * Prepares input processing by incrementing the counter of active input tasks and updating the
     * progressing status.
     */
    private void prepareInputProcessing() {
        synchronized (synchronizationObject) {
            inputTaskLength++;
        }
        if (listener != null) {
            listener.onProgressUpdate();
        }
    }

    /**
     * Finishes input processing by decrementing the counter of active input tasks.
     */
    private void finishInputProcessing() {
        synchronized (synchronizationObject) {
            if (inputTaskLength > 0) {
                inputTaskLength--;
            }
        }
    }

    /**
     * Calculates a new prediction and notifies the listener.
     */
    private void updatePrediction() {
        processingExecutor.execute(new UpdatePredictionTask());
    }

    /**
     * This class implements a task for clearing all input.
     */
    private class ClearTask implements Runnable {
        /**
         * Starts executing the code of the task.
         */
        @Override
        public void run() {
            historyBuffer.clear();
            int currentGenerator = randomManager.getCurrentGenerator();
            randomManager.reset();
            randomManager.setCurrentGenerator(currentGenerator);
            boolean posted = handler.post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.onClear();
                        missingUpdate = false;
                    } else {
                        missingUpdate = true;
                    }
                }
            });
            if (!posted) {
                missingUpdate = true;
            }
            processingDesirable = true;
        }
    }

    /**
     * This class implements a task for changing the input history capacity.
     */
    private class ChangeCapacityTask implements Runnable {
        /** The new input capacity. */
        private final int capacity;

        /**
         * Constructor for setting the new input capacity.
         */
        public ChangeCapacityTask(final int capacity) {
            this.capacity = capacity;
        }

        /**
         * Starts executing the code of the task.
         */
        @Override
        public void run() {
            historyBuffer.setCapacity(capacity);
        }
    }

    /**
     * This class implements a task for updating all history predictions and predictions.
     */
    private class UpdateAllTask implements Runnable {
        /** The index of the new generator. */
        private final int generatorIndex;
        /** Flag that determines whether the generator should be changed. */
        private final boolean changeGenerator;

        /**
         * Standard constructor that initializes a task that does not change the generator..
         */
        public UpdateAllTask() {
            this.generatorIndex = 0;
            this.changeGenerator = false;
        }

        /**
         * Constructor that initializes a task that does change the generator.
         * @param generatorIndex index of the new generator
         */
        public UpdateAllTask(final int generatorIndex) {
            this.generatorIndex = generatorIndex;
            this.changeGenerator = true;
        }

        /**
         * Starts executing the code of the task.
         */
        @Override
        public void run() {
            final boolean generatorChanged;
            if (changeGenerator && randomManager.getCurrentGenerator() != generatorIndex) {
                // Process complete history
                randomManager.setCurrentGenerator(generatorIndex);
                generatorChanged = true;
            } else {
                generatorChanged = false;
            }
            final long[] historyNumbers;
            final long[] historyPredictionNumbers;
            final long[] predictionNumbers;
            if ((generatorChanged || !changeGenerator) && historyBuffer.length() > 0) {
                randomManager.resetCurrentGenerator();
                randomManager.findCurrentSeries(historyBuffer.toArray(), null);
                historyNumbers = historyBuffer.toArray();
                historyPredictionNumbers = randomManager.getIncomingPredictionNumbers();
                // Generate new prediction without updating the state
                predictionNumbers = randomManager.predict(predictionLength);
            } else {
                historyNumbers = null;
                historyPredictionNumbers = null;
                predictionNumbers = null;
            }
            finishInputProcessing();
            boolean posted = handler.post(new Runnable() {
                @Override
                public void run() {
                    if (generatorChanged || !changeGenerator) {
                        if (listener != null) {
                            listener.onProgressUpdate();
                            listener.onHistoryPredictionReplaced(historyNumbers,
                                    historyPredictionNumbers);
                            listener.onPredictionChanged(predictionNumbers);
                            missingUpdate = false;
                        } else {
                            missingUpdate = true;
                        }
                    }
                }
            });
            if (!posted) {
                missingUpdate = true;
            }
        }
    }

    /**
     * This class implements a task that processes input numbers.
     */
    private class ProcessInputTask implements Runnable {
        /** Input string of newline separated integers. */
        private final String input;
        /** File input URI. */
        private final Uri fileUri;

        /**
         * Constructor for processing an input string.
         * @param input the input string to be processed
         */
        public ProcessInputTask(final String input) {
            this.input = input;
            this.fileUri = null;
        }

        /**
         * Constructor for processing the input file pointed to by fileUri.
         * @param fileUri the URI of the file to be processed
         */
        public ProcessInputTask(final Uri fileUri) {
            this.input = null;
            this.fileUri = fileUri;
        }

        /**
         * Constructor for processing input from current input reader.
         */
        public ProcessInputTask() {
            this.input = null;
            this.fileUri = null;
        }

        /**
         * Starts executing the code of the task.
         */
        @Override
        public void run() {
            try {
                if (input != null) {
                    processInputString(input);
                } else if (fileUri != null) {
                    processFileInput();
                } else if (inputReader != null) {
                    processSocketInput();
                }
            } catch (NumberFormatException e) {
                // Call listener for showing error message
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onInvalidInputNumber();
                        }
                    }
                });
            }
            finishInputProcessing();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.onProgressUpdate();
                    }
                }
            });
        }

        /**
         * Opens the input reader from the inputUri and initializes processing of the reader.
         */
        private void processFileInput() {
            inputUri = fileUri;
            try {
                InputStream stream = getActivity().getContentResolver().openInputStream(inputUri);
                if (stream == null) {
                    throw new NullPointerException();
                }
                inputReader = new BufferedReader(new InputStreamReader(stream));
            } catch (FileNotFoundException | NullPointerException e) {
                abortFileInput();
                return;
            }
            try {
                while (inputReader.ready() && processingDesirable) {
                    String nextInput = inputReader.readLine();
                    if (nextInput == null) {
                        break;
                    }
                    processInputString(nextInput);
                }
                inputReader.close();
            } catch (IOException | NullPointerException e) {
                abortFileInput();
            }
            try {
                inputReader.close();
            } catch (IOException e) {
                // We do not need to do anything more with this inputReader
            }
            inputReader = null;
        }

        /**
         * Reads input from the input reader and assembles an input string to be processed.
         */
        private void processSocketInput() {
            connectionLock.lock();
            try {
                while (clientSocket != null && !clientSocket.isClosed()) {
                    String nextInput = inputReader.readLine();
                    if (nextInput == null) {
                        break;
                    }
                    try {
                        processInputString(nextInput);
                    } catch (NumberFormatException e) {
                        // Call listener for showing error message
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (listener != null) {
                                    listener.onInvalidInputNumber();
                                }
                            }
                        });
                    }
                }
                disconnected.signal();
            } catch (IOException | NullPointerException e) {
                // Ignore exception
            } finally {
                connectionLock.unlock();
            }
        }

        /**
         * Aborts file input processing and updates the user interface.
         */
        private void abortFileInput() {
            if (inputReader != null) {
                try {
                    inputReader.close();
                } catch (IOException e) {
                    // We do not need to do anything more with this inputReader
                }
                inputReader = null;
            }
            if (inputUri != null) {
                inputUri = null;
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        // Abort file input processing
                        listener.onFileInputAborted();
                    }
                }
            });
        }

        /**
         * Processes the given input string by parsing the numbers and searching for compatible
         * generator states. The generator is eventually changed if the flag autoDetect is set and a
         * better generator is detected..
         * @param input string of newline separated integers
         * @throws NumberFormatException if input contains an invalid number string
         */
        private void processInputString(String input) throws NumberFormatException {
            long[] inputNumbers;
            String[] stringNumbers = input.split("\n");
            inputNumbers = new long[stringNumbers.length];
            // Parse numbers
            for (int i = 0; i < inputNumbers.length; i++) {
                inputNumbers[i] = Long.parseLong(stringNumbers[i]);
            }
            long[] historyPredictionNumbers;
            long[] historyNumbers = null;
            long[] replacedNumbers = null;
            int bestGenerator = 0;
            boolean generatorChanged = false;
            if (autoDetect) {
                // Detect best generator and update all states
                bestGenerator = randomManager.detectGenerator(inputNumbers, historyBuffer);
                historyPredictionNumbers = randomManager.getIncomingPredictionNumbers();
                if (bestGenerator != randomManager.getCurrentGenerator()) {
                    // Set generator and process complete history
                    randomManager.setCurrentGenerator(bestGenerator);
                    randomManager.resetCurrentGenerator();
                    historyNumbers = historyBuffer.toArray();
                    randomManager.findCurrentSeries(historyNumbers, null);
                    replacedNumbers = randomManager.getIncomingPredictionNumbers();
                    randomManager.findCurrentSeries(inputNumbers, historyBuffer);
                    historyPredictionNumbers = randomManager.getIncomingPredictionNumbers();
                    generatorChanged = true;
                }
            } else {
                randomManager.findCurrentSeries(inputNumbers, historyBuffer);
                historyPredictionNumbers = randomManager.getIncomingPredictionNumbers();
            }
            // Generate new prediction without updating the state
            long[] predictionNumbers = randomManager.predict(predictionLength);
            historyBuffer.put(inputNumbers);
            // Post result to user interface
            if (generatorChanged) {
                showGeneratorChange(inputNumbers, historyPredictionNumbers, predictionNumbers,
                        historyNumbers, replacedNumbers, bestGenerator);
            } else {
                showInputUpdate(inputNumbers, historyPredictionNumbers, predictionNumbers);
            }
        }

        /**
         * Sends the processing result to the processing listener.
         * @param inputNumbers the processed input numbers
         * @param historyPredictionNumbers the prediction numbers corresponding to the input
         * @param predictionNumbers the predicted numbers
         */
        private void showInputUpdate(final long[] inputNumbers,
                                     final long[] historyPredictionNumbers,
                                     final long[] predictionNumbers) {
            boolean posted = handler.post(new Runnable() {
                @Override
                public void run() {
                    // Append input numbers to history
                    if (listener != null) {
                        listener.onHistoryChanged(inputNumbers, historyPredictionNumbers);
                        listener.onPredictionChanged(predictionNumbers);
                    } else {
                        missingUpdate = true;
                    }
                }
            });
            if (!posted) {
                missingUpdate = true;
            }
            writeSocketOutput(predictionNumbers);
        }

        /**
         * Sends the processing result to the processing listener. The result includes a change of
         * generator.
         * @param inputNumbers the processed input numbers
         * @param historyPredictionNumbers the prediction numbers corresponding to the input
         * @param predictionNumbers the predicted numbers
         * @param historyNumbers the complete previous input
         * @param replacedNumbers the complete previous prediction numbers
         * @param bestGenerator index of the best generator
         */
        private void showGeneratorChange(final long[] inputNumbers,
                                         final long[] historyPredictionNumbers,
                                         final long[] predictionNumbers,
                                         final long[] historyNumbers,
                                         final long[] replacedNumbers,
                                         final int bestGenerator) {
            boolean posted = handler.post(new Runnable() {
                @Override
                public void run() {
                    // Append input numbers to history
                    if (listener != null) {
                        listener.onGeneratorChanged(bestGenerator);
                        listener.onHistoryPredictionReplaced(historyNumbers, replacedNumbers);
                        listener.onHistoryChanged(inputNumbers, historyPredictionNumbers);
                        listener.onPredictionChanged(predictionNumbers);
                    } else {
                        missingUpdate = true;
                    }
                }
            });
            if (!posted) {
                missingUpdate = true;
            }
            writeSocketOutput(predictionNumbers);
        }

        /**
         * Writes the prediction numbers to the client socket. Writes an additional newline after
         * the prediction block.
         * @param predictionNumbers the predicted numbers
         */
        private void writeSocketOutput(long[] predictionNumbers) {
            if (outputWriter != null && predictionNumbers != null) {
                try {
                    // Write numbers to output stream
                    for (long number : predictionNumbers) {
                        outputWriter.write(Long.toString(number));
                        outputWriter.newLine();
                    }
                    // Finish this sequence of numbers with an additional newline
                    outputWriter.newLine();
                    outputWriter.flush();
                } catch (IOException | NullPointerException e) {
                    // Ignore unsuccessful writes
                }
            }
        }
    }

    /**
     * This class implements a task for updating the generator prediction.
     */
    private class UpdatePredictionTask implements Runnable {
        /**
         * Starts executing the code of the task.
         */
        @Override
        public void run() {
            // Generate new prediction without updating the state
            final long[] predictionNumbers;
            if (historyBuffer.length() > 0) {
                predictionNumbers = randomManager.predict(predictionLength);
            } else {
                predictionNumbers = null;
            }
            boolean posted = handler.post(new Runnable() {
                @Override
                public void run() {
                    if (!missingUpdate && listener != null) {
                        listener.onPredictionChanged(predictionNumbers);
                    } else {
                        missingUpdate = true;
                    }
                }
            });
            if (!posted) {
                missingUpdate = true;
            }
        }
    }

    /**
     * This class implements a task for establishing a socket connection.
     */
    private class ServerTask implements Runnable {
        /**
         * Starts executing the code of the task.
         */
        @Override
        public void run() {
            connectionLock.lock();
            try {
                try {
                    serverSocket = new ServerSocket(serverPort);
                } catch (IOException e) {
                    abortSocketInput();
                    return;
                }
                while (!Thread.currentThread().isInterrupted()) {
                    String status;
                    if (clientSocket == null || clientSocket.isClosed()) {
                        try {
                            // Display information about the server socket
                            status = getResources().getString(R.string.server_listening) + " "
                                    + Integer.toString(serverPort);
                            postStatus(status);
                            clientSocket = serverSocket.accept();
                            status = getResources().getString(R.string.client_connected);
                            postStatus(status);
                            InputStream inputStream = clientSocket.getInputStream();
                            inputReader = new BufferedReader(new InputStreamReader(inputStream));
                            OutputStream outputStream = clientSocket.getOutputStream();
                            outputWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                        } catch (IOException e) {
                            closeClient();
                            continue;
                        }
                        boolean posted = handler.post(new Runnable() {
                            @Override
                            public void run() {
                                processInputSocket();
                            }
                        });
                        if (!posted) {
                            abortSocketInput();
                            return;
                        }
                    }
                    try {
                        disconnected.await();
                        closeClient();
                        status = getResources().getString(R.string.client_disconnected);
                        postStatus(status);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            } finally {
                connectionLock.unlock();
                closeSockets();
            }
        }

        /**
         * Posts the current socket status to the user interface thread.
         * @param status the status message
         */
        private void postStatus(final String status) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.onSocketStatusChanged(status);
                    }
                }
            });
        }

        /**
         * Aborts socket input processing and updates the user interface.
         */
        private void abortSocketInput() {
            closeSockets();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.onSocketInputAborted();
                    }
                }
            });
        }
    }
}
