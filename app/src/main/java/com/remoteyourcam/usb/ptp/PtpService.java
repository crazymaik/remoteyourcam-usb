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
package com.remoteyourcam.usb.ptp;

import com.remoteyourcam.usb.ptp.Camera.CameraListener;

import android.content.Context;
import android.content.Intent;

public interface PtpService {

    void setCameraListener(CameraListener listener);

    void initialize(Context context, Intent intent);

    void shutdown();

    void lazyShutdown();

    public static class Singleton {
        private static PtpService singleton;

        public static PtpService getInstance(Context context) {
            if (singleton == null) {
                singleton = new PtpUsbService(context);
            }
            return singleton;
        }

        public static void setInstance(PtpService service) {
            singleton = service;
        }
    }
}
