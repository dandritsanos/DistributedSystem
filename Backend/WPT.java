import java.io.Serializable;

public class WPT implements Serializable {
    public final double lat, lon, ele;
    public final String time;

    public WPT(double lat, double lon, double ele, String time) {
        this.lat = lat;
        this.lon = lon;
        this.ele = ele;
        this.time = time;
    }

    @Override
    public String toString() {
        return String.format("WPT(%f, %f, %f, %s)", lat, lon, ele, time);
    }
}
