/**
 * Copyright 2013 Nils Assbeck, Guersel Ayaz and Michael Zoech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.remoteyourcam.usb;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

public class GestureDetector {

    public interface GestureHandler {
        void onLongTouch(float posx, float posy);

        void onPinchZoom(float pX, float pY, float distInPixel);

        void onTouchMove(float dx, float dy);

        void onFling(float velx, float vely);

        void onStopFling();
    }

    private static class TouchInfo {
        public int id;
        public float startX;
        public float startY;
        public boolean moved;
        public float lastX;
        public float lastY;
        public float currentX;
        public float currentY;

        public TouchInfo(int id, float x, float y) {
            this.id = id;
            this.startX = this.lastX = this.currentX = x;
            this.startY = this.lastY = this.currentY = y;
        }

        public void moved() {
            lastX = currentX;
            lastY = currentY;
        }
    }

    private static final float TAP_JITTER = 20f;

    private final float pixelScaling;
    private final Handler handler = new Handler();
    private final List<TouchInfo> touches = new ArrayList<TouchInfo>(4);
    private final GestureHandler gestureHandler;
    private final int minimumFlingVelocity;
    private final int maximumFlingVelocity;
    private VelocityTracker velocityTracker;

    private final Runnable onLongTouchHandler = new Runnable() {
        @Override
        public void run() {
            TouchInfo touch = touches.get(0);
            gestureHandler.onLongTouch(touch.startX, touch.startY);
        }
    };

    public GestureDetector(Context context, GestureHandler gestureHandler) {
        this.gestureHandler = gestureHandler;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        pixelScaling = metrics.density;
        minimumFlingVelocity = ViewConfiguration.getMinimumFlingVelocity();
        maximumFlingVelocity = ViewConfiguration.getMaximumFlingVelocity();
    }

    public void onTouch(MotionEvent event) {
        int action = event.getActionMasked();
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            touches.add(new TouchInfo(pointerId, event.getX(pointerIndex), event.getY(pointerIndex)));
            gestureHandler.onStopFling();
        } else {
            for (int i = 0; i < event.getPointerCount(); ++i) {
                TouchInfo t = getTouch(event.getPointerId(i));
                t.currentX = event.getX(i);
                t.currentY = event.getY(i);
                if (!t.moved) {
                    float dx = t.currentX - t.startX;
                    float dy = t.currentY - t.startY;
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    float jitter = TAP_JITTER * pixelScaling;
                    if (dist > jitter) {
                        t.moved = true;
                        handler.removeCallbacks(onLongTouchHandler);

                        // consume initial
                        //t.startX = t.lastX = t.startX + dx * (dist - jitter);
                        //t.startY = t.lastY = t.startY + dy * (dist - jitter);
                    }
                }
            }
        }

        if (action == MotionEvent.ACTION_DOWN && touches.size() == 1) {
            handler.postDelayed(onLongTouchHandler, 600);
            velocityTracker = null;
        }

        if (action == MotionEvent.ACTION_POINTER_DOWN
                || (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP)
                && touches.get(0).id == pointerId) {
            handler.removeCallbacks(onLongTouchHandler);
        }

        if (action == MotionEvent.ACTION_MOVE) {
            if (touches.size() == 2) {
                velocityTracker = null;
                TouchInfo t0 = touches.get(0);
                TouchInfo t1 = touches.get(1);
                if (t0.moved || t1.moved) {
                    float oldDx = t1.lastX - t0.lastX;
                    float oldDy = t1.lastY - t0.lastY;
                    float oldD = (float) Math.sqrt(oldDx * oldDx + oldDy * oldDy);
                    float dx = t1.currentX - t0.currentX;
                    float dy = t1.currentY - t0.currentY;
                    float d = (float) Math.sqrt(dx * dx + dy * dy);
                    gestureHandler.onPinchZoom(t0.currentX + dx / 2, t0.currentY + dy / 2, d - oldD);
                    t0.moved();
                    t1.moved();
                }
            } else if (touches.size() == 1) {
                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain();
                }
                velocityTracker.addMovement(event);
                TouchInfo t0 = touches.get(0);
                if (t0.moved) {
                    gestureHandler.onTouchMove(t0.currentX - t0.lastX, t0.currentY - t0.lastY);
                    t0.moved();
                }
            }
        }

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
            for (int i=0; i < touches.size(); ++i) {
                if (touches.get(i).id == pointerId) {
                    touches.remove(i);
                    break;
                }
            }

            if (velocityTracker != null) {
                velocityTracker.computeCurrentVelocity(1000, maximumFlingVelocity);
                final float velocityY = velocityTracker.getYVelocity();
                final float velocityX = velocityTracker.getXVelocity();
                if (Math.abs(velocityY) > minimumFlingVelocity || Math.abs(velocityX) > minimumFlingVelocity) {
                    gestureHandler.onFling(velocityX, velocityY);

                }
                velocityTracker = null;
            }
        }
    }


    private TouchInfo getTouch(int pointerId) {
        for (int i = 0; i < touches.size(); ++i) {
            if (touches.get(i).id == pointerId) {
                return touches.get(i);
            }
        }
        return null;
    }
}
