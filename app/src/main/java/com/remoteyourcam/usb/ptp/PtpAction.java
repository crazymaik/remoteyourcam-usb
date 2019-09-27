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

/**
 * {@code PtpActions} execute one or more {@code Command}s against the actual
 * hardware.
 *
 * A {@code PtpCamera} queues {@Code PtpAction}s into the worker thread
 * for further communications. The action should do the communication via the
 * given {@code IO} interface and based on the received data and response repor
 * back to the actual {@code PtpCamera}.
 */
public interface PtpAction {

    void exec(PtpCamera.IO io);

    /**
     * Reset an already used action so it can be re-used.
     */
    void reset();
}
