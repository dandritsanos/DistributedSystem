import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MasterWorker extends Thread {
    static int cur_con_index = 0;
    static int num_of_workers = 0;

    static HashMap<String, Data> route_stats = new HashMap<>();
    static HashMap<String, Data> user_stats = new HashMap<>();
    static Data total_stats = Data.zero();

    ArrayList<Socket> workers_sockets = new ArrayList<>();
    ObjectInputStream client_in;
    ObjectOutputStream client_out;
    HashMap<String, Double> data = new HashMap<>();

    public MasterWorker(Socket socket, ArrayList<Socket> workers) {
        try {
            client_out = new ObjectOutputStream(socket.getOutputStream());
            client_in = new ObjectInputStream(socket.getInputStream());
            workers_sockets = workers;
            num_of_workers = workers.size();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T> List<List<T>> split_list(List<T> input_list, int size) {
        int list_size = input_list.size();
        int start_index = 0, end_index = 0;
        List<List<T>> sublists = new ArrayList<>();

        while (start_index < list_size) {
            end_index = Math.min(start_index + size, list_size);
            if ((end_index - start_index) < size)
                sublists.add(new ArrayList<>(input_list.subList(start_index, end_index)));
            else {
                sublists.add(new ArrayList<>(input_list.subList(start_index, end_index + 1)));
            }
            start_index = end_index;
        }
        return sublists;
    }

    public static synchronized void switch_connections() {
        // round robin
        cur_con_index = (cur_con_index + 1) % num_of_workers;
    }

    List<Data> get_workers_data(List<WPT> route, int chunks) throws IOException, ClassNotFoundException {
        var chunked_route = split_list(route, chunks);
        var intermediate_data = new ArrayList<Data>();

        for (var chunk : chunked_route) {
            var cur_socket = workers_sockets.get(cur_con_index);
            var out_stream = new ObjectOutputStream(cur_socket.getOutputStream());
            out_stream.writeObject(chunk);
            out_stream.flush();

            var in_stream = new ObjectInputStream(cur_socket.getInputStream());
            var indata = (Data) in_stream.readObject();
            intermediate_data.add(indata);
            switch_connections();
        }

        return intermediate_data;
    }

    Data reduce(List<Data> intermediate_data) {
        double distance = 0, elevation = 0, average_speed = 0, time = 0;

        for (var d : intermediate_data) {
            distance += d.distance;
            elevation += d.elevation;
            time += d.seconds;
            average_speed += d.speed;
        }

        double total_average_speed = average_speed / intermediate_data.size();
        return new Data(distance, elevation, total_average_speed, time);
    }

    synchronized public void update_route_stats(String route, Data new_data) {
        route_stats.put(route, new_data);
    }

    synchronized public void update_user_stats(String username, Data new_data) {
        var current_data = user_stats.getOrDefault(username, Data.zero());
        user_stats.put(username, current_data.sum(new_data));
    }

    synchronized public void update_total_stats(Data new_data) {
        total_stats = total_stats.sum(new_data);
    }

    static double round(double d) {
        return Math.round(d * 1000.0) / 1000.0;
    }

    @SuppressWarnings("unchecked")
    public void run() {
        try {
            var received_data = (HashMap<String, String>) client_in.readObject();

            if (received_data.containsKey("user-stats")) {
                var username = received_data.get("user-stats");
                var u_stats = user_stats.getOrDefault(username, Data.zero());
                var divider = route_stats.size();

                data.put("dist_user", round(u_stats.distance));
                data.put("ele_user", round(u_stats.elevation));
                data.put("sec_user", round(u_stats.seconds));
                data.put("dist_avg", round(total_stats.distance / divider));
                data.put("ele_avg", round(total_stats.elevation / divider));
                data.put("sec_avg", round(total_stats.seconds / divider));

                client_out.writeObject(data);
                client_out.flush();
                data.clear();

            } else {
                System.out.println("calculate route");
                var user = received_data.get("user");
                var entry = received_data.entrySet().iterator().next();
                String filename = entry.getKey();
                List<WPT> waypoints = Parser.extract_waypoints(entry.getValue());

                Data final_data;

                if (route_stats.containsKey(filename)) {
                    final_data = route_stats.get(filename);
                } else {
                    final_data = reduce(get_workers_data(waypoints, 10));
                    update_route_stats(filename, final_data);
                    update_user_stats(user, final_data);
                    update_total_stats(final_data);
                }

                data.put("dist", round(final_data.distance));
                data.put("ele", round(final_data.elevation));
                data.put("speed", round(final_data.speed));
                data.put("sec", round(final_data.seconds));

                System.out.println("Data been sent to client.");
                client_out.writeObject(data);
                client_out.flush();
                data.clear();
            }

            System.out.println(route_stats);
            System.out.println(user_stats);
            System.out.println(total_stats);

        } catch (IOException e) {
            e.printStackTrace();

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }
}
