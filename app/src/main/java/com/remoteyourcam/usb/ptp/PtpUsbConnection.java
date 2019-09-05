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

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbRequest;

public class PtpUsbConnection {

    private final UsbDeviceConnection connection;
    private final UsbEndpoint bulkOut;
    private final UsbEndpoint bulkIn;
    private final int vendorId;
    private final int productId;

    public PtpUsbConnection(UsbDeviceConnection connection, UsbEndpoint bulkIn, UsbEndpoint bulkOut, int vendorId,
            int productId) {
        this.connection = connection;
        this.bulkIn = bulkIn;
        this.bulkOut = bulkOut;
        this.vendorId = vendorId;
        this.productId = productId;
    }

    public int getVendorId() {
        return vendorId;
    }

    public int getProductId() {
        return productId;
    }

    public void close() {
        connection.close();
    }

    public int getMaxPacketInSize() {
        return bulkIn.getMaxPacketSize();
    }

    public int getMaxPacketOutSize() {
        return bulkOut.getMaxPacketSize();
    }

    public UsbRequest createInRequest() {
        UsbRequest r = new UsbRequest();
        r.initialize(connection, bulkIn);
        return r;
    }

    public int bulkTransferOut(byte[] buffer, int length, int timeout) {
        return connection.bulkTransfer(bulkOut, buffer, length, timeout);
    }

    public int bulkTransferIn(byte[] buffer, int maxLength, int timeout) {
        return connection.bulkTransfer(bulkIn, buffer, maxLength, timeout);
    }

    public void requestWait() {
        connection.requestWait();
    }
}
