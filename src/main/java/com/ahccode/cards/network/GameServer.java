package com.ahccode.cards.network;

import com.ahccode.cards.card.Card;
import com.ahccode.cards.card.CardFamily;
import com.ahccode.cards.card.CardNumber;
import com.ahccode.cards.card.game.*;
import com.ahccode.cards.card.game.daketi.Daketi;
import com.ahccode.cards.card.game.thulla.Thulla;
import com.ahccode.cards.network.message.CardMessage;
import com.ahccode.cards.network.message.Message;
import com.ahccode.cards.network.message.MessageType;
import com.ahccode.cards.network.message.MoveMessage;
import com.ahccode.cards.ui.controller.GameScreenStarter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.ahccode.cards.card.game.context.GameContextCore.deck;

@Slf4j
public class GameServer {

    private static GameServer instance;

    private GameServer() {
    }

    public static GameServer getInstance() {
        if (instance == null) {
            instance = new GameServer();
        }
        return instance;
    }

    private int port;
    private ServerSocket serverSocket;

    // Thread-safe collections and proper client management
    private final Map<Integer, ClientInfo> clients = new ConcurrentHashMap<>();
    private final Object gameLock = new Object(); // For game state synchronization

    // Client info to track player numbers and streams
    private static class ClientInfo {
        final ObjectOutputStream out;
        final int playerNumber;

        ClientInfo(ObjectOutputStream out, int playerNumber) {
            this.out = out;
            this.playerNumber = playerNumber;
        }
    }

    private ObservableList<Player> players;

    public GameServer(int port) {
        this.port = port;
        players = new ObservableList<>(() -> {
            broadcast(new Message(MessageType.PLAYER_INFO_LIST, getConnectedPlayers()));
        });
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);

        while (true) {
            Socket socket = serverSocket.accept();
            synchronized (gameLock) {
                if (players.size() >= 4) {
                    System.out.println("Maximum Players already Reached");
                    socket.close();
                    continue;
                }

                System.out.println("New client connected: " + socket.getInetAddress());
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.flush(); // Important: flush immediately after creation

                int playerNumber = players.size();
                clients.put(playerNumber, new ClientInfo(out, playerNumber));

                // Create a thread for reading messages from this client
                new Thread(new ServerHandler(socket, out, this, playerNumber)).start();
            }
        }
    }

    public synchronized void broadcast(Message message) {
        log.info("Broadcasting message: {} to {} clients", message.getType(), clients.size());

        List<Integer> failedClients = new ArrayList<>();

        for (Map.Entry<Integer, ClientInfo> entry : clients.entrySet()) {
            try {
                ObjectOutputStream out = entry.getValue().out;
                synchronized (out) { // Synchronize each stream individually
                    out.reset(); // Reset the stream to prevent stale references
                    out.writeObject(message);
                    out.flush();
                }
                log.info("Successfully sent to client {}", entry.getKey());
            } catch (IOException e) {
                log.error("Failed to send message to client {}: {}", entry.getKey(), e.getMessage());
                failedClients.add(entry.getKey());
            }
        }

        // Remove failed clients
        for (Integer clientId : failedClients) {
            removeClient(clientId);
        }
    }

    public synchronized void broadcastToOthers(Message message, int excludePlayerNumber) {
        log.info("Broadcasting message: {} to {} clients (excluding player {})",
                message.getType(), clients.size() - 1, excludePlayerNumber);

        List<Integer> failedClients = new ArrayList<>();

        for (Map.Entry<Integer, ClientInfo> entry : clients.entrySet()) {
            // Skip the sender
            if (entry.getKey() == excludePlayerNumber) {
                log.info("Skipping sender (player {})", excludePlayerNumber);
                continue;
            }

            try {
                ObjectOutputStream out = entry.getValue().out;
                synchronized (out) { // Synchronize each stream individually
                    out.reset(); // Reset the stream to prevent stale references
                    out.writeObject(message);
                    out.flush();
                }
                log.info("Successfully sent to client {}", entry.getKey());
            } catch (IOException e) {
                log.error("Failed to send message to client {}: {}", entry.getKey(), e.getMessage());
                failedClients.add(entry.getKey());
            }
        }

        // Remove failed clients
        for (Integer clientId : failedClients) {
            removeClient(clientId);
        }
    }

    public List<PlayerInfo> getConnectedPlayers() {
        return players.stream()
                .map(Player::toPlayerInfo)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public static void main(String[] args) throws IOException {
        new GameServer(5000).start();
    }

    public void sendHelloMessage(Message message, int playerNumber) throws IOException {
        synchronized (gameLock) {
            PlayerInfo info = (PlayerInfo) message.getData();
            players.add(new Player(info.getPlayerNumber(), info.getName(), null));

            if (players.size() >= 4) {
                initializeGame();
                broadcast(new Message(MessageType.START_GAME, null));
            }
        }
    }

    public void getPlayerNumber(ObjectOutputStream out, int playerNumber) throws IOException {
        synchronized (out) {
            out.reset();
            out.writeObject(new Message(MessageType.GET_PLAYER_NO, playerNumber));
            out.flush();
        }
    }

    public void sendPlayerCards(ObjectOutputStream out, int playerNumber) throws IOException {
        synchronized (gameLock) {
            try {
                List<CardMessage> cards = getCardsForPlayer(playerNumber);
                Message message = new Message(MessageType.GET_START_CARDS, cards);

                synchronized (out) {
                    out.reset();
                    out.writeObject(message);
                    out.flush();
                }

                System.out.println("Sent " + cards.size() + " cards to player " + playerNumber);
            } catch (Exception e) {
                System.err.println("Error sending player cards to player " + playerNumber + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void removeClient(int playerNumber) {
        clients.remove(playerNumber);
        System.out.println("Removed client " + playerNumber + ". Remaining clients: " + clients.size());
    }

    public void removeClient(ObjectOutputStream out) {
        clients.entrySet().removeIf(entry -> entry.getValue().out == out);
    }

    private GameScreenStarter gameScreenStarter;

    public void setGameScreenStarter(GameScreenStarter gameScreenStarter) {
        this.gameScreenStarter = gameScreenStarter;
    }

    Game currentGame;

    public void initializeGame() {
        synchronized (gameLock) {
            deck = new Card[52];

            int n = 0;
            for (CardFamily family : CardFamily.values()) {
                for (CardNumber number : CardNumber.values()) {
                    deck[n] = new Card(family, number, false, false);
                    n++;
                }
            }
            currentGame = initializeDaketi();
            System.out.println("Game initialized for " + players.size() + " players");
        }
    }

    public Daketi initializeDaketi() {
        Daketi daketi = new Daketi(getConnectedPlayers());
        CardMessage[] cardMessages = Arrays.stream(deck)
                .map(Card::toCardMessage)
                .toArray(CardMessage[]::new);
        daketi.assignCardsToPlayersAndCenter(cardMessages);
        return daketi;
    }

    public Thulla initializeThulla() {
        return null;
    }

    int turn = 0;

    // Modified to send cards specific to the requesting player
    public List<CardMessage> getCardsForPlayer(int requestingPlayer) {
        synchronized (gameLock) {
            try {
                if (currentGame == null) {
                    System.err.println("Current game is null!");
                    return new ArrayList<>();
                }

                List<Card> allCards = new ArrayList<>();

                // Get cards from each player
                for (int i = 0; i < 4; i++) {
                    List<Card> playerCards = ((Daketi) currentGame).getCardsOfPlayer(i);
                    if (playerCards != null) {
                        allCards.addAll(playerCards);
                    }
                }

                // Get center cards
                List<Card> centerCards = ((Daketi) currentGame).getCardsInCenter();
                if (centerCards != null) {
                    allCards.addAll(centerCards);
                }

                // Get remaining cards
                List<Card> remainingCards = ((Daketi) currentGame).getRemainingCards();
                if (remainingCards != null) {
                    allCards.addAll(remainingCards);
                }

                // Convert to CardMessage safely
                List<CardMessage> cardMessages = new ArrayList<>();
                for (Card card : allCards) {
                    if (card != null) {
                        try {
                            CardMessage cardMessage = card.toCardMessage();
                            if (cardMessage != null) {
                                cardMessages.add(cardMessage);
                            }
                        } catch (Exception e) {
                            System.err.println("Error converting card to CardMessage: " + e.getMessage());
                        }
                    }
                }

                System.out.println("Generated " + cardMessages.size() + " card messages for player " + requestingPlayer);
                return cardMessages;

            } catch (Exception e) {
                System.err.println("Error in getCardsForPlayer: " + e.getMessage());
                e.printStackTrace();
                return new ArrayList<>();
            }
        }
    }

    public void moveCard(Message message, int senderPlayerNumber) {
        log.info("Move card called from player {}", senderPlayerNumber);
        synchronized (gameLock) {
            try {
                CardMessage cardMessage = (CardMessage) message.getData();

                // Broadcast to all connected clients EXCEPT the sender
                log.info("Broadcasting to others....");
                broadcastToOthers(message, senderPlayerNumber);
                log.info("Broadcasted move card message to other clients (excluding sender)");

            } catch (Exception e) {
                log.error("Error processing move card: {}", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Keep the old method for backward compatibility
    public void moveCard(Message message) {
        log.info("Move card called");
        synchronized (gameLock) {
            try {
                CardMessage cardMessage = (CardMessage) message.getData();

                // Broadcast to all connected clients
                log.info("Broadcasting....");
                broadcast(message);
                log.info("Broadcasted move card message to all clients");

            } catch (Exception e) {
                log.error("Error processing move card: {}", e.getMessage());
                e.printStackTrace();
            }
        }
    }
}