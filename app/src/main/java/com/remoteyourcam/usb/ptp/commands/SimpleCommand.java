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

import com.remoteyourcam.usb.ptp.PtpCamera;
import com.remoteyourcam.usb.ptp.PtpCamera.IO;
import com.remoteyourcam.usb.ptp.PtpConstants.Response;

public class SimpleCommand extends Command {

    private final int operation;
    private int numParams;
    private int p0;
    private int p1;

    public SimpleCommand(PtpCamera camera, int operation) {
        super(camera);
        this.operation = operation;
    }

    public SimpleCommand(PtpCamera camera, int operation, int p0) {
        super(camera);
        this.operation = operation;
        this.p0 = p0;
        this.numParams = 1;
    }

    public SimpleCommand(PtpCamera camera, int operation, int p0, int p1) {
        super(camera);
        this.operation = operation;
        this.p0 = p0;
        this.p1 = p1;
        this.numParams = 2;
    }

    @Override
    public void exec(IO io) {
        io.handleCommand(this);
        if (responseCode == Response.DeviceBusy) {
            camera.onDeviceBusy(this, true);
            return;
        }
    }

    @Override
    public void encodeCommand(ByteBuffer b) {
        if (numParams == 2) {
            encodeCommand(b, operation, p0, p1);
        } else if (numParams == 1) {
            encodeCommand(b, operation, p0);
        } else {
            encodeCommand(b, operation);
        }
    }
}
