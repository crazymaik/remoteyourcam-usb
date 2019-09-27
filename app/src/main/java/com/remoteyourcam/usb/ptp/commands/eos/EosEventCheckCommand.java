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
package com.remoteyourcam.usb.ptp.commands.eos;

import java.nio.ByteBuffer;

import android.util.Log;

import com.remoteyourcam.usb.AppConfig;
import com.remoteyourcam.usb.ptp.EosCamera;
import com.remoteyourcam.usb.ptp.PacketUtil;
import com.remoteyourcam.usb.ptp.PtpCamera.IO;
import com.remoteyourcam.usb.ptp.PtpConstants;
import com.remoteyourcam.usb.ptp.PtpConstants.Event;
import com.remoteyourcam.usb.ptp.PtpConstants.Operation;
import com.remoteyourcam.usb.ptp.PtpConstants.Property;
import com.remoteyourcam.usb.ptp.PtpConstants.Response;

public class EosEventCheckCommand extends EosCommand {

    private static final String TAG = EosEventCheckCommand.class.getSimpleName();

    public EosEventCheckCommand(EosCamera camera) {
        super(camera);
    }

    @Override
    public void exec(IO io) {
        io.handleCommand(this);
        if (responseCode == Response.DeviceBusy) {
            camera.onDeviceBusy(this, false);
        }
    }

    @Override
    public void encodeCommand(ByteBuffer b) {
        encodeCommand(b, Operation.EosEventCheck);
    }

    @Override
    protected void decodeData(ByteBuffer b, int length) {
        while (b.position() < length) {
            int eventLength = b.getInt();
            int event = b.getInt();
            if (AppConfig.LOG) {
                Log.i(TAG, "event length " + eventLength);
                Log.i(TAG, "event type " + PtpConstants.eventToString(event));
            }
            switch (event) {
            case Event.EosObjectAdded: {
                int objectHandle = b.getInt();
                int storageId = b.getInt();
                int objectFormat = b.getShort();
                skip(b, eventLength - 18);
                camera.onEventDirItemCreated(objectHandle, storageId, objectFormat, "TODO");
                break;
            }
            case Event.EosDevicePropChanged: {
                int property = b.getInt();
                if (AppConfig.LOG) {
                    Log.i(TAG, "property " + PtpConstants.propertyToString(property));
                }
                switch (property) {
                case Property.EosApertureValue:
                case Property.EosShutterSpeed:
                case Property.EosIsoSpeed:
                case Property.EosWhitebalance:
                case Property.EosEvfOutputDevice:
                case Property.EosShootingMode:
                case Property.EosAvailableShots:
                case Property.EosColorTemperature:
                case Property.EosAfMode:
                case Property.EosMeteringMode:
                case Property.EosExposureCompensation:
                case Property.EosPictureStyle:
                case Property.EosEvfMode: {
                    int value = b.getInt();
                    if (AppConfig.LOG) {
                        Log.i(TAG, "value " + value);
                    }
                    camera.onPropertyChanged(property, value);
                    break;
                }
                default:
                    if (AppConfig.LOG && eventLength <= 200) {
                        PacketUtil.logHexdump(TAG, b.array(), b.position(), eventLength - 12);
                    }
                    skip(b, eventLength - 12);
                    break;
                }
                break;
            }
            case Event.EosDevicePropDescChanged: {
                int property = b.getInt();
                if (AppConfig.LOG) {
                    Log.i(TAG, "property " + PtpConstants.propertyToString(property));
                }
                switch (property) {
                case Property.EosApertureValue:
                case Property.EosShutterSpeed:
                case Property.EosIsoSpeed:
                case Property.EosMeteringMode:
                case Property.EosPictureStyle:
                case Property.EosExposureCompensation:
                case Property.EosColorTemperature:
                case Property.EosWhitebalance: {
                    /* int dataType = */b.getInt();
                    int num = b.getInt();
                    if (AppConfig.LOG) {
                        Log.i(TAG, "property desc with num " + num);
                    }
                    if (eventLength != 20 + 4 * num) {
                        if (AppConfig.LOG) {
                            Log.i(TAG, String.format("Event Desc length invalid should be %d but is %d", 20 + 4 * num,
                                    eventLength));
                            PacketUtil.logHexdump(TAG, b.array(), b.position() - 20, eventLength);
                        }
                    }
                    int[] values = new int[num];
                    for (int i = 0; i < num; ++i) {
                        values[i] = b.getInt();
                    }
                    if (eventLength != 20 + 4 * num) {
                        for (int i = 20 + 4 * num; i < eventLength; ++i) {
                            b.get();
                        }
                    }
                    camera.onPropertyDescChanged(property, values);
                    break;
                }
                default:
                    if (AppConfig.LOG && eventLength <= 50) {
                        PacketUtil.logHexdump(TAG, b.array(), b.position(), eventLength - 12);
                    }
                    skip(b, eventLength - 12);
                }
                break;
            }
            case Event.EosBulbExposureTime: {
                int seconds = b.getInt();
                camera.onBulbExposureTime(seconds);
                break;
            }
            case Event.EosCameraStatus: {
                // 0 - capture stopped
                // 1 - capture started
                int status = b.getInt();
                camera.onEventCameraCapture(status != 0);
                break;
            }
            case Event.EosWillSoonShutdown: {
                /* int seconds = */b.getInt();
                //TODO
                break;
            }
            default:
                //                if (BuildConfig.LOG) {
                //                    PacketUtil.logHexdump(b.array(), b.position(), eventLength - 8);
                //                }
                skip(b, eventLength - 8);
                break;
            }
        }
    }

    private void skip(ByteBuffer b, int length) {
        for (int i = 0; i < length; ++i) {
            b.get();
        }
    }
}
