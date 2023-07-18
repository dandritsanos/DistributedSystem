import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ActionsWorkers extends Thread {
    List<WPT> chunk;
    ObjectInputStream client_in;
    ObjectOutputStream client_out;

    public ActionsWorkers(Socket work_socket, List<WPT> chunk) {
        try {
            this.chunk = chunk;
            client_out = new ObjectOutputStream(work_socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Data map(List<WPT> chunk) {
        return new Data(total_distance(chunk), total_elevation(chunk),
                average_speed(chunk), total_time_seconds(chunk));
    }

    public static double total_distance(List<WPT> chunk) {
        var total = 0;
        for (int i = 0; i < chunk.size() - 1; i++) {
            var dist = distance_meters(chunk.get(i), chunk.get(i + 1));
            total += dist;
        }
        return total;
    }

    private static double distance_meters(WPT wpt1, WPT wpt2) {
        var lat1 = Math.toRadians(wpt1.lat);
        var lon1 = Math.toRadians(wpt1.lon);
        var lat2 = Math.toRadians(wpt2.lat);
        var lon2 = Math.toRadians(wpt2.lon);

        var dlon = lon2 - lon1;
        var dlat = lat2 - lat1;

        var a = Math.pow(Math.sin(dlat / 2), 2) +
                Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2), 2);

        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        var earth_radius = 6371;
        var distance = earth_radius * c * 1000;
        return distance;

    }

    public static double total_elevation(List<WPT> chunk) {
        var total_elevation = 0.0;

        for (int i = 1; i < chunk.size() - 1; i++) {
            var elevation_change = chunk.get(i).ele - chunk.get(i + 1).ele;

            if (elevation_change > 0)
                total_elevation += elevation_change;
        }
        return total_elevation;
    }

    public static double average_speed(List<WPT> chunk) {
        var total_distance = total_distance(chunk);
        var total_time_seconds = total_time_seconds(chunk);

        if (total_time_seconds == 0)
            return 0;

        return total_distance / total_time_seconds;
    }

    public static double total_time_seconds(List<WPT> chunk) {
        var total_seconds = 0;
        var formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        for (int i = 1; i < chunk.size(); i++) {
            try {
                Date prev_time = formatter.parse(chunk.get(i - 1).time);
                Date cur_time = formatter.parse(chunk.get(i).time);
                total_seconds += (cur_time.getTime() - prev_time.getTime()) / 1000.0;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return total_seconds;
    }

    public void run() {
        var mapped_data = map(chunk);
        try {
            client_out.writeObject(mapped_data);
            client_out.flush();
            System.out.println(mapped_data + " sent to master.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}