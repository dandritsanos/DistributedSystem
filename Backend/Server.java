import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    public static void main(String[] args) {
        Socket clientSocket = null;
        ArrayList<Socket> workerSockets = new ArrayList<>();

        try (var server_socket = new ServerSocket(9999)) {
            String userInput;

            do {
                System.out.println("Waiting for worker to connect...");
                workerSockets.add(server_socket.accept());
                System.out.println("Worker added.");
                System.out.println("Press enter to continue adding workers, and anything else to stop");
                userInput = System.console().readLine();
            } while (userInput.isEmpty());

            System.out.println("Total of " + workerSockets.size() + " workers added in the server.");

            if (workerSockets.isEmpty()) {
                System.err.println("Can't open the server due to the lack of workers");
                return;
            }

            // stay open to keep connecting with clients
            while (true) {
                System.out.println("Server is up.");

                System.out.println("Waiting for client to connect...");
                clientSocket = server_socket.accept();
                System.out.println("Client connected");

                // Create a new thread to handle the client connection
                Thread clientThread = new MasterWorker(clientSocket, workerSockets);
                clientThread.start();
            }

        } catch (IOException e) {
            System.err.println("Could not listen on port: 9999.");
            System.exit(1);

        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
                for (Socket workerSocket : workerSockets) {
                    if (workerSocket != null) {
                        workerSocket.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
