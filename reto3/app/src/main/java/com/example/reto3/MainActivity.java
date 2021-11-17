package com.example.reto3;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
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

public class MainActivity extends AppCompatActivity {

    static final int DIALOG_DIFFICULTY_ID = 0;
    static final int DIALOG_QUIT_ID = 1;
    static final int DIALOG_ABOUT_ID = 2;

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
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_game:
                startNewGame();
                return true;
            case R.id.ai_difficulty:
                createdDialog(DIALOG_DIFFICULTY_ID).show();
                return true;
            case R.id.about:
                createdDialog(DIALOG_ABOUT_ID).show();
                return true;
            case R.id.quit:
                createdDialog(DIALOG_QUIT_ID).show();
                return true;
        }
        return false;
    }


    protected Dialog createdDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch(id) {
            case DIALOG_DIFFICULTY_ID:
                builder.setTitle(R.string.difficulty_choose);
                final CharSequence[] levels = {
                        getResources().getString(R.string.difficulty_easy),
                        getResources().getString(R.string.difficulty_harder),
                        getResources().getString(R.string.difficulty_expert)};
// TODO: Set selected, an integer (0 to n-1), for the Difficulty dialog.
// selected is the radio button that should be selected.
                int selected = mGame.getDifficultyLevel().getValue();
                builder.setSingleChoiceItems(levels, selected,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                dialog.dismiss(); // Close dialog
// TODO: Set the diff level of mGame based on which item was selected.
// Display the selected difficulty level
                                mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.of(item));
                                Toast.makeText(getApplicationContext(), levels[item],

                                        Toast.LENGTH_SHORT).show();

                            }
                        });
                dialog = builder.create();
                break;

            case DIALOG_QUIT_ID:
// Create the quit confirmation dialog
                builder.setMessage(R.string.quit_question)
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                MainActivity.this.finish();
                            }
                        })
                        .setNegativeButton(R.string.no, null);
                dialog = builder.create();
                break;
            case DIALOG_ABOUT_ID:
                Context context = getApplicationContext();
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.about_dialog, null);
                builder.setView(layout);
                builder.setPositiveButton("OK", null);
                dialog = builder.create();
                break;
        }
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mInfoTextView = (TextView) findViewById(R.id.information);
        mWonTextView = (TextView) findViewById(R.id.won);
        mTiedTextView = (TextView) findViewById(R.id.tied);
        mLostTextView = (TextView) findViewById(R.id.lost);
        won = 0;
        tied = 0;
        lost = 0;
        mGame = new TicTacToeGame();
        mBoardView = (BoardView) findViewById(R.id.board);
        mBoardView.setGame(mGame);
        // Listen for touches on the board
        mBoardView.setOnTouchListener(mTouchListener);
        startNewGame();
    }

    // Set up the game board.
    private void startNewGame() {
        mGame.clearBoard();
        mBoardView.invalidate(); // Redraw the board

        mWonTextView.setText(getString(R.string.counter_won, won));
        mTiedTextView.setText(getString(R.string.counter_tied, tied));
        mLostTextView.setText(getString(R.string.counter_lost, lost));

        mGameOver = false;

        if(!mGame.firstTurn()) {
            playerTurn = false;
            mInfoTextView.setText(R.string.turn_computer);
            int move = mGame.getComputerMove();
            setMove(TicTacToeGame.COMPUTER_PLAYER, move);
        }
        playerTurn = true;
        mInfoTextView.setText(R.string.turn_human);
    } // End of startNewGame

    private boolean setMove(char player, int location) {
        if (mGame.setMove(player, location)) {
            if (player == TicTacToeGame.HUMAN_PLAYER) {
                mHumanMediaPlayer.start(); // Play the sound effect
            } else {
                mComputerMediaPlayer.start(); // Play the sound effect
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
                    mInfoTextView.setText(R.string.turn_computer);
                    Handler handler = new Handler();
                    handler.postDelayed(() -> {
                        int winner = mGame.checkForWinner();
                        if (winner == 0) {
                            mInfoTextView.setText(R.string.turn_computer);
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
        mHumanMediaPlayer.setPlaybackParams(mHumanMediaPlayer.getPlaybackParams().setSpeed(4.5f));
        mComputerMediaPlayer.setPlaybackParams(mComputerMediaPlayer.getPlaybackParams().setSpeed(2.5f));
    }
    @Override
    protected void onPause() {
        super.onPause();
        mHumanMediaPlayer.release();
        mComputerMediaPlayer.release();
    }

}