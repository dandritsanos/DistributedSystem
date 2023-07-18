import java.io.Serializable;

public class Data implements Serializable {
    final double distance, elevation, speed, seconds;

    public Data(double dist, double elevation, double speed, double seconds) {
        this.distance = dist;
        this.elevation = elevation;
        this.speed = speed;
        this.seconds = seconds;
    }

    static Data zero() {
        return new Data(0, 0, 0, 0);
    }

    Data sum(Data d) {
        return new Data(distance + d.distance, elevation + d.elevation, 0, seconds + d.seconds);
    }

    public String toString() {
        return String.format("Data[dist=%.2f, elevation=%.2f, seconds=%.2f]",
                distance, elevation, seconds);
    }
}