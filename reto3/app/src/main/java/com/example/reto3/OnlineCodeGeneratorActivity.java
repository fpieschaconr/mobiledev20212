package com.example.reto3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class OnlineCodeGeneratorActivity extends AppCompatActivity {

    private boolean isCodeMaker = true;
    private String code = "null";
    private boolean codeFound = false;
    private boolean checkTemp = true;
    private String keyValue = "null";


    private TextView headTV;
    private EditText codeEdt;
    private Button createCodeBtn;
    private ProgressBar loadingPB;
    private ListView gamesLV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_code_generator);
        headTV = findViewById(R.id.idTVHead);
        codeEdt = findViewById(R.id.idEdtCode);
        createCodeBtn = findViewById(R.id.idBtnCreate);
        loadingPB = findViewById(R.id.idPBLoading);
        gamesLV = findViewById(R.id.idLVGames);

        createCodeBtn.setOnClickListener(mCreateBtnClickListener);
        gamesLV.setOnItemClickListener(mJoinGameClickListener);

        List<String> list = new ArrayList<>();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
        gamesLV.setAdapter(arrayAdapter);
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference codesRef = rootRef.child("codes");
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    String code = ds.getValue(String.class);
                    list.add(code);
                }
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        codesRef.addListenerForSingleValueEvent(eventListener);
    }

    private View.OnClickListener mCreateBtnClickListener = (event) -> {
        code = "null";
        codeFound = false;
        checkTemp = true;
        keyValue="null";

        code = codeEdt.getText().toString();

        createCodeBtn.setVisibility(View.GONE);
        gamesLV.setVisibility(View.GONE);
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
                        if (!check) {
                            createCodeBtn.setVisibility(View.VISIBLE);
                            gamesLV.setVisibility(View.VISIBLE);
                            codeEdt.setVisibility(View.VISIBLE);
                            headTV.setVisibility(View.VISIBLE);
                            loadingPB.setVisibility(View.GONE);
                        }else{
                            FirebaseDatabase.getInstance().getReference().child("codes").push().setValue(code);
                            isValueAvailable(snapshot, code);
                            checkTemp = false;
                            new Handler().postDelayed(() -> {
                                accepted();
                                errorMsg("Please do not go back");
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
            gamesLV.setVisibility(View.VISIBLE);
            codeEdt.setVisibility(View.VISIBLE);
            headTV.setVisibility(View.VISIBLE);
            loadingPB.setVisibility(View.GONE);
            Toast.makeText(this, "Please enter a valid code",Toast.LENGTH_SHORT).show();
        }
    };

    private AdapterView.OnItemClickListener mJoinGameClickListener = (adapterView, view1, position, id) -> {
        code = "null";
        codeFound = false;
        checkTemp = true;
        keyValue="null";

        code = (String) adapterView.getItemAtPosition(position);

        if (!code.equals("null") && !code.equals("")){
            createCodeBtn.setVisibility(View.GONE);
            gamesLV.setVisibility(View.GONE);
            codeEdt.setVisibility(View.GONE);
            headTV.setVisibility(View.GONE);
            loadingPB.setVisibility(View.VISIBLE);
            isCodeMaker=false;
            FirebaseDatabase.getInstance().getReference().child("codes").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean check = isValueAvailable(snapshot, code);
                    new Handler().postDelayed(() -> {
                        if (!check) {
                            codeFound = true;
                            accepted();
                            createCodeBtn.setVisibility(View.VISIBLE);
                            gamesLV.setVisibility(View.VISIBLE);
                            codeEdt.setVisibility(View.VISIBLE);
                            headTV.setVisibility(View.VISIBLE);
                            loadingPB.setVisibility(View.GONE);
                        }else{
                            createCodeBtn.setVisibility(View.VISIBLE);
                            gamesLV.setVisibility(View.VISIBLE);
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

        }

    };

    private void accepted (){
        Intent intent = new Intent(this, OnlibeMultiPlayerGameActivity.class);
        //Create the bundle
        Bundle bundle = new Bundle();

//Add your data to bundle
        bundle.putBoolean("isCodeMaker", isCodeMaker);
        bundle.putString("code", code);
        bundle.putBoolean("codeFound", codeFound);
        bundle.putBoolean("checkTemp", checkTemp);
        bundle.putString("keyValue", keyValue);


//Add the bundle to the intent
        intent.putExtras(bundle);
        startActivity(intent);
        createCodeBtn.setVisibility(View.VISIBLE);
        gamesLV.setVisibility(View.VISIBLE);
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
        if (found.get()) return false;
        return true;
    }

    private void errorMsg(String value){
        Toast.makeText(this , value  , Toast.LENGTH_SHORT).show();
    }
}