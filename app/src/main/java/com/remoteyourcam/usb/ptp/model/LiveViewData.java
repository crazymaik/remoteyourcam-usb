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
package com.remoteyourcam.usb.ptp.model;

import java.nio.ByteBuffer;

import android.graphics.Bitmap;

public class LiveViewData {

    public Bitmap bitmap;

    public int zoomFactor;
    public int zoomRectLeft;
    public int zoomRectTop;
    public int zoomRectRight;
    public int zoomRectBottom;

    public boolean hasHistogram;
    public ByteBuffer histogram;

    // dimensions are in bitmap size
    public boolean hasAfFrame;
    public int nikonAfFrameCenterX;
    public int nikonAfFrameCenterY;
    public int nikonAfFrameWidth;
    public int nikonAfFrameHeight;

    public int nikonWholeWidth;
    public int nikonWholeHeight;
}
