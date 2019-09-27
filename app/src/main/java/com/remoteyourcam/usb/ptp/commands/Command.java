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

import android.util.Log;

import com.remoteyourcam.usb.AppConfig;
import com.remoteyourcam.usb.ptp.PacketUtil;
import com.remoteyourcam.usb.ptp.PtpAction;
import com.remoteyourcam.usb.ptp.PtpCamera;
import com.remoteyourcam.usb.ptp.PtpConstants;
import com.remoteyourcam.usb.ptp.PtpConstants.Type;

/**
 * Base class for all PTP commands.
 */
public abstract class Command implements PtpAction {

    private static final String TAG = "Command";

    protected final PtpCamera camera;

    /**
     * Deriving classes have to set this field to true if they want to send data
     * from host to camera.
     */
    protected boolean hasDataToSend;

    /**
     * Received response code, should be handled in
     * {@link #exec(com.remoteyourcam.usb.ptp.PtpCamera.IO)}.
     */
    protected int responseCode;

    private boolean hasResponseReceived;

    public Command(PtpCamera camera) {
        this.camera = camera;
    }

    @Override
    public abstract void exec(PtpCamera.IO io);

    public abstract void encodeCommand(ByteBuffer b);

    /**
     * Derived classes should implement this method if they want to send data
     * from host to camera. The field {@code hasDataToSend} has to be set to
     * true for the sending to be done. The data to send must not be greater
     * than the USB max packet size, any size below 256 should be save.
     */
    public void encodeData(ByteBuffer b) {
    }

    /**
     * Derived classes should implement this method if they want to decode data
     * received in an data packet that has been sent by the camera. The
     * {@code ByteBuffer} already points to the first byte behind the
     * transaction id, i.e. the payload.
     */
    protected void decodeData(ByteBuffer b, int length) {
        if (AppConfig.LOG) {
            Log.w(TAG, "Received data packet but handler not implemented");
        }
    }

    /**
     * Override if any special response data has to be decoded. The
     * {@code ByteBuffer} already points to the first byte behind the
     * transaction id, i.e. the payload.
     */
    protected void decodeResponse(ByteBuffer b, int length) {
    }

    public boolean hasDataToSend() {
        return hasDataToSend;
    }

    public boolean hasResponseReceived() {
        return hasResponseReceived;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void receivedRead(ByteBuffer b) {
        int length = b.getInt();
        int type = b.getShort() & 0xFFFF;
        int code = b.getShort() & 0xFFFF;
        int tx = b.getInt();

        if (AppConfig.LOG) {
            Log.i(TAG,
                    String.format("Received %s packet for %s, length %d, code %s, tx %d",
                            PtpConstants.typeToString(type), getClass().getSimpleName(), length,
                            PtpConstants.codeToString(type, code), tx));
        }
        if (AppConfig.LOG_PACKETS) {
            PacketUtil.logHexdump(TAG, b.array(), length < 512 ? length : 512);
        }

        if (type == Type.Data) {
            decodeData(b, length);
        } else if (type == Type.Response) {
            hasResponseReceived = true;
            responseCode = code;
            decodeResponse(b, length);
        } else {
            // error
            hasResponseReceived = true;
        }
    }

    /**
     * Reset fields so this command may be requeued.
     */
    @Override
    public void reset() {
        responseCode = 0;
        hasResponseReceived = false;
    }

    protected void encodeCommand(ByteBuffer b, int code) {
        b.putInt(12);
        b.putShort((short) Type.Command);
        b.putShort((short) code);
        b.putInt(camera.nextTransactionId());
        if (AppConfig.LOG_PACKETS) {
            Log.i(TAG, "command packet for " + getClass().getSimpleName());
            PacketUtil.logHexdump(TAG, b.array(), 12);
        }
    }

    protected void encodeCommand(ByteBuffer b, int code, int p0) {
        b.putInt(16);
        b.putShort((short) Type.Command);
        b.putShort((short) code);
        b.putInt(camera.nextTransactionId());
        b.putInt(p0);
        if (AppConfig.LOG_PACKETS) {
            Log.i(TAG, "command packet for " + getClass().getSimpleName());
            PacketUtil.logHexdump(TAG, b.array(), 16);
        }
    }

    protected void encodeCommand(ByteBuffer b, int code, int p0, int p1) {
        b.putInt(20);
        b.putShort((short) Type.Command);
        b.putShort((short) code);
        b.putInt(camera.nextTransactionId());
        b.putInt(p0);
        b.putInt(p1);
        if (AppConfig.LOG_PACKETS) {
            Log.i(TAG, "command packet for " + getClass().getSimpleName());
            PacketUtil.logHexdump(TAG, b.array(), 20);
        }
    }

    protected void encodeCommand(ByteBuffer b, int code, int p0, int p1, int p2) {
        b.putInt(24);
        b.putShort((short) Type.Command);
        b.putShort((short) code);
        b.putInt(camera.nextTransactionId());
        b.putInt(p0);
        b.putInt(p1);
        b.putInt(p2);
        if (AppConfig.LOG_PACKETS) {
            Log.i(TAG, "command packet for " + getClass().getSimpleName());
            PacketUtil.logHexdump(TAG, b.array(), 24);
        }
    }
}
