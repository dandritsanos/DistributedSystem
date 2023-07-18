import java.io.IOException;
import java.util.List;
import java.io.ObjectInputStream;
import java.net.ConnectException;
import java.net.Socket;

public class Worker {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        ObjectInputStream in_stream = null;

        try (var workerSocket = new Socket("localhost", 9999)) {
            System.out.println("Connected to Master Worker");

            while (true) {
                in_stream = new ObjectInputStream(workerSocket.getInputStream());
                List<WPT> chunk = (List<WPT>) in_stream.readObject();
                Thread clientThread = new ActionsWorkers(workerSocket, chunk);
                clientThread.start();
            }

        } catch (ConnectException conEx) {
            System.err.println("Server is Closed.");

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();

        } finally {
            try {
                if (in_stream != null)
                    in_stream.close();

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

    }

}
