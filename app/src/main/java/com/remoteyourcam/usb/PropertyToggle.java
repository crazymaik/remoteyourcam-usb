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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ToggleButton;

public class PropertyToggle extends ToggleButton {

    private Drawable drawable;

    public PropertyToggle(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public PropertyToggle(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PropertyToggle(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final Drawable d = drawable;
        if (d != null) {
            int h = d.getIntrinsicHeight();
            int w = d.getIntrinsicWidth();
            int y = (getHeight() - h) / 2;
            int x = (getWidth() - w) / 2;
            d.setBounds(x, y, x + w, y + h);
            d.draw(canvas);
        }
    }

    public void setButtonDrawable2(Drawable d) {
        this.drawable = d;
    }
}
