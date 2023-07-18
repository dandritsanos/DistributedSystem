package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;



import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class RouteForge extends AppCompatActivity  {
    private CardView user1, user2, user3;
    String ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_user);
        ip = getIntent().getStringExtra("ip");
        user1= (CardView)findViewById(R.id.User1);
        user2=(CardView)findViewById(R.id.User2);
        user3=(CardView)findViewById(R.id.User3);


        user1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUser("user1");
            }
        });
        user2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUser("user2");
            }
        });
        user3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUser("user3");
            }
        });

    }

    private void openUser(String user) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("ip", ip);
        intent.putExtra("user", user);
        startActivity(intent);
        finish();

    }
}
