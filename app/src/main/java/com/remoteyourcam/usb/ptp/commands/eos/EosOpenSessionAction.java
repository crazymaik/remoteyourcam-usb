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

import com.remoteyourcam.usb.ptp.EosCamera;
import com.remoteyourcam.usb.ptp.PtpAction;
import com.remoteyourcam.usb.ptp.PtpConstants;
import com.remoteyourcam.usb.ptp.PtpCamera.IO;
import com.remoteyourcam.usb.ptp.commands.OpenSessionCommand;

public class EosOpenSessionAction implements PtpAction {

    private final EosCamera camera;

    public EosOpenSessionAction(EosCamera camera) {
        this.camera = camera;
    }

    @Override
    public void exec(IO io) {
        OpenSessionCommand openSession = new OpenSessionCommand(camera);
        io.handleCommand(openSession);
        if (openSession.getResponseCode() == PtpConstants.Response.Ok) {
            EosSetPcModeCommand setPcMode = new EosSetPcModeCommand(camera);
            io.handleCommand(setPcMode);
            if (setPcMode.getResponseCode() == PtpConstants.Response.Ok) {
                EosSetExtendedEventInfoCommand c = new EosSetExtendedEventInfoCommand(camera);
                io.handleCommand(c);
                if (c.getResponseCode() == PtpConstants.Response.Ok) {
                    camera.onSessionOpened();
                    return;
                } else {
                    camera.onPtpError(String.format(
                            "Couldn't open session! Setting extended event info failed with error code \"%s\"",
                            PtpConstants.responseToString(c.getResponseCode())));
                }
            } else {
                camera.onPtpError(String.format(
                        "Couldn't open session! Setting PcMode property failed with error code \"%s\"",
                        PtpConstants.responseToString(setPcMode.getResponseCode())));
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
