package com.example.myapplication;

public class Parser {
    private String text, user = "";
    private int pos = 0, max_pos;
    private char cur_char;

    public Parser(String text) {
        this.max_pos = text.length();
        this.text = text;
        this.cur_char = max_pos > 0 ? text.charAt(0) : null;
    }

    private void advance() {
        pos++;
        cur_char = pos < max_pos ? text.charAt(pos) : null;
    }

    private void open_tag() {
        advance();
        get_name();
        advance();
    }

    private String get_value_until_tag_open() {
        StringBuilder sb = new StringBuilder();

        while (cur_char != '<') {
            sb.append(cur_char);
            advance();
        }

        return sb.toString();
    }

    private String get_name() {
        StringBuilder sb = new StringBuilder();

        while (Character.isAlphabetic(cur_char)) {
            sb.append(cur_char);
            advance();
        }

        return sb.toString();
    }

    private String get_str() {
        advance();
        StringBuilder sb = new StringBuilder();

        while (cur_char != '"') {
            sb.append(cur_char);
            advance();
        }

        advance();
        return sb.toString();
    }

    public void parse() {
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

    static String extract_user(String file) {
        Parser p = new Parser(file);
        p.parse();
        return p.user;
    }
}