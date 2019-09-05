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
package com.remoteyourcam.usb.ptp.model;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.remoteyourcam.usb.ptp.PacketUtil;
import com.remoteyourcam.usb.ptp.PtpConstants;

/**
 * Device info data set as defined by PTP standard.
 */
public class DeviceInfo {

    public short standardVersion;
    public int vendorExtensionId;
    public short vendorExtensionVersion;
    public String vendorExtensionDesc;
    public short functionalMode;
    public int[] operationsSupported;
    public int[] eventsSupported;
    public int[] devicePropertiesSupported;
    public int[] captureFormats;
    public int[] imageFormats;
    public String manufacture;
    public String model;
    public String deviceVersion;
    public String serialNumber;

    public DeviceInfo(ByteBuffer b, int length) {
        decode(b, length);
    }

    public DeviceInfo() {
    }

    public void decode(ByteBuffer b, int length) {
        standardVersion = b.getShort();
        vendorExtensionId = b.getInt();
        vendorExtensionVersion = b.getShort();
        vendorExtensionDesc = PacketUtil.readString(b);
        functionalMode = b.getShort();
        operationsSupported = PacketUtil.readU16Array(b);
        eventsSupported = PacketUtil.readU16Array(b);
        devicePropertiesSupported = PacketUtil.readU16Array(b);
        captureFormats = PacketUtil.readU16Array(b);
        imageFormats = PacketUtil.readU16Array(b);
        manufacture = PacketUtil.readString(b);
        model = PacketUtil.readString(b);
        deviceVersion = PacketUtil.readString(b);
        serialNumber = PacketUtil.readString(b);
    }

    public void encode(ByteBuffer b) {
        b.putShort(standardVersion);
        b.putInt(vendorExtensionId);
        b.putInt(vendorExtensionVersion);
        PacketUtil.writeString(b, "");
        b.putShort(functionalMode);
        PacketUtil.writeU16Array(b, new int[0]);
        PacketUtil.writeU16Array(b, new int[0]);
        PacketUtil.writeU16Array(b, new int[0]);
        PacketUtil.writeU16Array(b, new int[0]);
        PacketUtil.writeU16Array(b, new int[0]);
        PacketUtil.writeString(b, "");
        PacketUtil.writeString(b, "");
        PacketUtil.writeString(b, "");
    }

    @Override
    public String toString() {
        // Changes here have to reflect changes in PtpConstants.main()
        StringBuilder b = new StringBuilder();
        b.append("DeviceInfo\n");
        b.append("StandardVersion: ").append(standardVersion).append('\n');
        b.append("VendorExtensionId: ").append(vendorExtensionId).append('\n');
        b.append("VendorExtensionVersion: ").append(vendorExtensionVersion).append('\n');
        b.append("VendorExtensionDesc: ").append(vendorExtensionDesc).append('\n');
        b.append("FunctionalMode: ").append(functionalMode).append('\n');
        appendU16Array(b, "OperationsSupported", PtpConstants.Operation.class, operationsSupported);
        appendU16Array(b, "EventsSupported", PtpConstants.Event.class, eventsSupported);
        appendU16Array(b, "DevicePropertiesSupported", PtpConstants.Property.class, devicePropertiesSupported);
        appendU16Array(b, "CaptureFormats", PtpConstants.ObjectFormat.class, captureFormats);
        appendU16Array(b, "ImageFormats", PtpConstants.ObjectFormat.class, imageFormats);
        b.append("Manufacture: ").append(manufacture).append('\n');
        b.append("Model: ").append(model).append('\n');
        b.append("DeviceVersion: ").append(deviceVersion).append('\n');
        b.append("SerialNumber: ").append(serialNumber).append('\n');
        return b.toString();
    }

    private static void appendU16Array(StringBuilder b, String name, Class<?> cl, int[] a) {
        Arrays.sort(a);
        b.append(name).append(":\n");
        for (int i = 0; i < a.length; ++i) {
            b.append("    ").append(PtpConstants.constantToString(cl, a[i])).append('\n');
        }
    }
}
