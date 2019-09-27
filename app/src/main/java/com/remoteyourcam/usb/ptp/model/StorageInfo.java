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

public class StorageInfo {

    public int storageType;
    public int filesystemType;
    public int accessCapability;
    public long maxCapacity;
    public long freeSpaceInBytes;
    public int freeSpaceInImages;
    public String storageDescription;
    public String volumeLabel;

    public StorageInfo(ByteBuffer b, int length) {
        decode(b, length);
    }

    private void decode(ByteBuffer b, int length) {
        storageType = b.getShort() & 0xffff;
        filesystemType = b.getShort() & 0xffff;
        accessCapability = b.getShort() & 0xff;
        maxCapacity = b.getLong();
        freeSpaceInBytes = b.getLong();
        freeSpaceInImages = b.getInt();
        storageDescription = PacketUtil.readString(b);
        volumeLabel = PacketUtil.readString(b);
    }
}
