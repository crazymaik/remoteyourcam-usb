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

import com.remoteyourcam.usb.ptp.PacketUtil;
import com.remoteyourcam.usb.ptp.PtpConstants;

/**
 * Object info data set as defined by the PTP standard.
 */
public class ObjectInfo {

    public int storageId;
    public int objectFormat;
    public int protectionStatus;
    public int objectCompressedSize;
    public int thumbFormat;
    public int thumbCompressedSize;
    public int thumbPixWidth;
    public int thumbPixHeight;
    public int imagePixWidth;
    public int imagePixHeight;
    public int imageBitDepth;
    public int parentObject;
    public int associationType;
    public int associationDesc;
    public int sequenceNumber;
    public String filename;
    public String captureDate;
    public String modificationDate;
    public int keywords;

    public ObjectInfo() {
    }

    public ObjectInfo(ByteBuffer b, int length) {
        decode(b, length);
    }

    public void decode(ByteBuffer b, int length) {
        storageId = b.getInt();
        objectFormat = b.getShort();
        protectionStatus = b.getShort();
        objectCompressedSize = b.getInt();
        thumbFormat = b.getShort();
        thumbCompressedSize = b.getInt();
        thumbPixWidth = b.getInt();
        thumbPixHeight = b.getInt();
        imagePixWidth = b.getInt();
        imagePixHeight = b.getInt();
        imageBitDepth = b.getInt();
        parentObject = b.getInt();
        associationType = b.getShort();
        associationDesc = b.getInt();
        sequenceNumber = b.getInt();
        filename = PacketUtil.readString(b);
        captureDate = PacketUtil.readString(b);
        modificationDate = PacketUtil.readString(b);
        keywords = b.get(); // string, not used on camera?
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("ObjectInfo\n");
        b.append("StorageId: ").append(String.format("0x%08x\n", storageId));
        b.append("ObjectFormat: ").append(PtpConstants.objectFormatToString(objectFormat)).append('\n');
        b.append("ProtectionStatus: ").append(protectionStatus).append('\n');
        b.append("ObjectCompressedSize: ").append(objectCompressedSize).append('\n');
        b.append("ThumbFormat: ").append(PtpConstants.objectFormatToString(thumbFormat)).append('\n');
        b.append("ThumbCompressedSize: ").append(thumbCompressedSize).append('\n');
        b.append("ThumbPixWdith: ").append(thumbPixWidth).append('\n');
        b.append("ThumbPixHeight: ").append(thumbPixHeight).append('\n');
        b.append("ImagePixWidth: ").append(imagePixWidth).append('\n');
        b.append("ImagePixHeight: ").append(imagePixHeight).append('\n');
        b.append("ImageBitDepth: ").append(imageBitDepth).append('\n');
        b.append("ParentObject: ").append(String.format("0x%08x", parentObject)).append('\n');
        b.append("AssociationType: ").append(associationType).append('\n');
        b.append("AssociatonDesc: ").append(associationDesc).append('\n');
        b.append("Filename: ").append(filename).append('\n');
        b.append("CaptureDate: ").append(captureDate).append('\n');
        b.append("ModificationDate: ").append(modificationDate).append('\n');
        b.append("Keywords: ").append(keywords).append('\n');
        return b.toString();
    }
}
