package com.ahccode.client.network;

import com.ahccode.client.ClientMainFX;
import com.ahccode.client.game.daketi.DaketiController;
import com.ahccode.client.gui.PlayerWaitingScreen;
import com.ahccode.client.gui.StartScreen;
import com.ahccode.common.context.GameContextCore;
import com.ahccode.common.network.CardMessage;
import com.ahccode.common.network.Message;
import com.ahccode.common.network.MessageType;
import com.ahccode.common.network.PlayerInfo;
import javafx.application.Platform;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class GameClient {
    private final String host;
    private final int port;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private final CountDownLatch connectionReady = new CountDownLatch(1);
    private volatile boolean connected = false;

    public List<PlayerInfo> playerInfoList;

    @Getter
    private int myPlayerNumber = -1;

    public GameClient(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
    }

    public void start() throws IOException {
        Socket socket = new Socket(host, port);

        // Create streams with proper ordering
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush(); // Important: flush immediately

        in = new ObjectInputStream(socket.getInputStream());
        connected = true;

        log.info("Connected to server...");

        // Thread to listen to server messages
        new Thread(() -> {
            try {
                // Signal that the connection is ready
                connectionReady.countDown();

                while (connected) {
                    try {
                        Object obj = in.readObject();
                        if (!(obj instanceof Message)) {
                            log.error("Received non-Message object: {}", obj.getClass());
                            continue;
                        }

                        Message msg = (Message) obj;
                        log.info("Server says: {} - {}", msg.getType(), msg.getData());

                        switch (msg.getType()) {
                            case GET_PLAYER_NO:
                                myPlayerNumber = (int) msg.getData();
                                log.info("Assigned player number: {}", myPlayerNumber);
                                break;

                            case PLAYER_INFO_LIST:
                                playerInfoList = (List<PlayerInfo>) msg.getData();
                                log.info("Current players: {}", playerInfoList.size());
                                Platform.runLater(() -> {
                                    PlayerWaitingScreen.getInstance().updatePlayerInfoList(playerInfoList);
                                });
                                break;

                            case START_GAME:
                                log.info("Game started! Requesting cards...");
                                send(new Message(MessageType.GET_START_CARDS, null));
                                break;

                            case GET_START_CARDS:
                                List<CardMessage> cardMessages = (List<CardMessage>) msg.getData();
                                log.info("Received {} cards", cardMessages.size());
                                Platform.runLater(() -> {
                                    StartScreen.getInstance().startGame(playerInfoList, cardMessages);
                                });
                                break;

                            case MOVE_CARD:
                                log.info("Received move card message for player {}", myPlayerNumber);
                                CardMessage cardMessage  = (CardMessage) msg.getData();
                                if (animationRunnable != null) {
                                    log.info("Running animation...");
                                    DaketiController.setSelectedCard(cardMessage);
                                    Platform.runLater(animationRunnable);
                                }
                                break;

                            case SERVER_SHUTDOWN:
                                log.info("Received SERVER_SHUTDOWN for player {}, {}", myPlayerNumber, GameContextCore.GAME_FINISHED);
                                if (!GameContextCore.GAME_FINISHED)
                                    Platform.runLater(() -> ClientMainFX.restartGame(true));
                                break;

                            case GAME_FINISHED:
                                GameContextCore.GAME_FINISHED = true;
                                log.info("Processing GAME_FINISHED for player {}", myPlayerNumber);
                                break;

                            default:
                                log.info("Unknown message type: {}", msg.getType());
                                break;
                        }

                    } catch (ClassNotFoundException e) {
                        log.error("Error deserializing message: {}", e.getMessage());
                        log.error("error: {}", e.getCause().toString());
                        break;
                    } catch (IOException e) {
                        if (connected) {
                            log.error("IO Error reading from server: {}", e.getMessage());
                            log.error("error: {}", e.getCause().toString());
                        }
                        if (!GameContextCore.GAME_FINISHED)
                            Platform.runLater(() -> ClientMainFX.restartGame(true));
                        break;
                    } catch (Exception e) {
                        log.error("error: {}", e.getCause().toString());
                        if (!GameContextCore.GAME_FINISHED)
                            Platform.runLater(() -> ClientMainFX.restartGame(true));
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("Unexpected error in client listener: {}", e.getMessage());
                e.printStackTrace();
            } finally {
                connected = false;
                log.info("Client listener thread terminated");
            }
        }).start();

        // Wait for the listener thread to be ready before requesting player number
        try {
            connectionReady.await();
            Thread.sleep(200); // Increased delay to ensure server handler is ready
            requestPlayerNumber();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for connection", e);
        }
    }

    public void requestPlayerNumber() throws IOException {
        log.info("Requesting player number from server...");
        send(new Message(MessageType.GET_PLAYER_NO, null));
    }

    public synchronized void send(Message message) throws IOException {
        if (out != null && connected) {
            try {
                log.info("Sending Message: {}", message);
                out.reset(); // Reset stream to prevent stale references
                out.writeObject(message);
                out.flush();
                log.info("Sent message: {}", message.getType());
            } catch (IOException e) {
                log.error("Error sending message: {}", e.getMessage());
                throw e;
            }
        } else {
            throw new IOException("Output stream not initialized or not connected");
        }
    }

    @Setter
    private volatile Runnable animationRunnable;

    public void disconnect() {
        connected = false;
        try {
            if (out != null) out.close();
            if (in != null) in.close();
        } catch (IOException e) {
            System.err.println("Error closing streams: " + e.getMessage());
        }
    }

    public void close() throws IOException {
        out.close();
        in.close();
    }
}