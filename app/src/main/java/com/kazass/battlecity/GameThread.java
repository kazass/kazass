package com.kazass.battlecity;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class GameThread extends Thread {

    private final SurfaceHolder holder;
    private final GameView gameView;
    private volatile boolean running = false;

    public GameThread(SurfaceHolder holder, GameView gameView) {
        this.holder = holder;
        this.gameView = gameView;
        setName("BattleCityGameThread");
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public void run() {
        while (running) {
            long frameStart = System.nanoTime();
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    synchronized (holder) {
                        long now = System.currentTimeMillis();
                        gameView.update(now);
                        gameView.render(canvas);
                    }
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }

            long elapsed = (System.nanoTime() - frameStart) / 1_000_000L;
            long sleep = Constants.FRAME_MS - elapsed;
            if (sleep > 0) {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException ignored) {}
            }
        }
    }
}
