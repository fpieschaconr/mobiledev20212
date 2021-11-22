package com.example.reto3;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TicTacToeGame {

    // Characters used to represent the human, computer, and open spots
    public static final char HUMAN_PLAYER = 'X';
    public static final char COMPUTER_PLAYER = 'O';
    public static final char OPEN_SPOT = ' ';

    private char[] mBoard = {OPEN_SPOT, OPEN_SPOT, OPEN_SPOT, OPEN_SPOT, OPEN_SPOT, OPEN_SPOT, OPEN_SPOT, OPEN_SPOT, OPEN_SPOT};
    public static final int BOARD_SIZE = 9;

    // The computer's difficulty levels
    public enum DifficultyLevel {
        Easy(0), Harder(1), Expert(2);


        private static final Map<Integer, DifficultyLevel> map = new HashMap<>(values().length, 1);

        static {
            for (DifficultyLevel c : values()) map.put(c.value, c);
        }

        private final int value;

        DifficultyLevel(int value) {
            this.value = value;
        }

        public static DifficultyLevel of(int value) {
            DifficultyLevel result = map.get(value);
            if (result == null) {
                throw new IllegalArgumentException("Invalid index: " + value);
            }
            return result;
        }

        public int getValue() {
            return value;
        }

    }
    // Current difficulty level
    private DifficultyLevel mDifficultyLevel = DifficultyLevel.Expert;

    private Random mRand;

    public TicTacToeGame() {

        // Seed the random number generator
        mRand = new Random();

    }

    public DifficultyLevel getDifficultyLevel() {
        return mDifficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        mDifficultyLevel = difficultyLevel;
    }

    public boolean firstTurn(){
        return mRand.nextBoolean();
    }

    /** Clear the board of all X's and O's by setting all spots to OPEN_SPOT. */
    public void clearBoard(){
        mBoard = new char[]{OPEN_SPOT, OPEN_SPOT, OPEN_SPOT, OPEN_SPOT, OPEN_SPOT, OPEN_SPOT, OPEN_SPOT, OPEN_SPOT, OPEN_SPOT};
    }
    /** Set the given player at the given location on the game board.
     * The location must be available, or the board will not be changed.
     *
     * @param player - The HUMAN_PLAYER or COMPUTER_PLAYER
     * @param location - The location (0-8) to place the move
     */
    public boolean setMove(char player, int location){
        if (mBoard[location]==OPEN_SPOT){
            mBoard[location] = player;
            return true;
        }
        return false;
    }
    /** Return the best move for the computer to make. You must call setMove()
     * to actually make the computer move to that location.
     * @return The best move for the computer to make (0-8).
     */
    public int getRandomMove(){
        int move;

        // Generate random move
        do
        {
            move = mRand.nextInt(BOARD_SIZE);
        } while (mBoard[move] == HUMAN_PLAYER || mBoard[move] == COMPUTER_PLAYER);

        return move;
    }
    public int getWinningMove(){
        int move = -1;

        // First see if there's a move O can make to win
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (mBoard[i] != HUMAN_PLAYER && mBoard[i] != COMPUTER_PLAYER) {
                char curr = mBoard[i];
                mBoard[i] = COMPUTER_PLAYER;
                if (checkForWinner() == 3) {
                    mBoard[i] = curr;
                    return i;
                }
                mBoard[i] = curr;
            }
        }

        return move;
    }
    public int getBlockingMove(){
        int move = -1;

        // See if there's a move O can make to block X from winning
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (mBoard[i] != HUMAN_PLAYER && mBoard[i] != COMPUTER_PLAYER) {
                char curr = mBoard[i];   // Save the current number
                mBoard[i] = HUMAN_PLAYER;
                if (checkForWinner() == 2) {
                    mBoard[i] = curr;
                    return i;
                }
                mBoard[i] = curr;
            }
        }

        return move;
    }

    public int getComputerMove(){
        int move = -1;
        if (mDifficultyLevel == DifficultyLevel.Easy)
            move = getRandomMove();
        else if (mDifficultyLevel == DifficultyLevel.Harder) {
            move = getWinningMove();
            if (move == -1)
                move = getRandomMove();
        }
        else if (mDifficultyLevel == DifficultyLevel.Expert) {
// Try to win, but if that's not possible, block.
// If that's not possible, move anywhere.
            move = getWinningMove();
            if (move == -1)
                move = getBlockingMove();
            if (move == -1)
                move = getRandomMove();
        }
        return move;
    }
    /**
     * Check for a winner and return a status value indicating who has won.
     * @return Return 0 if no winner or tie yet, 1 if it's a tie, 2 if X won,
     * or 3 if O won.
     */
    public int checkForWinner(){
        // Check horizontal wins
        for (int i = 0; i <= 6; i += 3)	{
            if (mBoard[i] == HUMAN_PLAYER &&
                    mBoard[i+1] == HUMAN_PLAYER &&
                    mBoard[i+2]== HUMAN_PLAYER)
                return 2;
            if (mBoard[i] == COMPUTER_PLAYER &&
                    mBoard[i+1]== COMPUTER_PLAYER &&
                    mBoard[i+2] == COMPUTER_PLAYER)
                return 3;
        }

        // Check vertical wins
        for (int i = 0; i <= 2; i++) {
            if (mBoard[i] == HUMAN_PLAYER &&
                    mBoard[i+3] == HUMAN_PLAYER &&
                    mBoard[i+6]== HUMAN_PLAYER)
                return 2;
            if (mBoard[i] == COMPUTER_PLAYER &&
                    mBoard[i+3] == COMPUTER_PLAYER &&
                    mBoard[i+6]== COMPUTER_PLAYER)
                return 3;
        }

        // Check for diagonal wins
        if ((mBoard[0] == HUMAN_PLAYER &&
                mBoard[4] == HUMAN_PLAYER &&
                mBoard[8] == HUMAN_PLAYER) ||
                (mBoard[2] == HUMAN_PLAYER &&
                        mBoard[4] == HUMAN_PLAYER &&
                        mBoard[6] == HUMAN_PLAYER))
            return 2;
        if ((mBoard[0] == COMPUTER_PLAYER &&
                mBoard[4] == COMPUTER_PLAYER &&
                mBoard[8] == COMPUTER_PLAYER) ||
                (mBoard[2] == COMPUTER_PLAYER &&
                        mBoard[4] == COMPUTER_PLAYER &&
                        mBoard[6] == COMPUTER_PLAYER))
            return 3;

        // Check for tie
        for (int i = 0; i < BOARD_SIZE; i++) {
            // If we find a number, then no one has won yet
            if (mBoard[i] != HUMAN_PLAYER && mBoard[i] != COMPUTER_PLAYER)
                return 0;
        }

        // If we make it through the previous loop, all places are taken, so it's a tie
        return 1;
    }

    public char getBoardOccupant(int i){
        return mBoard[i];
    }

    public char[] getBoardState() {
        return mBoard;
    }

    public void setBoardState(char[] mBoard) {
        this.mBoard = mBoard;
    }

}