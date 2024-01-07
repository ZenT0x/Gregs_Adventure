/**
 * The Client class is responsible for managing the client-side logic of the game.
 * It connects to the server, sends and receives messages, and handles reconnections.
 */
public class Client {
    private String serverIp; // Server IP address
    private int serverPort; // Server port number
    private String name; // Client name
    private Socket echoSocket; // Socket for communication
    private ObjectOutputStream out; // Output stream
    private ObjectInputStream in; // Input stream

    private static final int MAX_RECONNECT_ATTEMPTS = 5; // Maximum number of reconnection attempts
    private static final int RECONNECT_DELAY_MS = 5000; // Delay between reconnection attempts

    /**
     * Constructs a new Client with the given server IP, server port, and client name.
     * @param serverIp the IP address of the server.
     * @param serverPort the port number of the server.
     * @param name the name of the client.
     */
    public Client(String serverIp, int serverPort, String name) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.name = name;
    }
}
    /**
     * Tries to connect to the server and create input and output streams.
     * If a connection attempt times out, it retries up to a maximum number of attempts.
     * Between each attempt, it waits for a specified delay.
     * @throws IOException if it fails to connect after the maximum number of attempts.
     * @throws InterruptedException if the thread is interrupted while waiting between attempts.
     */
    private void connect() throws IOException, InterruptedException {
        int attempts = 0;
        while (true) {
            try {
                // Connect to the server
                echoSocket = new Socket(serverIp, serverPort);
                System.out.println("["+name+"] " + "Connected to " + serverIp + ":" + serverPort);

                // Create input and output streams
                System.out.println("["+name+"] " + "Creating streams");
                out = new ObjectOutputStream(echoSocket.getOutputStream());
                in = new ObjectInputStream(echoSocket.getInputStream());
                System.out.println("["+name+"] " + "Streams created");

                // Connection successful, break the loop
                break;
            } catch (SocketTimeoutException e) {
                attempts++;
                if (attempts > MAX_RECONNECT_ATTEMPTS) {
                    throw new IOException("Failed to connect after " + MAX_RECONNECT_ATTEMPTS + " attempts", e);
                }
                System.err.println("Connection timed out, retrying in " + RECONNECT_DELAY_MS + "ms...");
                Thread.sleep(RECONNECT_DELAY_MS);
            }
        }
    }

    /**
     * The main loop of the Client.
     * Connects to the server, sends the client's name, and starts the receive and debug send threads.
     * After the debug send thread finishes, it closes the streams and the connection.
     * If an error occurs during this process, it is caught and printed.
     */
    public void run() {
        try {
            connect();

            System.out.println("["+name+"] " + "Sending name: " + name);
        
            Message<String> answer = request("CONNEXION", "NAME");
                
            System.out.println("");
            System.out.println("["+name+"] " + name + " is correctly connected");
            System.out.println("");

            Thread receiveThread = new Thread(new ReceiveThread());

            Thread debugSendThread = new Thread(new DebugSendThread());
            debugSendThread.start();
            debugSendThread.join();
            //receiveThread.start();
            //receiveThread.join();

            // Close the streams and the connection
            out.close();
            in.close();
            echoSocket.close();
            
        } catch (IOException e) {
            System.err.println("IOException1: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("InterruptedException: " + e.getMessage());
        }
    }

    /**
     * Stops the client connection by closing the input and output streams and the socket.
     * If an error occurs while stopping the client, it is caught and printed.
     */
    public void stop() {
        try {
            // Close the streams and the connection
            out.close();
            in.close();
            echoSocket.close();
        } catch (IOException e) {
            System.err.println("IOException1: " + e.getMessage());
        }
    }

    /**
     * The ReceiveThread class implements the Runnable interface and is responsible for receiving messages from the server.
     * It reads messages from the input stream and performs actions based on the type and content of the messages.
     * It continues to read messages until an error occurs or the end of the stream is reached.
     */
    class ReceiveThread implements Runnable {
        public void run() {
            try {
                System.out.println("["+name+"] " + "Listening for messages");
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
                System.err.println("IOException2: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                System.err.println("ClassNotFoundException: " + e.getMessage());
            }
        }
    }

    /**
     * The AnswerReceiver class implements the Runnable interface and is responsible for receiving answers from the server.
     * It reads messages from the input stream and checks if they match the expected ID, type, and purpose.
     * If a matching message is found, it is stored and the thread returns.
     * If an error occurs while reading a message, it is caught and printed, and the request is sent again.
     * @param <T> the type of the content of the message, which must implement Serializable.
     */
    public class AnswerReceiver<T extends Serializable> implements Runnable {
        private final UUID id; // The expected ID of the answer
        private final String type; // The expected type of the answer
        private final String purpose; // The expected purpose of the answer
        private final Socket echoSocket; // The socket for communication
        private final ObjectInputStream in; // The input stream
        private final String name; // The name of the client
        private Message<T> answer; // The received answer

        /**
         * Constructs a new AnswerReceiver with the expected ID, type, and purpose of the answer, the socket for communication, the input stream, and the name of the client.
         * @param id the expected ID of the answer.
         * @param type the expected type of the answer.
         * @param purpose the expected purpose of the answer.
         * @param echoSocket the socket for communication.
         * @param in the input stream.
         * @param name the name of the client.
         */
        public AnswerReceiver(UUID id, String type, String purpose, Socket echoSocket, ObjectInputStream in, String name) {
            this.id = id;
            this.type = type;
            this.purpose = purpose;
            this.echoSocket = echoSocket;
            this.in = in;
            this.name = name;
        }

        /**
         * The main loop of the AnswerReceiver.
         * Reads messages from the input stream and checks if they match the expected ID, type, and purpose.
         * If a matching message is found, it is stored and the thread returns.
         * If an error occurs while reading a message, it is caught and printed, and the request is sent again.
         */
        @Override
        public void run() {
            try {   
                echoSocket.setSoTimeout(5000);
                Object inputObject;
                while ((inputObject = in.readObject()) != null) {
                    if (inputObject instanceof Message) {
                        Message<T> inputMessage = (Message<T>) inputObject;
                        if (inputMessage.getId().equals(id) && inputMessage.getType().equals(type) && inputMessage.getPurpose().equals(purpose)) {
                            System.out.println("["+name+"] "+"Received answer: " + inputMessage.getType() + " " + inputMessage.getPurpose());
                            answer = inputMessage;
                            return;
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("IOException3: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                System.err.println("ClassNotFoundException: " + e.getMessage());
            }
            System.out.println("["+name+"] "+"Error: no answer received");
            //Send the same request again
            System.out.println("["+name+"] =-=-=-=-=-=-=-=-=-=-=-==-=--==--==--=-=-==--==-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=----=-=-=-=-=--==-=-=-=--=-=-=");
            sendRequest(new Message<T>(id, name, type, purpose, null, String.class));
        }
    
        public Message<T> getAnswer() {
            return answer;
        }
    }


    // Debug thread for sending requests every 5 seconds
    class DebugSendThread implements Runnable {
        public void run() {
            try {
                while (true) {
                    getPlayerList();
                    Thread.sleep(5000);
                }
            } catch (InterruptedException e) {
                System.err.println("InterruptedException: " + e.getMessage());
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
            System.err.println("IOException3: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("ClassNotFoundException: " + e.getMessage());
        }
        System.out.println("["+name+"] "+"Error: no answer received");
        return null;
    }

    // Generic method to send a request and receive an answer
    public <T extends Serializable> Message<T> request(String type, String purpose) {
        Message<String> request_locale = new Message<String>(name, type, purpose,"",String.class);
        System.out.println("["+name+"] " + "Sending request. Name: " + request_locale.getSender() + " Type: " + request_locale.getType() + " Purpose: " + request_locale.getPurpose());

        // Create an instance of AnswerReceiver with the necessary parameters
        AnswerReceiver<T> answerReceiver = new AnswerReceiver<>(request_locale.getId(), type, purpose, echoSocket, in, name);  

        // Create a new thread with the AnswerReceiver instance
        Thread answerReceiverThread = new Thread(answerReceiver);

        answerReceiverThread.start();
        sendRequest(request_locale);

        try {
            answerReceiverThread.join();
        } catch (InterruptedException e) {
            System.err.println("InterruptedException: " + e.getMessage());
        }
        Message<T> answer = answerReceiver.getAnswer();

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
        System.out.println("-------------[CLIENT] " + answer.getContent().size());
        return answer.getContent();
    }

    public boolean initGame() {
        Message<Boolean> answer = request("GAME", "INIT_GAME");
        System.out.println("["+name+"] " + name + " initialized the game");
        return answer.getContent();
    }

    public String ping() {
        Message<String> answer = request("PING", "");
        System.out.println("["+name+"] " + name + " pinged the server");
        return answer.getContent();
    }
}
