package com.example.reto3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MultiPlayerGameSelectionActivity extends AppCompatActivity {
    private Button onlineBtn;
    private Button offlineBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_player_game_selection);
        onlineBtn = findViewById(R.id.idBtnOnline);
        offlineBtn = findViewById(R.id.idBtnOffline);
        onlineBtn.setOnClickListener(mOnlineBtnClickListener);
        offlineBtn.setOnClickListener(mOfflineBtnClickListener);
    }

    private View.OnClickListener mOnlineBtnClickListener = (event) -> {
        MainActivity.singlePlayer = false;
        Intent intent = new Intent(this, OnlineCodeGeneratorActivity.class);
        startActivity(intent);
    };

    private View.OnClickListener mOfflineBtnClickListener = (event) -> {
        MainActivity.singlePlayer = true;
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    };

}