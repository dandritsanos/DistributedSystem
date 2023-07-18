package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity2 extends AppCompatActivity {
    EditText input;
    Button btn;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        input = (EditText) findViewById(R.id.input);
        btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = input.getText().toString();
                openApp(ip);
            }
        });
    }

    private void openApp(String ip) {
        if (input != null) {
            Intent intent = new Intent(this, RouteForge.class);
            intent.putExtra("ip", ip);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Please insert a server ip", Toast.LENGTH_SHORT).show();
        }
    }
}