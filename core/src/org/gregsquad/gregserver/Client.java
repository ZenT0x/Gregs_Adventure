package org.gregsquad.gregserver;

import org.gregsquad.gregsadventure.card.*;
import org.gregsquad.gregsadventure.game.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
    private String serverIp; // Server IP address
    private int serverPort; // Server port number
    private String name; // Client name
    private Socket echoSocket; // Socket for communication
    private ObjectOutputStream out; // Output stream
    private ObjectInputStream in; // Input stream

    // Constructor
    public Client(String serverIp, int serverPort, String name) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.name = name;
    }

    // Method to start the client
    public void run() {
        try {
            // Connect to the server
            echoSocket = new Socket(serverIp, serverPort);
            System.out.println("["+name+"] " + "Connected to " + serverIp + ":" + serverPort);

            // Create input and output streams
            System.out.println("["+name+"] " + "Creating streams");
            out = new ObjectOutputStream(echoSocket.getOutputStream());
            in = new ObjectInputStream(echoSocket.getInputStream());
            System.out.println("["+name+"] " + "Streams created");

            // Send the client name to the server
            System.out.println("["+name+"] " + "Sending name: " + name);
            //out.writeObject(new Message<String>(name, "CONNEXION", "NAME","has joined the chat"));
            //sendInformation("CONNEXION", "NAME", "has joined the chat");
            
            while(true){
                Message<String> answer = request("CONNEXION", "NAME");
                if(answer.getContent().equals("OK")){
                    break;
                }
                System.out.println("["+name+"] " +"Name already taken. Please enter a new name.");
            }
            
            System.out.println("");
            System.out.println("["+name+"] " + name + " is correctly connected");
            System.out.println("");

            Thread receiveThread = new Thread(new ReceiveThread());
            receiveThread.start();
            receiveThread.join();

            // Close the streams and the connection
            out.close();
            in.close();
            echoSocket.close();
            
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("InterruptedException: " + e.getMessage());
        }
    }

    // Thread for receiving messages
    class ReceiveThread implements Runnable {
        public void run() {
            try {
                Message<?> inputMessage;
                while ((inputMessage = (Message<?>) in.readObject()) != null) {

                    if (inputMessage.isOfType(String.class)) {
                        Message<String> stringMessage = (Message<String>) inputMessage;
                        // Manage the message of type String here
                        System.out.println("["+name+"] " + "Received message: " + stringMessage.getType() + " " + stringMessage.getPurpose() + " " + stringMessage.getContent());
                        // Check if the message is a connexion message
                        if(stringMessage.getType().equals("CONNEXION")) {

                            if(stringMessage.getPurpose().equals("NAME")) {
                                
                            }
                        }

                    } 
                    else if (inputMessage.isOfType(Card.class)) {
                        Message<Card> cardMessage = (Message<Card>) inputMessage;
                        // Traitez le message de type Card ici    
                    }
            }
            } catch (IOException e) {
                System.err.println("IOException: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                System.err.println("ClassNotFoundException: " + e.getMessage());
            }
        }
    }

    // REQUESTS SECTION

    // Generic method to send a request
    public <T extends Serializable> void sendRequest(Message<T> request) {
        try {
            System.out.println("["+name+"] " + "Sending request: " + request.getType() + " " + request.getPurpose());
            out.writeObject(request);
        } catch (IOException e) {
            System.err.println("["+name+"] " + "Error sending message: " + e.getMessage());
        }
    }
    // Generic method to receive an answer
    public <T extends Serializable> Message<T> receiveAnswer(UUID id, String type, String purpose) {
        try {   
                echoSocket.setSoTimeout(5000);
                Object inputObject;
                while ((inputObject = in.readObject()) != null) {
                    if (inputObject instanceof Message) {
                        Message<T> inputMessage = (Message<T>) inputObject;
                        if (inputMessage.getId().equals(id) && inputMessage.getType().equals(type) && inputMessage.getPurpose().equals(purpose)) {
                            System.out.println("["+name+"] "+"Received answer: " + inputMessage.getType() + " " + inputMessage.getPurpose());
                            return inputMessage;
                        }
                    }
                }
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("ClassNotFoundException: " + e.getMessage());
        }
        System.out.println("["+name+"] "+"Error: no answer received");
        return null;
    }

    // Generic method to send a request and receive an answer
    public <T extends Serializable> Message<T> request(String type, String purpose) {
        Message<String> request = new Message<String>(name, type, purpose,"",String.class);
        System.out.println("["+name+"] " + "Sending request. Name: " + request.getSender() + " Type: " + request.getType() + " Purpose: " + request.getPurpose());
        sendRequest(request);
        Message<T> answer = receiveAnswer(request.getId(), type, purpose);
        System.out.println("["+name+"] " + "Received answer. Name: " + answer.getSender() + " Type: " + answer.getType() + " Purpose: " + answer.getPurpose());
        return answer;
    } 
    // Generic method to send information
    public <T extends Serializable> void sendInformation(String type, String purpose, T content) {
        Message<T> information = new Message<T>(name, type, purpose, content, content.getClass());
        System.out.println("["+name+"] " + "Sending information. Name: " + information.getSender() + " Type: " + information.getType() + " Purpose: " + information.getPurpose());
        sendRequest(information);
    }

    // Specific methods to send requests and receive answers
    public Card drawDonjonCard() {
        Message<Card> answer = request("GAME", "DRAW_DONJON_CARD");
        System.out.println("["+name+"] " + name + " drew a donjon card: " + answer.getContent().getName());
        return answer.getContent();
    }

    public Card drawTreasureCard() {
        Message<Card> answer = request("GAME", "DRAW_TREASURE_CARD");
        System.out.println("["+name+"] " + name + " drew a treasure card: " + answer.getContent().getName());
        return answer.getContent();
    }

    public LinkedList<Card> getDonjonDiscard() {
        Message<LinkedList<Card>> answer = request("GAME", "GET_DONJON_DISCARD");
        System.out.println("["+name+"] " + name + " got the donjon discard");
        return answer.getContent();
    }

    public LinkedList<Card> getTreasureDiscard() {
        Message<LinkedList<Card>> answer = request("GAME", "GET_TREASURE_DISCARD");
        System.out.println("["+name+"] " + name + " got the treasure discard");
        return answer.getContent();
    }

    public Player getCurrentPlayer() {
        Message<Player> answer = request("GAME", "GET_CURRENT_PLAYER");
        System.out.println("["+name+"] " + name + " got the current player");
        return answer.getContent();
    }

    public ArrayList<Player> getPlayerList() {
        Message<ArrayList<Player>> answer = request("GAME", "GET_PLAYER_LIST");
        System.out.println("["+name+"] " + name + " got the player list");
        //print the player list
        for(Player player : answer.getContent()){
            System.out.println(player.getName());
        }
        return answer.getContent();
    }

    public boolean initGame() {
        Message<Boolean> answer = request("GAME", "INIT_GAME");
        System.out.println("["+name+"] " + name + " initialized the game");
        return answer.getContent();
    }

    // Main method
    public static void main(String[] args) {
        // Check the arguments
        if (args.length != 3) {
            System.err.println("Need 3 arguments: server IP, server port, player name");
            System.exit(1);
        }
        // Create the client
        Client client = new Client(args[0], Integer.parseInt(args[1]), args[2]);
        // Start the client
        client.run();
    }
}
