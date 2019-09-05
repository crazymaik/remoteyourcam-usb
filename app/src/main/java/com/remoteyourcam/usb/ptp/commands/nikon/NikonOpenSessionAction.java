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

import com.remoteyourcam.usb.ptp.NikonCamera;
import com.remoteyourcam.usb.ptp.PtpAction;
import com.remoteyourcam.usb.ptp.PtpCamera.IO;
import com.remoteyourcam.usb.ptp.PtpConstants;
import com.remoteyourcam.usb.ptp.PtpConstants.Datatype;
import com.remoteyourcam.usb.ptp.PtpConstants.Operation;
import com.remoteyourcam.usb.ptp.PtpConstants.Property;
import com.remoteyourcam.usb.ptp.commands.OpenSessionCommand;
import com.remoteyourcam.usb.ptp.commands.SetDevicePropValueCommand;

public class NikonOpenSessionAction implements PtpAction {

    private final NikonCamera camera;

    public NikonOpenSessionAction(NikonCamera camera) {
        this.camera = camera;
    }

    @Override
    public void exec(IO io) {
        OpenSessionCommand openSession = new OpenSessionCommand(camera);
        io.handleCommand(openSession);
        if (openSession.getResponseCode() == PtpConstants.Response.Ok) {
            if (camera.hasSupportForOperation(Operation.NikonGetVendorPropCodes)) {
                NikonGetVendorPropCodesCommand getPropCodes = new NikonGetVendorPropCodesCommand(camera);
                io.handleCommand(getPropCodes);
                SetDevicePropValueCommand c = new SetDevicePropValueCommand(camera, Property.NikonRecordingMedia, 1,
                        Datatype.uint8);
                io.handleCommand(c);
                if (getPropCodes.getResponseCode() == PtpConstants.Response.Ok
                        && c.getResponseCode() == PtpConstants.Response.Ok) {
                    camera.setVendorPropCodes(getPropCodes.getPropertyCodes());
                    camera.onSessionOpened();
                } else {
                    camera.onPtpError(String.format(
                            "Couldn't read device property codes! Open session command failed with error code \"%s\"",
                            PtpConstants.responseToString(getPropCodes.getResponseCode())));
                }
            } else {
                camera.onSessionOpened();
            }
        } else {
            camera.onPtpError(String.format(
                    "Couldn't open session! Open session command failed with error code \"%s\"",
                    PtpConstants.responseToString(openSession.getResponseCode())));
        }
    }

    @Override
    public void reset() {
    }
}
