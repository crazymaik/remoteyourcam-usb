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

import java.nio.ByteBuffer;

import android.util.Log;

public class PacketUtil {

    public static int[] readU32Array(ByteBuffer b) {
        int len = b.getInt();
        int[] a = new int[len];
        for (int i = 0; i < len; ++i) {
            a[i] = b.getInt();
        }
        return a;
    }

    public static int[] readU16Array(ByteBuffer b) {
        int len = b.getInt();
        int[] a = new int[len];
        for (int i = 0; i < len; ++i) {
            a[i] = b.getShort() & 0xFFFF;
        }
        return a;
    }

    public static void writeU16Array(ByteBuffer b, int[] a) {
        b.putInt(a.length);
        for (int v : a) {
            b.putShort((short) v);
        }
    }

    public static int[] readU8Array(ByteBuffer b) {
        int len = b.getInt();
        int[] a = new int[len];
        for (int i = 0; i < len; ++i) {
            a[i] = b.get() & 0xFF;
        }
        return a;
    }

    public static int[] readU32Enumeration(ByteBuffer b) {
        int len = b.getShort() & 0xFFFF;
        int[] a = new int[len];
        for (int i = 0; i < len; ++i) {
            a[i] = b.getInt();
        }
        return a;
    }

    public static int[] readS16Enumeration(ByteBuffer b) {
        int len = b.getShort() & 0xFFFF;
        int[] a = new int[len];
        for (int i = 0; i < len; ++i) {
            a[i] = b.getShort();
        }
        return a;
    }

    public static int[] readU16Enumeration(ByteBuffer b) {
        int len = b.getShort() & 0xFFFF;
        int[] a = new int[len];
        for (int i = 0; i < len; ++i) {
            a[i] = b.getShort() & 0xFFFF;
        }
        return a;
    }

    public static int[] readU8Enumeration(ByteBuffer b) {
        int len = b.getShort() & 0xFFFF;
        int[] a = new int[len];
        for (int i = 0; i < len; ++i) {
            a[i] = b.get() & 0xFF;
        }
        return a;
    }

    public static String readString(ByteBuffer b) {
        int len = b.get() & 0xFF;
        if (len > 0) {
            char[] ch = new char[len - 1];
            for (int i = 0; i < len - 1; ++i) {
                ch[i] = b.getChar();
            }
            // read '\0'
            b.getChar();
            return String.copyValueOf(ch);
        }
        return "";
    }

    public static void writeString(ByteBuffer b, String s) {
        b.put((byte) s.length());
        if (s.length() > 0) {
            for (int i = 0; i < s.length(); ++i) {
                b.putShort((short) s.charAt(i));
            }
            b.putShort((short) 0);
        }
    }

    public static String hexDumpToString(byte[] a, int offset, int len) {
        int lines = len / 16;
        int rest = len % 16;

        StringBuilder b = new StringBuilder((lines + 1) * 97);

        for (int i = 0; i < lines; ++i) {
            b.append(String.format("%04x ", i * 16));
            for (int k = 0; k < 16; ++k) {
                b.append(String.format("%02x ", a[offset + i * 16 + k]));
            }
            for (int k = 0; k < 16; ++k) {
                char ch = (char) a[offset + i * 16 + k];
                b.append(ch >= 0x20 && ch <= 0x7E ? ch : '.');
            }
            b.append('\n');
        }

        if (rest != 0) {
            b.append(String.format("%04x ", lines * 16));
            for (int k = 0; k < rest; ++k) {
                b.append(String.format("%02x ", a[offset + lines * 16 + k]));
            }
            for (int k = 0; k < (16 - rest) * 3; ++k) {
                b.append(' ');
            }
            for (int k = 0; k < rest; ++k) {
                char ch = (char) a[offset + lines * 16 + k];
                b.append(ch >= 0x20 && ch <= 0x7E ? ch : '.');
            }
            b.append('\n');
        }

        return b.toString();
    }

    public static void logHexdump(String tag, byte[] a, int offset, int len) {
        Log.i(tag, hexDumpToString(a, offset, len));
    }

    public static void logHexdump(String tag, byte[] a, int len) {
        logHexdump(tag, a, 0, len);
    }

}
