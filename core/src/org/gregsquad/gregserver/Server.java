package org.gregsquad.gregserver;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private int port;
    private List<ClientHandler> clients;

    public Server(int port) {
        this.port = port;
        this.clients = new ArrayList<>();
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[INFO] Server is listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[CLIENT] New client connected.");

                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void broadcast(Message<String> message, ClientHandler excludeClient) {
        for (ClientHandler client : clients) {
            if (client != excludeClient) {
                client.sendMessage(message);
            }
        }
    }

    public static void main(String[] args) {
        //Server server = new Server(27093);
        //server.run();
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private Server server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String clientName;

    public ClientHandler(Socket clientSocket, Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    public void run() {
        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
            System.out.println("[CLIENT] Waiting for client name...");

            // Read the first object from the stream and handle it based on its type
            Object firstObject = in.readObject();
            if (firstObject instanceof Message) {
                Message<String> firstMessage = (Message<String>) firstObject;
                clientName = firstMessage.getSender();
                System.out.println("[CLIENT] " + clientName + " connected.");
                server.broadcast(firstMessage, this);
            }

            // FAIRE LA DETECTION DES MESSAGES ICI
            Message<String> inputMessage;
            while ((inputMessage = (Message<String>) in.readObject()) != null) {
                System.out.println(inputMessage.getSender() + " says: " + inputMessage.getContent());
                server.broadcast(inputMessage, this);
            }

            out.close();
            in.close();
            clientSocket.close();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error handling client connection: " + e.getMessage());
        }
    }

    public void sendMessage(Message<String> message) {
        try {
            out.writeObject(message);
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }
}