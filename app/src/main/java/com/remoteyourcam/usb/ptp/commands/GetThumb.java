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
package com.remoteyourcam.usb.ptp.commands;

import java.nio.ByteBuffer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.remoteyourcam.usb.ptp.PtpCamera;
import com.remoteyourcam.usb.ptp.PtpCamera.IO;
import com.remoteyourcam.usb.ptp.PtpConstants;

public class GetThumb extends Command {

    private static final String TAG = GetThumb.class.getSimpleName();

    private final int objectHandle;
    private Bitmap inBitmap;

    public GetThumb(PtpCamera camera, int objectHandle) {
        super(camera);
        this.objectHandle = objectHandle;
    }

    public Bitmap getBitmap() {
        return inBitmap;
    }

    @Override
    public void exec(IO io) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {
        super.reset();
        inBitmap = null;
    }

    @Override
    public void encodeCommand(ByteBuffer b) {
        encodeCommand(b, PtpConstants.Operation.GetThumb, objectHandle);
    }

    @Override
    protected void decodeData(ByteBuffer b, int length) {
        try {
            // 12 == offset of data header
            inBitmap = BitmapFactory.decodeByteArray(b.array(), 12, length - 12);
        } catch (RuntimeException e) {
            Log.i(TAG, "exception on decoding picture : " + e.toString());
        } catch (OutOfMemoryError e) {
            System.gc();
        }
    }
}
