package com.kazass.battlecity.input;

import android.view.MotionEvent;
import com.kazass.battlecity.entity.Direction;

public class TouchController {

    private Direction direction = null;
    private boolean firePressed = false;

    // Touch tracking for D-pad (pointer ID)
    private int dpadPointerId = -1;
    private float dpadOriginX, dpadOriginY;

    // Touch tracking for fire button
    private int firePointerId = -1;

    // Threshold in pixels to register a directional input
    private static final float DEAD_ZONE = 20f;

    private float fireZoneBoundary; // x pixel where fire zone starts

    public void setScreenWidth(int screenWidth) {
        fireZoneBoundary = screenWidth * 0.6f;
    }

    public void onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        int index = event.getActionIndex();
        int pointerId = event.getPointerId(index);
        float ex = event.getX(index);
        float ey = event.getY(index);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (ex < fireZoneBoundary) {
                    if (dpadPointerId == -1) {
                        dpadPointerId = pointerId;
                        dpadOriginX = ex;
                        dpadOriginY = ey;
                        direction = null;
                    }
                } else {
                    if (firePointerId == -1) {
                        firePointerId = pointerId;
                        firePressed = true;
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                for (int i = 0; i < event.getPointerCount(); i++) {
                    int pid = event.getPointerId(i);
                    if (pid == dpadPointerId) {
                        float dx = event.getX(i) - dpadOriginX;
                        float dy = event.getY(i) - dpadOriginY;
                        if (Math.abs(dx) < DEAD_ZONE && Math.abs(dy) < DEAD_ZONE) {
                            direction = null;
                        } else if (Math.abs(dx) > Math.abs(dy)) {
                            direction = dx > 0 ? Direction.RIGHT : Direction.LEFT;
                        } else {
                            direction = dy > 0 ? Direction.DOWN : Direction.UP;
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (pointerId == dpadPointerId) {
                    dpadPointerId = -1;
                    direction = null;
                }
                if (pointerId == firePointerId) {
                    firePointerId = -1;
                    firePressed = false;
                }
                break;

            case MotionEvent.ACTION_CANCEL:
                dpadPointerId = -1;
                firePointerId = -1;
                direction = null;
                firePressed = false;
                break;
        }
    }

    public Direction getDirection() { return direction; }
    public boolean isFirePressed()  { return firePressed; }
}
