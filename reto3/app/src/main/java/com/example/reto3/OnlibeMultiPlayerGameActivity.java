package com.example.reto3;

import static com.example.reto3.OnlineCodeGeneratorActivity.code;
import static com.example.reto3.OnlineCodeGeneratorActivity.isCodeMaker;
import static com.example.reto3.OnlineCodeGeneratorActivity.keyValue;

import static java.lang.System.exit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class OnlibeMultiPlayerGameActivity extends AppCompatActivity {

    private boolean isMyMove = isCodeMaker;

    private Button resetBtn;

    // Represents the internal state of the game
    private TicTacToeGame mGame;

    private BoardView mBoardView;

    // Buttons making up the board
    private Button[] mBoardButtons;
    // Various text displayed
    private TextView mInfoTextView;

    private TextView mWonTextView;
    private TextView mTiedTextView;
    private TextView mLostTextView;

    private int won;
    private int tied;
    private int lost;

    private boolean mGameOver;

    MediaPlayer mHumanMediaPlayer;
    MediaPlayer mComputerMediaPlayer;

    private boolean playerTurn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Restore the scores
        won = 0;
        lost = 0;
        tied = 0;
        setContentView(R.layout.activity_onlibe_multi_player_game);
        mInfoTextView = (TextView) findViewById(R.id.information);
        mWonTextView = (TextView) findViewById(R.id.won);
        mTiedTextView = (TextView) findViewById(R.id.tied);
        mLostTextView = (TextView) findViewById(R.id.lost);
        mGame = new TicTacToeGame();
        mBoardView = (BoardView) findViewById(R.id.board);
        resetBtn = findViewById(R.id.idBtnReset);
        resetBtn.setOnClickListener(mResetBtnClickListener);
        mBoardView.setGame(mGame);
        // Listen for touches on the board
        mBoardView.setOnTouchListener(mTouchListener);
        displayScores();

        FirebaseDatabase.getInstance().getReference().child("data").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Object data = snapshot.getValue();
                if (isMyMove){
                    isMyMove=false;
                }else{
                    isMyMove = true;
                }
                moveOnline(data.toString(), isMyMove);
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

    private void moveOnline(String data, Boolean isMyMove){
        if (isMyMove) {
            setMove(TicTacToeGame.COMPUTER_PLAYER, Integer.parseInt(data));

        }else{
            setMove(TicTacToeGame.HUMAN_PLAYER, Integer.parseInt(data));
        }

    }

    private View.OnClickListener mResetBtnClickListener = (event) -> {
        mGame.clearBoard();
        mBoardView.invalidate(); // Redraw the board

        displayScores();

        mGameOver = false;

        isMyMove = isCodeMaker;
        if (isMyMove){
            FirebaseDatabase.getInstance().getReference().child("data").child(code).removeValue();
        }
        playerTurn = isMyMove;
        if (playerTurn) {
            mInfoTextView.setText(R.string.turn_human);
        }else{
            mInfoTextView.setText(R.string.turn_remote);
        }

    };

    private void reset (){
        mGame.clearBoard();
        mBoardView.invalidate(); // Redraw the board

        displayScores();

        mGameOver = false;

        isMyMove = isCodeMaker;
        if (isMyMove){
            FirebaseDatabase.getInstance().getReference().child("data").child(code).removeValue();
        }
        playerTurn = isMyMove;
        if (playerTurn) {
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

    private void displayScores() {
        mWonTextView.setText(getString(R.string.counter_won, won));
        mTiedTextView.setText(getString(R.string.counter_tied, tied));
        mLostTextView.setText(getString(R.string.counter_lost, lost));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mGame.setBoardState(savedInstanceState.getCharArray("board"));
        mGameOver = savedInstanceState.getBoolean("mGameOver");
        mInfoTextView.setText(savedInstanceState.getCharSequence("info"));
        playerTurn = savedInstanceState.getBoolean("playerTurn");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharArray("board", mGame.getBoardState());
        outState.putBoolean("mGameOver", mGameOver);
        outState.putCharSequence("info", mInfoTextView.getText());
        outState.putBoolean("playerTurn", playerTurn);
    }

    private boolean setMove(char player, int location) {
        if (mGame.setMove(player, location)) {
            if (player == TicTacToeGame.HUMAN_PLAYER) {
                if(mHumanMediaPlayer!=null) mHumanMediaPlayer.start(); // Play the sound effect
            } else {
                if(mComputerMediaPlayer!=null) mComputerMediaPlayer.start(); // Play the sound effect
            }
            mBoardView.invalidate(); // Redraw the board
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
            if (playerTurn){
                if (!mGameOver && setMove(TicTacToeGame.HUMAN_PLAYER, pos)){
                    // If no winner yet, let the computer make a move
                    playerTurn = false;
                    mInfoTextView.setText(R.string.turn_remote);
                    Handler handler = new Handler();
                    handler.postDelayed(() -> {
                        int winner = mGame.checkForWinner();
                        if (winner == 0) {
                            mInfoTextView.setText(R.string.turn_remote);
                            int move = mGame.getComputerMove();
                            setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                            winner = mGame.checkForWinner();
                        }
                        if (winner == 0){
                            playerTurn = true;
                            mInfoTextView.setText(R.string.turn_human);}
                        else if (winner == 1){
                            mInfoTextView.setText(R.string.result_tie);
                            tied++;
                            mTiedTextView.setText(getString(R.string.counter_tied, tied));
                            mGameOver = true;
                        }
                        else if (winner == 2){
                            mInfoTextView.setText(R.string.result_human_wins);
                            won++;
                            mWonTextView.setText(getString(R.string.counter_won, won));
                            mGameOver = true;
                        }
                        else{
                            mInfoTextView.setText(R.string.result_computer_wins);
                            lost++;
                            mLostTextView.setText(getString(R.string.counter_lost, lost));
                            mGameOver = true;
                        }
                    }, 1000);

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