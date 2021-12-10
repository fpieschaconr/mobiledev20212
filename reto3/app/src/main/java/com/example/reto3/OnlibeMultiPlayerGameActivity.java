package com.example.reto3;

import static com.example.reto3.TicTacToeGame.OPEN_SPOT;

import static java.lang.System.exit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class OnlibeMultiPlayerGameActivity extends AppCompatActivity {

    private boolean isCodeMaker = true;
    private String code = "null";
    private String keyValue = "null";

    private boolean isMyMove = isCodeMaker;

    private Button resetBtn;

    // Represents the internal state of the game
    private TicTacToeGame mGame;

    private BoardView mBoardView;

    // Buttons making up the board
    private Button[] mBoardButtons;
    // Various text displayed
    private TextView mInfoTextView;
    private TextView mRoom;


    private boolean mGameOver;

    MediaPlayer mHumanMediaPlayer;
    MediaPlayer mComputerMediaPlayer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onlibe_multi_player_game);
        //Get the bundle
        Bundle bundle = getIntent().getExtras();

//Extract the data…
        isCodeMaker = bundle.getBoolean("isCodeMaker");
        code = bundle.getString("code");
        keyValue = bundle.getString("keyValue");

        isMyMove = isCodeMaker;

        mInfoTextView = findViewById(R.id.information);
        if (isMyMove) {
            mInfoTextView.setText(R.string.turn_human);
        }else{
            mInfoTextView.setText(R.string.turn_remote);
        }
        mRoom = findViewById(R.id.idTVRoom);
        String room = "Room ID: " + code;
        mRoom.setText(room);
        mGame = new TicTacToeGame();
        mBoardView = findViewById(R.id.board);
        resetBtn = findViewById(R.id.idBtnReset);
        resetBtn.setOnClickListener(mResetBtnClickListener);
        mBoardView.setGame(mGame);
        // Listen for touches on the board
        mBoardView.setOnTouchListener(mTouchListener);

        mGameOver = false;

        FirebaseDatabase.getInstance().getReference().child("data").child(code).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Object data =  snapshot.getValue();
                isMyMove= !isMyMove;
                if (isMyMove) {
                    mInfoTextView.setText(R.string.turn_human);
                }else{
                    mInfoTextView.setText(R.string.turn_remote);
                }
                moveOnline(Integer.parseInt(data.toString()), isMyMove);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                reset();
                errorMsg("Game Reset");
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void moveOnline(int data, Boolean isMyMove){
        if (isMyMove) {
            setMove(TicTacToeGame.COMPUTER_PLAYER, data);

        }else{
            setMove(TicTacToeGame.HUMAN_PLAYER, data);
        }

    }

    private View.OnClickListener mResetBtnClickListener = (event) -> {
        reset();
    };

    private void reset (){
        mGame.clearBoard();
        mBoardView.invalidate(); // Redraw the board

        mGameOver = false;

        isMyMove = isCodeMaker;
        if (isMyMove) {
            FirebaseDatabase.getInstance().getReference().child("data").child(code).removeValue();
            mInfoTextView.setText(R.string.turn_human);
        }else{
            mInfoTextView.setText(R.string.turn_remote);
        }
    }

    private void removeCode(){
        if (isCodeMaker){
            FirebaseDatabase.getInstance().getReference().child("codes").child(keyValue).removeValue();
        }
    }

    private void errorMsg(String value){
        Toast.makeText(this , value  , Toast.LENGTH_SHORT).show();
    }

    private void disableReset (){
        resetBtn.setEnabled(false);
        new Handler().postDelayed(() -> resetBtn.setEnabled(true),2000);
    }

    @Override
    public void onBackPressed(){
        removeCode();
        if (isCodeMaker){
            FirebaseDatabase.getInstance().getReference().child("data").child(code).removeValue();
        }
        exit(0);
    }

    private void updateDatabase (int cellId){
        FirebaseDatabase.getInstance().getReference().child("data").child(code).push().setValue(cellId);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mGame.setBoardState(savedInstanceState.getCharArray("board"));
        mGameOver = savedInstanceState.getBoolean("mGameOver");
        mInfoTextView.setText(savedInstanceState.getCharSequence("info"));
        isMyMove = savedInstanceState.getBoolean("isMyMove");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharArray("board", mGame.getBoardState());
        outState.putBoolean("mGameOver", mGameOver);
        outState.putCharSequence("info", mInfoTextView.getText());
        outState.putBoolean("isMyMove", isMyMove);
    }

    private boolean setMove(char player, int location) {
        if (mGame.setMove(player, location)) {
            if (player == TicTacToeGame.HUMAN_PLAYER) {
                if(mHumanMediaPlayer!=null) mHumanMediaPlayer.start(); // Play the sound effect
            } else {
                if(mComputerMediaPlayer!=null) mComputerMediaPlayer.start(); // Play the sound effect
            }
            mBoardView.invalidate(); // Redraw the board
            int winner = mGame.checkForWinner();
            if (winner == 1){
                mInfoTextView.setText(R.string.result_tie);
                mGameOver = true;
            }
            else if (winner == 2){
                mInfoTextView.setText(R.string.result_human_wins);
                mGameOver = true;
            }
            else if (winner == 3){
                mInfoTextView.setText(R.string.result_remote_wins);
                mGameOver = true;
            }
            return true;
        }
        return false;
    }

    // Listen for touches on the board
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
// Determine which cell was touched
            int col = (int) event.getX() / mBoardView.getBoardCellWidth();
            int row = (int) event.getY() / mBoardView.getBoardCellHeight();
            int pos = row * 3 + col;
            if (isMyMove && !mGameOver){
                if (mGame.getBoardOccupant(pos)==OPEN_SPOT){
                    updateDatabase(pos);
                }
            }
// So we aren't notified of continued events when finger is moved
            return false;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mHumanMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.human);
        mComputerMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.computer);
    }
    @Override
    protected void onPause() {
        super.onPause();
        mHumanMediaPlayer.release();
        mComputerMediaPlayer.release();
    }
}