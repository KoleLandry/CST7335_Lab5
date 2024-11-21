package com.landrykole.platformjumpgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameView extends SurfaceView implements Runnable {
    private Thread gameThread;
    private boolean isPlaying;
    private Player player;
    private List<Platform> platforms;
    private boolean isGameOver = false;


    public GameView(Context context) {
        super(context);
        player = new Player();
        platforms = new ArrayList<>();
        // Add a platform for demonstration purposes
        platforms.add(new Platform(500, 600));
    }

    @Override
    public void run() {
        while (isPlaying) {
            update();
            draw();
            sleep();
        }
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

        // Generate new platforms periodically
        if (Math.random() < 0.02) { // 2% chance very frame to spawn a platform
            float yPosition = (float) (400 + Math.random() * 200); // Random height between 400 and 600
            platforms.add(new Platform(getWidth(), yPosition));
        }

        // Handle game over if player falls off the screen
        if (player.y > getHeight()) {
            gameOver();
        }
    }

    private void draw() {
        if (getHolder().getSurface().isValid()) {
            Canvas canvas = getHolder().lockCanvas();

            // Clear screen
            canvas.drawColor(Color.WHITE);

            // Draw player (as a rectangle for this example)
            canvas.drawRect(player.x, player.y, player.x + 100, player.y + 100, new Paint(Color.RED));

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
        isPlaying = true;
        isGameOver = false;
    }


    private void gameOver() {
        isPlaying = false;
        isGameOver = true;
    }

}
