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
import com.remoteyourcam.usb.ptp.EosConstants;
import com.remoteyourcam.usb.ptp.PtpAction;
import com.remoteyourcam.usb.ptp.EosConstants.EvfMode;
import com.remoteyourcam.usb.ptp.PtpCamera.IO;
import com.remoteyourcam.usb.ptp.PtpConstants.Property;
import com.remoteyourcam.usb.ptp.PtpConstants.Response;

public class EosSetLiveViewAction implements PtpAction {

    private final EosCamera camera;
    private final boolean enabled;

    public EosSetLiveViewAction(EosCamera camera, boolean enabled) {
        this.camera = camera;
        this.enabled = enabled;
    }

    @Override
    public void exec(IO io) {
        int evfMode = camera.getPtpProperty(Property.EosEvfMode);

        if (enabled && evfMode != EvfMode.ENABLE || !enabled && evfMode != EvfMode.DISABLE) {
            EosSetPropertyCommand setEvfMode = new EosSetPropertyCommand(camera, Property.EosEvfMode,
                    enabled ? EvfMode.ENABLE : EvfMode.DISABLE);
            io.handleCommand(setEvfMode);

            if (setEvfMode.getResponseCode() == Response.DeviceBusy) {
                camera.onDeviceBusy(this, true);
                return;
            } else if (setEvfMode.getResponseCode() != Response.Ok) {
                camera.onPtpWarning("Couldn't open live view");
                return;
            }
        }

        int outputDevice = camera.getPtpProperty(Property.EosEvfOutputDevice);

        if (enabled) {
            outputDevice |= EosConstants.EvfOutputDevice.PC;
        } else {
            outputDevice &= ~EosConstants.EvfOutputDevice.PC;
        }

        EosSetPropertyCommand setOutputDevice = new EosSetPropertyCommand(camera, Property.EosEvfOutputDevice,
                outputDevice);
        io.handleCommand(setOutputDevice);

        if (setOutputDevice.getResponseCode() == Response.DeviceBusy) {
            camera.onDeviceBusy(this, true);
        } else if (setOutputDevice.getResponseCode() == Response.Ok) {
            if (!enabled) {
                camera.onLiveViewStopped();
            } else {
                camera.onLiveViewStarted();
            }
            return;
        } else {
            camera.onPtpWarning("Couldn't open live view");
        }

    }

    @Override
    public void reset() {
    }
}
