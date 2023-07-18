package com.example.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

public class CommunicationThread extends Thread{
    Handler handler;
    HashMap<String, String> data_to_send;
    String ip;

    public CommunicationThread(HashMap<String, String> data_to_send, Handler handler, String port){
        this.data_to_send = data_to_send;
        this.handler = handler;
        this.ip = port;
    }



    @Override
    public void run() {
        try {
            Socket request_socket = new Socket(ip, 9999);
            ObjectOutputStream out_stream = new ObjectOutputStream(request_socket.getOutputStream());
            out_stream.writeObject(data_to_send);
            out_stream.flush();

            ObjectInputStream in_stream = new ObjectInputStream(request_socket.getInputStream());
            HashMap<String, Double> datareslts = (HashMap<String, Double>) in_stream.readObject();
            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putSerializable("results", datareslts);
            msg.setData(bundle);

            handler.sendMessage(msg);

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

