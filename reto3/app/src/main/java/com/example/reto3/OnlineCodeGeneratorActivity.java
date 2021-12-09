package com.example.reto3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.atomic.AtomicBoolean;

public class OnlineCodeGeneratorActivity extends AppCompatActivity {

    static boolean isCodeMaker = true;
    static String code = "null";
    static boolean codeFound = false;
    static boolean checkTemp = true;
    static String keyValue = "null";


    private TextView headTV;
    private EditText codeEdt;
    private Button createCodeBtn;
    private Button joinCodeBtn;
    private ProgressBar loadingPB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_code_generator);
        headTV = findViewById(R.id.idTVHead);
        codeEdt = findViewById(R.id.idEdtCode);
        createCodeBtn = findViewById(R.id.idBtnCreate);
        joinCodeBtn = findViewById(R.id.idBtnJoin);
        loadingPB = findViewById(R.id.idPBLoading);

        createCodeBtn.setOnClickListener(mCreateBtnClickListener);
        joinCodeBtn.setOnClickListener(mJoinBtnClickListener);
    }

    private View.OnClickListener mCreateBtnClickListener = (event) -> {
        code = "null";
        codeFound = false;
        checkTemp = true;
        keyValue="null";

        code = codeEdt.getText().toString();

        createCodeBtn.setVisibility(View.GONE);
        joinCodeBtn.setVisibility(View.GONE);
        codeEdt.setVisibility(View.GONE);
        headTV.setVisibility(View.GONE);
        loadingPB.setVisibility(View.VISIBLE);

        if (!code.equals("null") && !code.equals("")){
            isCodeMaker = true;
            FirebaseDatabase.getInstance().getReference().child("codes").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean check = isValueAvailable(snapshot, code);
                    new Handler().postDelayed(() -> {
                        if (check) {
                            createCodeBtn.setVisibility(View.VISIBLE);
                            joinCodeBtn.setVisibility(View.VISIBLE);
                            codeEdt.setVisibility(View.VISIBLE);
                            headTV.setVisibility(View.VISIBLE);
                            loadingPB.setVisibility(View.GONE);
                        }else{
                            FirebaseDatabase.getInstance().getReference().child("codes").push().setValue(code);
                            isValueAvailable(snapshot, code);
                            checkTemp = false;
                            new Handler().postDelayed(() -> {
                                accepted();
                                Toast.makeText(OnlineCodeGeneratorActivity.this,"Please do not go back", Toast.LENGTH_SHORT).show();
                            },300);
                        }
                    },2000);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            createCodeBtn.setVisibility(View.VISIBLE);
            joinCodeBtn.setVisibility(View.VISIBLE);
            codeEdt.setVisibility(View.VISIBLE);
            headTV.setVisibility(View.VISIBLE);
            loadingPB.setVisibility(View.GONE);
            Toast.makeText(this, "Please enter a valid code",Toast.LENGTH_SHORT).show();
        }
    };

    private View.OnClickListener mJoinBtnClickListener = (event) -> {
        code = "null";
        codeFound = false;
        checkTemp = true;
        keyValue="null";

        code = codeEdt.getText().toString();

        if (!code.equals("null") && !code.equals("")){
            createCodeBtn.setVisibility(View.GONE);
            joinCodeBtn.setVisibility(View.GONE);
            codeEdt.setVisibility(View.GONE);
            headTV.setVisibility(View.GONE);
            loadingPB.setVisibility(View.VISIBLE);
            isCodeMaker=false;
            FirebaseDatabase.getInstance().getReference().child("codes").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean check = isValueAvailable(snapshot, code);
                    new Handler().postDelayed(() -> {
                        if (check) {
                            codeFound = true;
                            accepted();
                            createCodeBtn.setVisibility(View.VISIBLE);
                            joinCodeBtn.setVisibility(View.VISIBLE);
                            codeEdt.setVisibility(View.VISIBLE);
                            headTV.setVisibility(View.VISIBLE);
                            loadingPB.setVisibility(View.GONE);
                        }else{
                            createCodeBtn.setVisibility(View.VISIBLE);
                            joinCodeBtn.setVisibility(View.VISIBLE);
                            codeEdt.setVisibility(View.VISIBLE);
                            headTV.setVisibility(View.VISIBLE);
                            loadingPB.setVisibility(View.GONE);
                            Toast.makeText(OnlineCodeGeneratorActivity.this,"Invalid code", Toast.LENGTH_SHORT).show();
                        }
                    },2000);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }else{
            Toast.makeText(this, "Please enter a valid code",Toast.LENGTH_SHORT).show();
        }

    };

    private void accepted (){
        Intent intent = new Intent(this, OnlibeMultiPlayerGameActivity.class);
        startActivity(intent);
        createCodeBtn.setVisibility(View.VISIBLE);
        joinCodeBtn.setVisibility(View.VISIBLE);
        codeEdt.setVisibility(View.VISIBLE);
        headTV.setVisibility(View.VISIBLE);
        loadingPB.setVisibility(View.GONE);
    }

    private Boolean isValueAvailable(DataSnapshot snapshot, String code){
        Iterable<DataSnapshot> data = snapshot.getChildren();
        AtomicBoolean found = new AtomicBoolean(false);
        data.forEach(item -> {
            if (item.getValue().toString().equals(code)){
                keyValue = item.getKey().toString();
                found.set(true);
            }
        });
        if (found.get()) return true;
        return false;
    }
}