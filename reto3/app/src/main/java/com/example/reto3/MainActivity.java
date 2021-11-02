package com.example.reto3;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // Represents the internal state of the game
    private TicTacToeGame mGame;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add("New Game");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        startNewGame();
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBoardButtons = new Button[TicTacToeGame.BOARD_SIZE];
        mBoardButtons[0] = (Button) findViewById(R.id.one);
        mBoardButtons[1] = (Button) findViewById(R.id.two);
        mBoardButtons[2] = (Button) findViewById(R.id.three);
        mBoardButtons[3] = (Button) findViewById(R.id.four);
        mBoardButtons[4] = (Button) findViewById(R.id.five);
        mBoardButtons[5] = (Button) findViewById(R.id.six);
        mBoardButtons[6] = (Button) findViewById(R.id.seven);
        mBoardButtons[7] = (Button) findViewById(R.id.eight);
        mBoardButtons[8] = (Button) findViewById(R.id.nine);
        mInfoTextView = (TextView) findViewById(R.id.information);
        mWonTextView = (TextView) findViewById(R.id.won);
        mTiedTextView = (TextView) findViewById(R.id.tied);
        mLostTextView = (TextView) findViewById(R.id.lost);
        won = 0;
        tied = 0;
        lost = 0;
        mGame = new TicTacToeGame();
        startNewGame();
    }

    // Set up the game board.
    private void startNewGame() {
        mGame.clearBoard();

        // Reset all buttons
        for (int i = 0; i < mBoardButtons.length; i++) {
            mBoardButtons[i].setText("");
            mBoardButtons[i].setEnabled(true);
            mBoardButtons[i].setOnClickListener(new ButtonClickListener(i));
        }

        mWonTextView.setText(getString(R.string.counter_won, won));
        mTiedTextView.setText(getString(R.string.counter_tied, tied));
        mLostTextView.setText(getString(R.string.counter_lost, lost));

        mGameOver = false;

        if(!mGame.firstTurn()) {
            mInfoTextView.setText(R.string.turn_computer);
            int move = mGame.getComputerMove();
            setMove(TicTacToeGame.COMPUTER_PLAYER, move);
        }
        mInfoTextView.setText(R.string.first_human);
    } // End of startNewGame

    private void setMove(char player, int location) {
        mGame.setMove(player, location);
        mBoardButtons[location].setEnabled(false);
        mBoardButtons[location].setText(String.valueOf(player));
        if (player == TicTacToeGame.HUMAN_PLAYER)
            mBoardButtons[location].setTextColor(Color.rgb(0, 200, 0));
        else
            mBoardButtons[location].setTextColor(Color.rgb(200, 0, 0));
    }

    // Handles clicks on the game board buttons
    private class ButtonClickListener implements View.OnClickListener {
        int location;
        public ButtonClickListener(int location) {
            this.location = location;
        }
        public void onClick(View view) {
            if (mBoardButtons[location].isEnabled()&&!mGameOver) {
                setMove(TicTacToeGame.HUMAN_PLAYER, location);
// If no winner yet, let the computer make a move
                int winner = mGame.checkForWinner();
                if (winner == 0) {
                    mInfoTextView.setText(R.string.turn_computer);
                    int move = mGame.getComputerMove();
                    setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                    winner = mGame.checkForWinner();
                }
                if (winner == 0){
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
            }
        }
    }
}