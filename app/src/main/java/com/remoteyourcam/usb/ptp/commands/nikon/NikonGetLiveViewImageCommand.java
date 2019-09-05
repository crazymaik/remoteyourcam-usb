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
package com.remoteyourcam.usb.ptp.commands.nikon;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.acra.ErrorReporter;

import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.Log;

import com.remoteyourcam.usb.AppConfig;
import com.remoteyourcam.usb.ptp.NikonCamera;
import com.remoteyourcam.usb.ptp.PacketUtil;
import com.remoteyourcam.usb.ptp.PtpCamera.IO;
import com.remoteyourcam.usb.ptp.PtpConstants.Operation;
import com.remoteyourcam.usb.ptp.PtpConstants.Product;
import com.remoteyourcam.usb.ptp.PtpConstants.Response;
import com.remoteyourcam.usb.ptp.model.LiveViewData;

public class NikonGetLiveViewImageCommand extends NikonCommand {

    private static boolean haveAddedDumpToAcra = false;

    private static final String TAG = NikonGetLiveViewImageCommand.class.getSimpleName();
    private static byte[] tmpStorage = new byte[0x4000];
    private final Options options;
    private LiveViewData data;

    public NikonGetLiveViewImageCommand(NikonCamera camera, LiveViewData data) {
        super(camera);
        this.data = data;
        if (data == null) {
            this.data = new LiveViewData();
            //this.data.histogram = ByteBuffer.allocate(1024 * 4);
            //this.data.histogram.order(ByteOrder.LITTLE_ENDIAN);
        } else {
            this.data = data;
        }
        options = new BitmapFactory.Options();
        options.inBitmap = this.data.bitmap;
        options.inSampleSize = 1;
        options.inTempStorage = tmpStorage;
        this.data.bitmap = null;
    }

    @Override
    public void exec(IO io) {
        if (!camera.isLiveViewOpen()) {
            return;
        }
        io.handleCommand(this);
        if (responseCode == Response.DeviceBusy) {
            camera.onDeviceBusy(this, true);
            return;
        }
        data.hasHistogram = false;
        if (this.data.bitmap != null && responseCode == Response.Ok) {
            camera.onLiveViewReceived(data);
        } else {
            camera.onLiveViewReceived(null);
        }
    }

    @Override
    public void encodeCommand(ByteBuffer b) {
        encodeCommand(b, Operation.NikonGetLiveViewImage);
    }

    @Override
    protected void decodeData(ByteBuffer b, int length) {
        if (length <= 128) {
            return;
        }

        data.hasAfFrame = false;

        int productId = camera.getProductId();
        int start = b.position();
        int pictureOffset;

        switch (productId) {
        case Product.NikonD5000:
        case Product.NikonD3S:
        case Product.NikonD90:
            pictureOffset = 128;
            break;
        case Product.NikonD3X:
        case Product.NikonD300S:
        case Product.NikonD3:
        case Product.NikonD300:
        case Product.NikonD700:
            pictureOffset = 64;
            break;
        case Product.NikonD7000:
        case Product.NikonD5100:
            pictureOffset = 384;
            break;
        default:
            if (AppConfig.USE_ACRA && !haveAddedDumpToAcra) {
                try {
                    haveAddedDumpToAcra = true;
                    String hex = PacketUtil.hexDumpToString(b.array(), start, length < 728 ? length : 728);
                    ErrorReporter.getInstance().putCustomData("liveview hexdump", hex);
                } catch (Throwable e) {
                    // no fail
                }
            }
            return;
        }

        b.order(ByteOrder.BIG_ENDIAN);

        // read af frame
        {
            data.hasAfFrame = true;

            int jpegImageWidth = b.getShort() & 0xFFFF;
            int jpegImageHeight = b.getShort() & 0xFFFF;
            int wholeWidth = b.getShort() & 0xFFFF;
            int wholeHeight = b.getShort() & 0xFFFF;

            float multX = jpegImageWidth / (float) wholeWidth;
            float multY = jpegImageHeight / (float) wholeHeight;

            b.position(start + 16);
            data.nikonWholeWidth = wholeWidth;
            data.nikonWholeHeight = wholeHeight;
            data.nikonAfFrameWidth = (int) ((b.getShort() & 0xFFFF) * multX);
            data.nikonAfFrameHeight = (int) ((b.getShort() & 0xFFFF) * multY);
            data.nikonAfFrameCenterX = (int) ((b.getShort() & 0xFFFF) * multX);
            data.nikonAfFrameCenterY = (int) ((b.getShort() & 0xFFFF) * multY);
        }

        b.order(ByteOrder.LITTLE_ENDIAN);

        b.position(start + pictureOffset);

        if (b.remaining() <= 128) {
            data.bitmap = null;
            return;
        }

        try {
            data.bitmap = BitmapFactory.decodeByteArray(b.array(), b.position(), length - b.position(), options);
        } catch (RuntimeException e) {
            Log.e(TAG, "decoding failed " + e.toString());
            Log.e(TAG, e.getLocalizedMessage());
            if (AppConfig.LOG) {
                PacketUtil.logHexdump(TAG, b.array(), start, 512);
            }
        }
    }
}
