import java.util.ArrayList;
import java.util.List;

public class Parser {
    private String text, user = "";
    private int pos = 0, max_pos;
    private char cur_char;
    List<WPT> waypoints = new ArrayList<>();

    public Parser(String text) {
        this.max_pos = text.length();
        this.text = text;
        this.cur_char = max_pos > 0 ? text.charAt(0) : null;
    }

    private void advance() {
        pos++;
        cur_char = pos < max_pos ? text.charAt(pos) : null;
    }

    private void skip_spaces() {
        while (Character.isWhitespace(cur_char)) {
            advance();
        }
    }

    private void open_tag() {
        advance();
        get_name();
        advance();
    }

    private void close_tag() {
        advance();
        open_tag();
    }

    private String get_value_until_tag_open() {
        var sb = new StringBuilder();

        while (cur_char != '<') {
            sb.append(cur_char);
            advance();
        }

        return sb.toString();
    }

    private String get_name() {
        var sb = new StringBuilder();

        while (Character.isAlphabetic(cur_char)) {
            sb.append(cur_char);
            advance();
        }

        return sb.toString();
    }

    private String get_str() {
        advance();
        var sb = new StringBuilder();

        while (cur_char != '"') {
            sb.append(cur_char);
            advance();
        }

        advance();
        return sb.toString();
    }

    private String get_inner_tag_value() {
        open_tag();
        var val = get_value_until_tag_open();
        close_tag();
        return val;
    }

    private WPT get_wpt_tag() {
        open_tag();
        get_name();
        advance();
        var lat_value = Double.parseDouble(get_str());
        advance();
        get_name();
        advance();
        var lon_value = Double.parseDouble(get_str());
        advance();
        skip_spaces();
        var ele_value = Double.parseDouble(get_inner_tag_value());
        skip_spaces();
        String time = get_inner_tag_value();
        skip_spaces();
        close_tag();
        return new WPT(lat_value, lon_value, ele_value, time);
    }

    private void user_parse() {
        // <?xml version="1.0"?>
        open_tag();
        get_value_until_tag_open();

        // <gpx version="1.1" creator="user2">
        open_tag();
        get_name();
        advance();
        get_name();
        advance();
        get_str();
        advance();
        get_name();
        advance();
        this.user = get_str();
    }

    private void parse() {
        user_parse();
        get_value_until_tag_open();

        while (text.charAt(pos + 1) != '/') {
            waypoints.add(get_wpt_tag());
            skip_spaces();
        }
    }

    static String extract_user(String file) {
        var p = new Parser(file);
        p.user_parse();
        return p.user;
    }

    static List<WPT> extract_waypoints(String file) {
        var p = new Parser(file);
        p.parse();
        return p.waypoints;
    }

}

