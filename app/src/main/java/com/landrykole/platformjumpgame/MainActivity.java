package com.landrykole.platformjumpgame;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private GameView gameView;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseHelper = new DatabaseHelper(this);

        gameView = new GameView(this);
        setContentView(gameView);
    }

    protected void onResume() {
        super.onResume();
        gameView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }

    public void storeHighScore(int score) {
        databaseHelper.saveScore(score);
    }

    public int retrieveHighScore() {
        return databaseHelper.getTopScore();
    }
}