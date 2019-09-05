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

import android.util.Log;

import com.remoteyourcam.usb.AppConfig;
import com.remoteyourcam.usb.ptp.NikonCamera;
import com.remoteyourcam.usb.ptp.PtpCamera.IO;
import com.remoteyourcam.usb.ptp.PtpConstants;
import com.remoteyourcam.usb.ptp.PtpConstants.Event;
import com.remoteyourcam.usb.ptp.PtpConstants.Operation;

public class NikonEventCheckCommand extends NikonCommand {

    private static final String TAG = NikonEventCheckCommand.class.getSimpleName();

    public NikonEventCheckCommand(NikonCamera camera) {
        super(camera);
    }

    @Override
    public void exec(IO io) {
        io.handleCommand(this);
    }

    @Override
    public void encodeCommand(ByteBuffer b) {
        encodeCommand(b, Operation.NikonGetEvent);
    }

    @Override
    protected void decodeData(ByteBuffer b, int length) {
        int count = b.getShort();

        while (count > 0) {
            --count;

            int eventCode = b.getShort();
            int eventParam = b.getInt();

            if (AppConfig.LOG) {
                Log.i(TAG,
                        String.format("event %s value %s(%04x)", PtpConstants.eventToString(eventCode),
                                PtpConstants.propertyToString(eventParam), eventParam));
            }

            switch (eventCode) {
            case Event.ObjectAdded:
                camera.onEventObjectAdded(eventParam);
                break;
            case Event.DevicePropChanged:
                camera.onEventDevicePropChanged(eventParam);
                break;
            case Event.CaptureComplete:
                camera.onEventCaptureComplete();
                break;
            }
        }
    }
}
