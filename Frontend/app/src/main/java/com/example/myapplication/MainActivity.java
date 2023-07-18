package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.net.Uri;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;



import android.app.Activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;


import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;

import java.io.FileReader;
import java.io.IOException;

import java.net.URISyntaxException;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 123;
    private static final int PICK_FILE_REQUEST_CODE = 1;
    private CardView addFile;
    final static String CHANNEL_ID = "pushNot";



    String username;
    String ip;
    Handler handler;
    File route = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check if the app has permission to read and write external storage
        setContentView(R.layout.activity_main);
        createNotificationChannel();

        username = getIntent().getStringExtra("user");
        ip = getIntent().getStringExtra("ip");
        TextView wlcm = findViewById(R.id.Welcome);
        wlcm.setText("Hello " + username);
        TextView disttxt = (TextView) findViewById(R.id.dist);
        TextView speeddata = (TextView) findViewById(R.id.speed);
        TextView ele = (TextView) findViewById(R.id.elev);
        TextView tm = (TextView) findViewById(R.id.time);
        TextView rt = (TextView) findViewById(R.id.routeid);


        handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message message) {
                HashMap<String, Double> result = (HashMap<String, Double>) message.getData().getSerializable("results");
                if (result.containsKey("speed")) {
                    showNotification();
                    rt.setText("File : " + route.getName());
                    disttxt.setText("Distance : " + Double.toString(result.get("dist")) + " m");
                    speeddata.setText("Speed : " + Double.toString(result.get("speed")) + " m/s");
                    ele.setText("Elevation : " + Double.toString(result.get("ele")) + " m");
                    tm.setText("Time : " + Double.toString(result.get("sec")) + " s");
                } else {
                    Intent stat = new Intent(MainActivity.this, StatsActivity.class);
                    stat.putExtra("user", username);
                    stat.putExtra("result", result);
                    stat.putExtra("ip", ip);
                    startActivity(stat);
                    finish();

                }


                return true;
            }
        });

        Button backButton = findViewById(R.id.backBut);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start the ActivitySelectUser
                Intent selectuser = new Intent(MainActivity.this, RouteForge.class);
                selectuser.putExtra("ip", ip);
                startActivity(selectuser);
                finish();
            }
        });

        Button stats = findViewById(R.id.statsBut);
        stats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> datatosend = new HashMap<>();
                datatosend.put("user-stats", username);
                CommunicationThread userdata = new CommunicationThread(datatosend, handler, ip);
                userdata.start();
            }
        });

        addFile = (CardView) findViewById(R.id.AddFile);
        addFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPerm()) {
                    filePicker();
                }
            }
        });
    }

    private boolean checkPerm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                // Permission granted
                return true;
            } else {
                // Request the permission
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, PERMISSION_REQUEST_CODE);
                return false;
            }
        }
        return true;
    }




    private void filePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
    }

    static String read_file(File clientroute) {
        StringBuilder sb = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(clientroute))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

        } catch (IOException e) {
            System.out.println("File Error: " + e.getMessage());
        }

        return sb.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Toast.makeText(this, "Permission Successfully Granted", Toast.LENGTH_SHORT).show();
                    filePicker(); // Proceed with file picking after permission is granted
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == PICK_FILE_REQUEST_CODE) {
            // Handle file picking result
        }

        if (resultCode == Activity.RESULT_OK && requestCode == PICK_FILE_REQUEST_CODE) {
            Uri uri = data.getData();
            String filePath;
            try {
                filePath=PathUtil.getPath(this,uri);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            route = new File(filePath);
            System.out.println(username);
            System.out.println(Parser.extract_user(read_file(route)));
            if (!Parser.extract_user(read_file(route)).equals(username)) {
                Toast.makeText(this, "Please choose a file that belongs to " + username, Toast.LENGTH_SHORT).show();
                route = null;
            }
            if (route != null) {
                HashMap<String, String> datatosend = new HashMap<>();
                datatosend.put("user", username);
                datatosend.put(route.getName(), read_file(route));
                CommunicationThread userdata = new CommunicationThread(datatosend, handler, ip);
                userdata.start();
            }
        }
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.icon2)
                .setContentTitle("RouteForge")
                .setContentText("Gpx data processing is complete!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        int notificationId = 30;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(notificationId, builder.build());
    }
}
