package com.landrykole.platformjumpgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameView extends SurfaceView implements Runnable, SurfaceHolder.Callback {
    private Thread gameThread;
    private boolean isPlaying = true;
    private Player player;
    private List<Platform> platforms = new ArrayList<>();
    private boolean isGameOver = false;
    private DatabaseHelper dbHelper;
    private int playerScore = 0;
    private long lastTime = System.currentTimeMillis();


    public GameView(Context context) {
        super(context);
        player = new Player();
        platforms = new ArrayList<>();
        // Add a platform for demonstration purposes
        platforms.add(new Platform(500, 600));
        dbHelper = new DatabaseHelper(context);
        resume();
        getHolder().addCallback(this);
    }

    @Override
    public void run() {
        while (isPlaying) {
            update();
            draw();
            sleep();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        resume();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Handle surface changes if necessary
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        pause();
    }


    private void update() {
        player.update();
        Iterator<Platform> iterator = platforms.iterator();

        while (iterator.hasNext()) {
            Platform platform = iterator.next();
            platform.update();
            if (player.collidesWith(platform) && player.isAbove(platform)) {
                player.y = platform.y - 100; // Position player on top of platform
                player.velocityY = 0; // Stop downward motion
            }
            // Remove platform if it's off the screen
            if (platform.x + 200 < 0) {
                iterator.remove();
            }
        }

        for (Platform platform : platforms) {
            platform.update();

            if (player.collidesWith(platform)) {
                player.y = platform.y - 100;
                player.velocityY = 0;
            }
        }

        // Generate new platforms periodically
        if (Math.random() < 0.02) { // 2% chance very frame to spawn a platform
            float yPosition = (float) (400 + Math.random() * 200); // Random height between 400 and 600
            platforms.add(new Platform(getWidth(), yPosition));
        }

        // Handle game over if player falls off the screen
        if (player.y > getHeight()) {
            gameOver();
        }

        // Increase score over time
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTime > 1000) {
            playerScore += 1;
            lastTime = currentTime;
        }
    }

    private void draw() {
        if (getHolder().getSurface().isValid()) {
            Canvas canvas = getHolder().lockCanvas();

            // Clear screen
            canvas.drawColor(Color.WHITE);

            // Draw the top score
            Paint topScorePaint = new Paint();
            topScorePaint.setTextSize(50);
            topScorePaint.setColor(Color.BLACK);
            String topScoreText = "Top Score: " + dbHelper.getTopScore();
            canvas.drawText(topScoreText, 20, 60, topScorePaint);

            // Draw the current score
            Paint currentScorePaint = new Paint();
            currentScorePaint.setTextSize(50);
            currentScorePaint.setColor(Color.BLACK);
            String currentScoreText = "Score: " + playerScore;
            canvas.drawText(currentScoreText, 790, 60, currentScorePaint);

            // Load the player image using Bitmap
            Bitmap playerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.madelinepixelart);

            // Draw the Bitmap on the canvas at the player's position
            canvas.drawBitmap(playerBitmap, player.x - 100, player.y - 150, null);

            // Draw platforms
            Paint platformPaint = new Paint(Color.GREEN);
            for (Platform platform : platforms) {
                canvas.drawRect(platform.x, platform.y, platform.x + 200, platform.y + 50, platformPaint);
            }

            // Draw Game Over text
            if (isGameOver) {
                Paint gameOverPaint = new Paint();
                gameOverPaint.setTextSize(50);
                gameOverPaint.setColor(Color.BLACK);
                canvas.drawText("Game Over! Tap to restart.", getWidth() / 2 - 250, getHeight() / 2, gameOverPaint);
                String finalScoreText = "Final Score: " + playerScore;
                canvas.drawText(finalScoreText, getWidth() / 2 - 150, getHeight() / 2 + 100, gameOverPaint);
            }

            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    private void sleep() {
        try {
            Thread.sleep(17); // Cap the game loop to roughly 60 frames per second
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void pause() {
        try {
            isPlaying = false;
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (isGameOver) {
                restartGame();
            } else {
                player.jump();
                // If the game isn't running yet, start it.
                if (!isPlaying) {
                    isPlaying = true;
                }
            }
        }
        return true;
    }

    private void restartGame() {
        player = new Player();
        platforms.clear();
        platforms.add(new Platform(500, 600));
        isGameOver = false;
        isPlaying = true;
        resume();
    }


    private void gameOver() {
        isPlaying = false;
        isGameOver = true;

        // Save player score
        int currentScore = playerScore;
        dbHelper.saveScore(currentScore);
    }

    public class Player {
        public float x = 100, y = 500;
        public float velocityY = 0;
        private static final float GRAVITY = 1f;
        private static final float JUMP_STRENGTH = -15f;

        public void update() {
            y += velocityY;
            velocityY += GRAVITY;

            // TODO: Add collision detection with ground or platforms here
        }

        public void jump() {
            velocityY = JUMP_STRENGTH;
        }

        public boolean collidesWith(Platform platform) {
            return x < platform.x + 200 &&
                    x + 100 > platform.x &&
                    y + 100 > platform.y &&
                    y + 100 < platform.y + 50;
        }

        public boolean isAbove(Platform platform) {
            return x + 100 > platform.x &&
                    x < platform.x + 200 &&
                    y + 100 <= platform.y;
        }
    }

    public class Platform {
        public float x, y;
        private static final float SPEED = 5f;

        public Platform(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public void update() {
            x -= SPEED;
        }
    }

}
