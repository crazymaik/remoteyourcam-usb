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

import java.util.HashMap;
import java.util.Map;

import com.remoteyourcam.usb.R;
import com.remoteyourcam.usb.ptp.PtpConstants.Product;
import com.remoteyourcam.usb.ptp.PtpConstants.Property;

/**
 * Helper to convert property values to their string representations.
 */
public class PtpPropertyHelper {

    public static final int EOS_SHUTTER_SPEED_BULB = 0x0c;

    private static final Map<Integer, String> eosShutterSpeedMap = new HashMap<Integer, String>();
    private static final Map<Integer, String> eosApertureValueMap = new HashMap<Integer, String>();
    private static final Map<Integer, String> eosIsoSpeedMap = new HashMap<Integer, String>();
    private static final Map<Integer, String> eosWhitebalanceMap = new HashMap<Integer, String>();
    private static final Map<Integer, Integer> eosWhitebalanceIconsMap = new HashMap<Integer, Integer>();
    private static final Map<Integer, String> eosShootingModeMap = new HashMap<Integer, String>();
    private static final Map<Integer, Integer> eosShootingModeIconsMap = new HashMap<Integer, Integer>();
    private static final Map<Integer, Integer> eosMeteringModeIconsMap = new HashMap<Integer, Integer>();
    private static final Map<Integer, String> eosDriveModeMap = new HashMap<Integer, String>();
    private static final Map<Integer, Integer> eosDriveModeIconsMap = new HashMap<Integer, Integer>();
    private static final Map<Integer, String> eosFocusModeMap = new HashMap<Integer, String>();
    private static final Map<Integer, String> eosPictureStyleMap = new HashMap<Integer, String>();

    private static final Map<Integer, String> nikonWhitebalanceMap = new HashMap<Integer, String>();
    private static final Map<Integer, Integer> nikonWhitebalanceIconsMap = new HashMap<Integer, Integer>();
    private static final Map<Integer, String> nikonExposureIndexMap = new HashMap<Integer, String>();
    private static final Map<Integer, Integer> nikonExposureProgramMap = new HashMap<Integer, Integer>();
    private static final Map<Integer, String> nikonWbColorTempD300SMap = new HashMap<Integer, String>();
    private static final Map<Integer, String> nikonWbColorTempD200Map = new HashMap<Integer, String>();
    private static final Map<Integer, String> nikonFocusModeMap = new HashMap<Integer, String>();
    private static final Map<Integer, String> nikonActivePicCtrlItemMap = new HashMap<Integer, String>();
    private static final Map<Integer, Integer> nikonMeteringModeMap = new HashMap<Integer, Integer>();
    private static final Map<Integer, String> nikonFocusMeteringModeMap = new HashMap<Integer, String>();
    private static final Map<Integer, Integer> nikonFocusMeteringModeIconsMap = new HashMap<Integer, Integer>();

    public static String mapToString(int productId, int property, int value) {
        switch (property) {
        case Property.EosShutterSpeed: {
            String s = eosShutterSpeedMap.get(value);
            return s != null ? s : "?";
        }
        case Property.EosApertureValue: {
            String s = eosApertureValueMap.get(value);
            return "f " + (s != null ? s : '?');
        }
        case Property.EosIsoSpeed:
            return eosIsoSpeedMap.get(value);
        case Property.EosWhitebalance:
            return eosWhitebalanceMap.get(value);
        case Property.EosShootingMode:
            return eosShootingModeMap.get(value);
        case Property.EosDriveMode:
            return eosDriveModeMap.get(value);
        case Property.WhiteBalance:
            return nikonWhitebalanceMap.get(value);
        case Property.FNumber: {
            int major = value / 100;
            int minor = value % 100;
            if (minor == 0) {
                return "f " + major;
            } else if (minor % 10 == 0) {
                return "f " + major + '.' + minor / 10;
            } else {
                return "f " + major + '.' + minor;
            }
        }
        case Property.ExposureTime: {
            if (value == 0xFFFFFFFF) {
                return "Bulb";
            }
            int seconds = value / 10000;
            int rest = value % 10000;
            StringBuilder b = new StringBuilder();
            if (seconds > 0) {
                b.append(seconds).append("\"");
            }
            if (rest > 0) {
                b.append("1/").append(Math.round(1.0 / (rest * 0.0001)));
            }
            return b.toString();
        }
        case Property.ExposureIndex:
            return getNikonExposureIndex(productId, value);
        case Property.EosColorTemperature:
            return Integer.toString(value) + "K";
        case Property.NikonWbColorTemp:
            return getNikonWbColorTemp(productId, value);
        case Property.EosAfMode:
            return eosFocusModeMap.get(value);
        case Property.FocusMode:
            return nikonFocusModeMap.get(value);
        case Property.NikonShutterSpeed: {
            int numerator = value >> 16 & 0xffff;
            int denominator = value & 0xffff;
            if (denominator == 1) {
                return "" + numerator + "\"";
            } else if (numerator == 1) {
                return "1/" + denominator;
            } else if (value == 0xFFFFFFFF) {
                return "Bulb";
            } else if (value == 0xFFFFFFFE) {
                return "Flash";
            } else if (numerator > denominator) {
                return String.format("%.1f\"", numerator / (double) denominator);
            }
            return "" + numerator + "/" + denominator;
        }
        case Property.NikonActivePicCtrlItem:
            return nikonActivePicCtrlItemMap.get(value);
        case Property.FocusMeteringMode:
            return nikonFocusMeteringModeMap.get(value);
        case Property.NikonExposureIndicateStatus:
            return "" + value / 6 + "." + Math.abs(value) % 6 + " EV";
        case Property.EosPictureStyle:
            return eosPictureStyleMap.get(value);
        case Property.EosExposureCompensation: {
            int v;
            char ch;
            if (value > 0x80) {
                v = 0x100 - value;
                ch = '-';
            } else {
                v = value;
                ch = '+';
            }
            if (v == 0) {
                return " 0";
            }
            int first = v / 8;
            int second = v % 8;
            String dec = second == 3 ? "1/3" : second == 4 ? "1/2" : second == 5 ? "2/3" : "";
            if (first > 0) {
                return String.format("%c%d %s", ch, first, dec);
            } else {
                return String.format("%c%s", ch, dec);
            }
        }
        case Property.ExposureBiasCompensation: {
            int dec = Math.round(Math.abs(value) / 100.0f);
            int upper = dec / 10;
            int lower = dec % 10;
            char sign = value >= 0 ? '+' : '-';
            return String.format("%c%d.%d", sign, upper, lower);
        }
        default:
            return "?";
        }
    }

    private static String getNikonExposureIndex(int productId, int value) {
        switch (productId) {
        case Product.NikonD300:
        case Product.NikonD300S:
        case Product.NikonD5000:
            if (value == 0x0064) {
                return "LO-1";
            } else if (value == 0x007D) {
                return "LO-0.7";
            } else if (value == 0x00A0) {
                return "LO-0.3";
            } else if (value == 0x0FA0) {
                return "Hi-0.3";
            } else if (value == 0x1194) {
                return "Hi-0.5";
            } else if (value == 0x1388) {
                return "Hi-0.7";
            } else if (value == 0x1900) {
                return "Hi-1";
            }
            break;
        case Product.NikonD7000:
            if (value == 0x1F40) {
                return "Hi-0.3";
            } else if (value == 0x2328) {
                return "Hi-0.5";
            } else if (value == 0x2710) {
                return "Hi-0.7";
            } else if (value == 0x3200) {
                return "Hi-1";
            } else if (value == 0x6400) {
                return "Hi-2";
            }
            break;
        case Product.NikonD80:
        case Product.NikonD200:
        case Product.NikonD40:
            if (value == 0x07D0) {
                return "Hi-0.3";
            } else if (value == 0x09C4) {
                return "Hi-0.7";
            } else if (value == 0x0C80) {
                return "Hi-1";
            } else if (value == 0x0898) { // D200 only
                return "Hi-0.5";
            }
            break;
        case Product.NikonD3:
            if (value == 0x0064) {
                return "LO-1";
            } else if (value == 0x007D) {
                return "LO-0.7";
            } else if (value == 0x008C) {
                return "LO-0.5";
            } else if (value == 0x00A0) {
                return "LO-0.3";
            } else if (value == 0x2080) {
                return "Hi-0.3";
            } else if (value == 0x2300) {
                return "Hi-0.5";
            } else if (value == 0x2800) {
                return "Hi-0.7";
            } else if (value == 0x3200) {
                return "Hi-1";
            } else if (value == 0x6400) {
                return "Hi-2";
            }
            break;
        case Product.NikonD3X:
            if (value == 0x0032) {
                return "LO-1";
            } else if (value == 0x003E) {
                return "LO-0.7";
            } else if (value == 0x0046) {
                return "LO-0.5";
            } else if (value == 0x0050) {
                return "LO-0.3";
            } else if (value == 0x07D0) {
                return "Hi-0.3";
            } else if (value == 0x08C0) {
                return "Hi-0.5";
            } else if (value == 0x0A00) {
                return "Hi-0.7";
            } else if (value == 0x0C80) {
                return "Hi-1";
            } else if (value == 0x1900) {
                return "Hi-2";
            }
            break;
        case Product.NikonD3S:
            if (value == 0x0064) {
                return "LO-1";
            } else if (value == 0x007D) {
                return "LO-0.7";
            } else if (value == 0x008C) {
                return "LO-0.5";
            } else if (value == 0x00A0) {
                return "LO-0.3";
            } else if (value == 0x3840) {
                return "Hi-0.3";
            } else if (value == 0x4650) {
                return "Hi-0.5";
            } else if (value == 0x4E20) {
                return "Hi-0.7";
            } else if (value == 0x6400) {
                return "Hi-1";
            } else if (value == 0xC800) {
                return "Hi-2";
            }
            break;
        }
        return nikonExposureIndexMap.get(value);
    }

    private static String getNikonWbColorTemp(int productId, int value) {
        switch (productId) {
        case Product.NikonD300S:
        case Product.NikonD3:
        case Product.NikonD3S:
        case Product.NikonD3X:
        case Product.NikonD300:
        case Product.NikonD700:
        case Product.NikonD7000:
        case Product.NikonD90:
            return nikonWbColorTempD300SMap.get(value);
        case Product.NikonD200:
        case Product.NikonD80:
            return nikonWbColorTempD200Map.get(value);
            //case Product.NikonD60:
            //case Product.NikonD40:
            //case Product.NikonD5000:
            //    return null;
        }
        return null;
    }

    public static Integer mapToDrawable(int property, int value) {
        switch (property) {
        case Property.EosWhitebalance: {
            Integer resId = eosWhitebalanceIconsMap.get(value);
            return resId != null ? resId : R.drawable.whitebalance_unknown;
        }
        case Property.EosShootingMode: {
            Integer resId = eosShootingModeIconsMap.get(value);
            return resId != null ? resId : R.drawable.shootingmode_unknown;
        }
        case Property.EosMeteringMode: {
            Integer resId = eosMeteringModeIconsMap.get(value);
            return resId != null ? resId : R.drawable.whitebalance_unknown; //TODO own unknown image
        }
        case Property.EosDriveMode: {
            Integer resId = eosDriveModeIconsMap.get(value);
            return resId != null ? resId : R.drawable.whitebalance_unknown; //TODO own unknown image
        }
        case Property.WhiteBalance: {
            Integer resId = nikonWhitebalanceIconsMap.get(value);
            return resId != null ? resId : R.drawable.whitebalance_unknown;
        }
        case Property.ExposureProgramMode: {
            Integer resId = nikonExposureProgramMap.get(value);
            return resId != null ? resId : R.drawable.whitebalance_unknown;
        }
        case Property.ExposureMeteringMode: {
            Integer resId = nikonMeteringModeMap.get(value);
            return resId != null ? resId : R.drawable.whitebalance_unknown;
        }
        case Property.FocusMeteringMode: {
            Integer resId = nikonFocusMeteringModeIconsMap.get(value);
            return resId != null ? resId : R.drawable.whitebalance_unknown;
        }
        default:
            return null;
        }
    }

    public static String getBiggestValue(int property) {
        switch (property) {
        case Property.EosShutterSpeed:
            return "1/8000";
        case Property.EosApertureValue:
            return "f 9.5";
        case Property.EosIsoSpeed:
            return "102400";
        case Property.FNumber:
            return "33.3"; // ?
        case Property.ExposureTime:
            return "1/10000";
        case Property.ExposureIndex:
            return "LO-0.3";
        default:
            return "";
        }
    }

    static {
        eosShutterSpeedMap.put(0x0c, "Bulb");
        eosShutterSpeedMap.put(0x10, "30\"");
        eosShutterSpeedMap.put(0x13, "25\"");
        eosShutterSpeedMap.put(0x14, "20\"");
        eosShutterSpeedMap.put(0x15, "20\""); // (1/3)");
        eosShutterSpeedMap.put(0x18, "15\"");
        eosShutterSpeedMap.put(0x1B, "13\"");
        eosShutterSpeedMap.put(0x1C, "10\"");
        eosShutterSpeedMap.put(0x1D, "10\""); // (1/3)");
        eosShutterSpeedMap.put(0x20, "8\"");
        eosShutterSpeedMap.put(0x23, "6\""); // (1/3)");
        eosShutterSpeedMap.put(0x24, "6\"");
        eosShutterSpeedMap.put(0x25, "5\"");
        eosShutterSpeedMap.put(0x28, "4\"");
        eosShutterSpeedMap.put(0x2B, "3\"2");
        eosShutterSpeedMap.put(0x2C, "3\"");
        eosShutterSpeedMap.put(0x2D, "2\"5");
        eosShutterSpeedMap.put(0x25, "5\"");
        eosShutterSpeedMap.put(0x28, "4\"");
        eosShutterSpeedMap.put(0x2B, "3\"2");
        eosShutterSpeedMap.put(0x2C, "3\"");
        eosShutterSpeedMap.put(0x2D, "2\"5");
        eosShutterSpeedMap.put(0x30, "2\"");
        eosShutterSpeedMap.put(0x33, "1\"6");
        eosShutterSpeedMap.put(0x34, "1\"5");
        eosShutterSpeedMap.put(0x35, "1\"3");
        eosShutterSpeedMap.put(0x38, "1");
        eosShutterSpeedMap.put(0x3B, "0\"8");
        eosShutterSpeedMap.put(0x3C, "0\"7");
        eosShutterSpeedMap.put(0x3D, "0\"6");
        eosShutterSpeedMap.put(0x40, "0\"5");
        eosShutterSpeedMap.put(0x43, "0\"4");
        eosShutterSpeedMap.put(0x44, "0\"3");
        eosShutterSpeedMap.put(0x45, "0\"3"); // (1/3)");
        eosShutterSpeedMap.put(0x48, "1/4");
        eosShutterSpeedMap.put(0x4B, "1/5");
        eosShutterSpeedMap.put(0x4C, "1/6");
        eosShutterSpeedMap.put(0x4D, "1/6"); // (1/3)");
        eosShutterSpeedMap.put(0x50, "1/8");
        eosShutterSpeedMap.put(0x53, "1/10"); // (1/3)");
        eosShutterSpeedMap.put(0x54, "1/10");
        eosShutterSpeedMap.put(0x55, "1/13");
        eosShutterSpeedMap.put(0x58, "1/15");
        eosShutterSpeedMap.put(0x5B, "1/20"); // (1/3)");
        eosShutterSpeedMap.put(0x5C, "1/20");
        eosShutterSpeedMap.put(0x5D, "1/25");
        eosShutterSpeedMap.put(0x60, "1/30");
        eosShutterSpeedMap.put(0x63, "1/40");
        eosShutterSpeedMap.put(0x64, "1/45");
        eosShutterSpeedMap.put(0x65, "1/50");
        eosShutterSpeedMap.put(0x68, "1/60");
        eosShutterSpeedMap.put(0x6B, "1/80");
        eosShutterSpeedMap.put(0x6C, "1/90");
        eosShutterSpeedMap.put(0x6D, "1/100");
        eosShutterSpeedMap.put(0x70, "1/125");
        eosShutterSpeedMap.put(0x73, "1/160");
        eosShutterSpeedMap.put(0x74, "1/180");
        eosShutterSpeedMap.put(0x75, "1/200");
        eosShutterSpeedMap.put(0x78, "1/250");
        eosShutterSpeedMap.put(0x7B, "1/320");
        eosShutterSpeedMap.put(0x7C, "1/350");
        eosShutterSpeedMap.put(0x7D, "1/400");
        eosShutterSpeedMap.put(0x80, "1/500");
        eosShutterSpeedMap.put(0x83, "1/640");
        eosShutterSpeedMap.put(0x84, "1/750");
        eosShutterSpeedMap.put(0x85, "1/800");
        eosShutterSpeedMap.put(0x88, "1/1000");
        eosShutterSpeedMap.put(0x8B, "1/1250");
        eosShutterSpeedMap.put(0x8C, "1/1500");
        eosShutterSpeedMap.put(0x8D, "1/1600");
        eosShutterSpeedMap.put(0x90, "1/2000");
        eosShutterSpeedMap.put(0x93, "1/2500");
        eosShutterSpeedMap.put(0x94, "1/3000");
        eosShutterSpeedMap.put(0x95, "1/3200");
        eosShutterSpeedMap.put(0x98, "1/4000");
        eosShutterSpeedMap.put(0x9B, "1/5000");
        eosShutterSpeedMap.put(0x9C, "1/6000");
        eosShutterSpeedMap.put(0x9D, "1/6400");
        eosShutterSpeedMap.put(0xA0, "1/8000");

        eosApertureValueMap.put(0x08, "1");
        eosApertureValueMap.put(0x0B, "1.1");
        eosApertureValueMap.put(0x0C, "1.2");
        eosApertureValueMap.put(0x0D, "1.2"); // (1/3)");
        eosApertureValueMap.put(0x10, "1.4");
        eosApertureValueMap.put(0x13, "1.6");
        eosApertureValueMap.put(0x14, "1.8");
        eosApertureValueMap.put(0x15, "1.8"); // (1/3)");
        eosApertureValueMap.put(0x18, "2");
        eosApertureValueMap.put(0x1B, "2.2");
        eosApertureValueMap.put(0x1C, "2.5");
        eosApertureValueMap.put(0x1D, "2.5"); // (1/3)");
        eosApertureValueMap.put(0x20, "2.8");
        eosApertureValueMap.put(0x23, "3.2");
        eosApertureValueMap.put(0x24, "3.5");
        eosApertureValueMap.put(0x25, "3.5"); // (1/3)");
        eosApertureValueMap.put(0x28, "4");
        eosApertureValueMap.put(0x2B, "4.5");
        eosApertureValueMap.put(0x2C, "4.5");
        eosApertureValueMap.put(0x2D, "5.0");
        eosApertureValueMap.put(0x30, "5.6");
        eosApertureValueMap.put(0x33, "6.3");
        eosApertureValueMap.put(0x34, "6.7");
        eosApertureValueMap.put(0x35, "7.1");
        eosApertureValueMap.put(0x38, "8");
        eosApertureValueMap.put(0x3B, "9");
        eosApertureValueMap.put(0x3C, "9.5");
        eosApertureValueMap.put(0x3D, "10");
        eosApertureValueMap.put(0x40, "11");
        eosApertureValueMap.put(0x43, "13"); // (1/3)");
        eosApertureValueMap.put(0x44, "13");
        eosApertureValueMap.put(0x45, "14");
        eosApertureValueMap.put(0x48, "16");
        eosApertureValueMap.put(0x4B, "18");
        eosApertureValueMap.put(0x4C, "19");
        eosApertureValueMap.put(0x4D, "20");
        eosApertureValueMap.put(0x50, "22");
        eosApertureValueMap.put(0x53, "25");
        eosApertureValueMap.put(0x54, "27");
        eosApertureValueMap.put(0x55, "29");
        eosApertureValueMap.put(0x58, "32");
        eosApertureValueMap.put(0x5B, "36");
        eosApertureValueMap.put(0x5C, "38");
        eosApertureValueMap.put(0x5D, "40");
        eosApertureValueMap.put(0x60, "45");
        eosApertureValueMap.put(0x63, "51");
        eosApertureValueMap.put(0x64, "54");
        eosApertureValueMap.put(0x65, "57");
        eosApertureValueMap.put(0x68, "64");
        eosApertureValueMap.put(0x6B, "72");
        eosApertureValueMap.put(0x6C, "76");
        eosApertureValueMap.put(0x6D, "80");
        eosApertureValueMap.put(0x70, "91");

        eosIsoSpeedMap.put(0x00, "Auto");
        eosIsoSpeedMap.put(0x28, "6");
        eosIsoSpeedMap.put(0x30, "12");
        eosIsoSpeedMap.put(0x38, "25");
        eosIsoSpeedMap.put(0x40, "50");
        eosIsoSpeedMap.put(0x48, "100");
        eosIsoSpeedMap.put(0x4b, "125");
        eosIsoSpeedMap.put(0x4d, "160");
        eosIsoSpeedMap.put(0x50, "200");
        eosIsoSpeedMap.put(0x53, "250");
        eosIsoSpeedMap.put(0x55, "320");
        eosIsoSpeedMap.put(0x58, "400");
        eosIsoSpeedMap.put(0x5b, "500");
        eosIsoSpeedMap.put(0x5d, "640");
        eosIsoSpeedMap.put(0x60, "800");
        eosIsoSpeedMap.put(0x63, "1000");
        eosIsoSpeedMap.put(0x65, "1250");
        eosIsoSpeedMap.put(0x68, "1600");
        eosIsoSpeedMap.put(0x6b, "2000");
        eosIsoSpeedMap.put(0x6d, "2500");
        eosIsoSpeedMap.put(0x70, "3200");
        eosIsoSpeedMap.put(0x73, "4000");
        eosIsoSpeedMap.put(0x75, "5000");
        eosIsoSpeedMap.put(0x78, "6400");
        eosIsoSpeedMap.put(0x80, "12800");
        eosIsoSpeedMap.put(0x88, "25600");
        eosIsoSpeedMap.put(0x90, "51200");
        eosIsoSpeedMap.put(0x98, "102400");

        eosWhitebalanceMap.put(0, "Auto");
        eosWhitebalanceMap.put(1, "Daylight");
        eosWhitebalanceMap.put(2, "Cloudy");
        eosWhitebalanceMap.put(3, "Tungsten");
        eosWhitebalanceMap.put(4, "Fluorescent");
        eosWhitebalanceMap.put(5, "Flash");
        eosWhitebalanceMap.put(6, "Manual 1");
        eosWhitebalanceMap.put(8, "Shade");
        eosWhitebalanceMap.put(9, "Color temperature");
        eosWhitebalanceMap.put(10, "PC-1");
        eosWhitebalanceMap.put(11, "PC-2");
        eosWhitebalanceMap.put(12, "PC-3");
        eosWhitebalanceMap.put(15, "Manual 2");
        eosWhitebalanceMap.put(16, "Manual 3");
        eosWhitebalanceMap.put(18, "Manual 4");
        eosWhitebalanceMap.put(19, "Manual");
        eosWhitebalanceMap.put(20, "PC-4");
        eosWhitebalanceMap.put(21, "PC-5");

        eosWhitebalanceIconsMap.put(0, R.drawable.whitebalance_auto);
        eosWhitebalanceIconsMap.put(1, R.drawable.whitebalance_daylight);
        eosWhitebalanceIconsMap.put(2, R.drawable.whitebalance_cloudy);
        eosWhitebalanceIconsMap.put(3, R.drawable.whitebalance_tungsten);
        eosWhitebalanceIconsMap.put(4, R.drawable.whitebalance_fluorescent);
        eosWhitebalanceIconsMap.put(5, R.drawable.whitebalance_flash);
        eosWhitebalanceIconsMap.put(6, R.drawable.whitebalance_manual1);
        eosWhitebalanceIconsMap.put(8, R.drawable.whitebalance_shade);
        eosWhitebalanceIconsMap.put(9, R.drawable.whitebalance_color_temperature);
        eosWhitebalanceIconsMap.put(10, R.drawable.whitebalance_custom1);
        eosWhitebalanceIconsMap.put(11, R.drawable.whitebalance_custom2);
        eosWhitebalanceIconsMap.put(12, R.drawable.whitebalance_custom3);
        eosWhitebalanceIconsMap.put(15, R.drawable.whitebalance_manual2);
        eosWhitebalanceIconsMap.put(16, R.drawable.whitebalance_manual3);
        eosWhitebalanceIconsMap.put(18, R.drawable.whitebalance_manual4);
        eosWhitebalanceIconsMap.put(19, R.drawable.whitebalance_manual5);
        eosWhitebalanceIconsMap.put(20, R.drawable.whitebalance_custom4);
        eosWhitebalanceIconsMap.put(21, R.drawable.whitebalance_custom5);

        eosShootingModeMap.put(0, "Program AE");
        eosShootingModeMap.put(1, "Shutter-Speed Priority AE");
        eosShootingModeMap.put(2, "Aperture Priority AE");
        eosShootingModeMap.put(3, "Manual Exposure");
        eosShootingModeMap.put(4, "Bulb");
        eosShootingModeMap.put(5, "Auto Depth-of-Field AE");
        eosShootingModeMap.put(6, "Depth-of-Field AE");
        eosShootingModeMap.put(8, "Lock");
        eosShootingModeMap.put(9, "Auto");
        eosShootingModeMap.put(10, "Night Scene Portrait");
        eosShootingModeMap.put(11, "Sports");
        eosShootingModeMap.put(12, "Portrait");
        eosShootingModeMap.put(13, "Landscape");
        eosShootingModeMap.put(14, "Close-Up");
        eosShootingModeMap.put(15, "Flash Off");
        eosShootingModeMap.put(19, "Creative Auto");

        eosShootingModeIconsMap.put(0, R.drawable.shootingmode_program);
        eosShootingModeIconsMap.put(1, R.drawable.shootingmode_tv);
        eosShootingModeIconsMap.put(2, R.drawable.shootingmode_av);
        eosShootingModeIconsMap.put(3, R.drawable.shootingmode_m);
        eosShootingModeIconsMap.put(4, R.drawable.shootingmode_bulb);
        eosShootingModeIconsMap.put(5, R.drawable.shootingmode_adep);
        eosShootingModeIconsMap.put(6, R.drawable.shootingmode_dep);
        eosShootingModeIconsMap.put(8, R.drawable.shootingmode_lock);
        eosShootingModeIconsMap.put(9, R.drawable.shootingmode_auto);
        eosShootingModeIconsMap.put(10, R.drawable.shootingmode_night_scene_portrait);
        eosShootingModeIconsMap.put(11, R.drawable.shootingmode_sports);
        eosShootingModeIconsMap.put(12, R.drawable.shootingmode_portrait);
        eosShootingModeIconsMap.put(13, R.drawable.shootingmode_landscape);
        eosShootingModeIconsMap.put(14, R.drawable.shootingmode_close_up);
        eosShootingModeIconsMap.put(15, R.drawable.shootingmode_flash_off);
        eosShootingModeIconsMap.put(19, R.drawable.shootingmode_creativeauto);

        eosDriveModeMap.put(0, "Single Shooting");
        eosDriveModeMap.put(1, "Continuous Shooting");
        eosDriveModeMap.put(2, "Video");
        eosDriveModeMap.put(3, "?");
        eosDriveModeMap.put(4, "High-Speed Continuous Shooting");
        eosDriveModeMap.put(5, "Low-Speed Continuous Shooting");
        eosDriveModeMap.put(6, "Silent Single Shooting");
        eosDriveModeMap.put(7, "10-Sec Self-Timer plus Continuous Shooting");
        eosDriveModeMap.put(0x10, "10-Sec Self-Timer");
        eosDriveModeMap.put(0x11, "2-Sec Self-Timer");

        // TODO easDriveModeIconsmap

        eosFocusModeMap.put(0, "One-Shot AF");
        eosFocusModeMap.put(1, "AI Servo AF");
        eosFocusModeMap.put(2, "AI Focus AF");
        eosFocusModeMap.put(3, "Manual Focus");

        nikonWhitebalanceMap.put(2, "Auto");
        nikonWhitebalanceMap.put(4, "Sunny");
        nikonWhitebalanceMap.put(5, "Fluorescent");
        nikonWhitebalanceMap.put(6, "Incandescent");
        nikonWhitebalanceMap.put(7, "Flash");
        nikonWhitebalanceMap.put(0x8010, "Cloudy");
        nikonWhitebalanceMap.put(0x8011, "Sunny shade");
        nikonWhitebalanceMap.put(0x8012, "Color temperature");
        nikonWhitebalanceMap.put(0x8013, "Preset");

        nikonWhitebalanceIconsMap.put(2, R.drawable.whitebalance_auto);
        nikonWhitebalanceIconsMap.put(4, R.drawable.whitebalance_daylight);
        nikonWhitebalanceIconsMap.put(5, R.drawable.whitebalance_fluorescent);
        nikonWhitebalanceIconsMap.put(6, R.drawable.whitebalance_tungsten);
        nikonWhitebalanceIconsMap.put(7, R.drawable.whitebalance_flash);
        nikonWhitebalanceIconsMap.put(0x8010, R.drawable.whitebalance_cloudy);
        nikonWhitebalanceIconsMap.put(0x8011, R.drawable.whitebalance_shade);
        nikonWhitebalanceIconsMap.put(0x8012, R.drawable.whitebalance_color_temperature);
        nikonWhitebalanceIconsMap.put(0x8013, R.drawable.whitebalance_custom1); // TODO create Nikon specific icon

        nikonExposureIndexMap.put(0x0064, "100");
        nikonExposureIndexMap.put(0x007D, "125");
        nikonExposureIndexMap.put(0x00A0, "160");
        nikonExposureIndexMap.put(0x00C8, "200");
        nikonExposureIndexMap.put(0x00FA, "250");
        nikonExposureIndexMap.put(0x0118, "280");
        nikonExposureIndexMap.put(0x0140, "320");
        nikonExposureIndexMap.put(0x0190, "400");
        nikonExposureIndexMap.put(0x01F4, "500");
        nikonExposureIndexMap.put(0x0230, "560");
        nikonExposureIndexMap.put(0x0280, "640");
        nikonExposureIndexMap.put(0x0320, "800");
        nikonExposureIndexMap.put(0x03E8, "1000");
        nikonExposureIndexMap.put(0x044C, "1100");
        nikonExposureIndexMap.put(0x04E2, "1250");
        nikonExposureIndexMap.put(0x0640, "1600");
        nikonExposureIndexMap.put(0x07D0, "2000");
        nikonExposureIndexMap.put(0x0898, "2200");
        nikonExposureIndexMap.put(0x09C4, "2500");
        nikonExposureIndexMap.put(0x0C80, "3200");
        nikonExposureIndexMap.put(0x0FA0, "4000");
        nikonExposureIndexMap.put(0x1194, "4500");
        nikonExposureIndexMap.put(0x1388, "5000");
        nikonExposureIndexMap.put(0x1900, "6400");
        nikonExposureIndexMap.put(0x1F40, "8000");
        nikonExposureIndexMap.put(0x2328, "9000");
        nikonExposureIndexMap.put(0x2710, "10000");
        nikonExposureIndexMap.put(0x3200, "12800");

        nikonExposureProgramMap.put(0x0001, R.drawable.shootingmode_m);
        nikonExposureProgramMap.put(0x0002, R.drawable.shootingmode_program);
        nikonExposureProgramMap.put(0x0003, R.drawable.shootingmode_av);
        nikonExposureProgramMap.put(0x0004, R.drawable.shootingmode_tv);
        nikonExposureProgramMap.put(0x8010, R.drawable.shootingmode_auto);
        nikonExposureProgramMap.put(0x8011, R.drawable.shootingmode_portrait);
        nikonExposureProgramMap.put(0x8012, R.drawable.shootingmode_landscape);
        nikonExposureProgramMap.put(0x8013, R.drawable.shootingmode_close_up);
        nikonExposureProgramMap.put(0x8014, R.drawable.shootingmode_sports);
        nikonExposureProgramMap.put(0x8015, R.drawable.shootingmode_night_scene_portrait);
        nikonExposureProgramMap.put(0x8016, R.drawable.shootingmode_flash_off);
        nikonExposureProgramMap.put(0x8017, R.drawable.shootingmode_unknown); // TODO Child
        nikonExposureProgramMap.put(0x8018, R.drawable.shootingmode_unknown); // TODO SCENE
        nikonExposureProgramMap.put(0x8050, R.drawable.shootingmode_unknown); // TODO User mode U1
        nikonExposureProgramMap.put(0x8051, R.drawable.shootingmode_unknown); // TODO User mode U2

        nikonWbColorTempD300SMap.put(0, "2500K");
        nikonWbColorTempD300SMap.put(1, "2560K");
        nikonWbColorTempD300SMap.put(2, "2630K");
        nikonWbColorTempD300SMap.put(3, "2700K");
        nikonWbColorTempD300SMap.put(4, "2780K");
        nikonWbColorTempD300SMap.put(5, "2860K");
        nikonWbColorTempD300SMap.put(6, "2940K");
        nikonWbColorTempD300SMap.put(7, "3030K");
        nikonWbColorTempD300SMap.put(8, "3130K");
        nikonWbColorTempD300SMap.put(9, "3230K");
        nikonWbColorTempD300SMap.put(10, "3330K");
        nikonWbColorTempD300SMap.put(11, "3450K");
        nikonWbColorTempD300SMap.put(12, "3570K");
        nikonWbColorTempD300SMap.put(13, "3700K");
        nikonWbColorTempD300SMap.put(14, "3850K");
        nikonWbColorTempD300SMap.put(15, "4000K");
        nikonWbColorTempD300SMap.put(16, "4170K");
        nikonWbColorTempD300SMap.put(17, "4350K");
        nikonWbColorTempD300SMap.put(18, "4550K");
        nikonWbColorTempD300SMap.put(19, "4760K");
        nikonWbColorTempD300SMap.put(20, "5000K");
        nikonWbColorTempD300SMap.put(21, "5260K");
        nikonWbColorTempD300SMap.put(22, "5560K");
        nikonWbColorTempD300SMap.put(23, "5880K");
        nikonWbColorTempD300SMap.put(24, "6250K");
        nikonWbColorTempD300SMap.put(25, "6670K");
        nikonWbColorTempD300SMap.put(26, "7140K");
        nikonWbColorTempD300SMap.put(27, "7690K");
        nikonWbColorTempD300SMap.put(28, "8330K");
        nikonWbColorTempD300SMap.put(29, "9090K");
        nikonWbColorTempD300SMap.put(30, "10000K");

        nikonWbColorTempD200Map.put(0, "2500K");
        nikonWbColorTempD200Map.put(1, "2550K");
        nikonWbColorTempD200Map.put(2, "2650K");
        nikonWbColorTempD200Map.put(3, "2700K");
        nikonWbColorTempD200Map.put(4, "2800K");
        nikonWbColorTempD200Map.put(5, "2850K");
        nikonWbColorTempD200Map.put(6, "2950K");
        nikonWbColorTempD200Map.put(7, "3000K");
        nikonWbColorTempD200Map.put(8, "3100K");
        nikonWbColorTempD200Map.put(9, "3200K");
        nikonWbColorTempD200Map.put(10, "3300K");
        nikonWbColorTempD200Map.put(11, "3400K");
        nikonWbColorTempD200Map.put(12, "3600K");
        nikonWbColorTempD200Map.put(13, "3700K");
        nikonWbColorTempD200Map.put(14, "3800K");
        nikonWbColorTempD200Map.put(15, "4000K");
        nikonWbColorTempD200Map.put(16, "4200K");
        nikonWbColorTempD200Map.put(17, "4300K");
        nikonWbColorTempD200Map.put(18, "4500K");
        nikonWbColorTempD200Map.put(19, "4800K");
        nikonWbColorTempD200Map.put(20, "5000K");
        nikonWbColorTempD200Map.put(21, "5300K");
        nikonWbColorTempD200Map.put(22, "5600K");
        nikonWbColorTempD200Map.put(23, "5900K");
        nikonWbColorTempD200Map.put(24, "6300K");
        nikonWbColorTempD200Map.put(25, "6700K");
        nikonWbColorTempD200Map.put(26, "7100K");
        nikonWbColorTempD200Map.put(27, "7700K");
        nikonWbColorTempD200Map.put(28, "8300K");
        nikonWbColorTempD200Map.put(29, "9100K");
        nikonWbColorTempD200Map.put(30, "10000K");

        nikonFocusModeMap.put(0x0001, "Manual Focus");
        nikonFocusModeMap.put(0x8010, "Single AF servo");
        nikonFocusModeMap.put(0x8011, "Continous AF servo");
        nikonFocusModeMap.put(0x8012, "AF servo auto switch");
        nikonFocusModeMap.put(0x8013, "Constant AF servo");

        nikonActivePicCtrlItemMap.put(1, "SD");
        nikonActivePicCtrlItemMap.put(2, "NL");
        nikonActivePicCtrlItemMap.put(3, "VI");
        nikonActivePicCtrlItemMap.put(4, "MC");
        nikonActivePicCtrlItemMap.put(5, "PT");
        nikonActivePicCtrlItemMap.put(6, "LS");
        nikonActivePicCtrlItemMap.put(101, "O-1");
        nikonActivePicCtrlItemMap.put(102, "O-2");
        nikonActivePicCtrlItemMap.put(103, "O-3");
        nikonActivePicCtrlItemMap.put(104, "O-4");
        nikonActivePicCtrlItemMap.put(201, "C-1");
        nikonActivePicCtrlItemMap.put(202, "C-2");
        nikonActivePicCtrlItemMap.put(203, "C-3");
        nikonActivePicCtrlItemMap.put(204, "C-4");
        nikonActivePicCtrlItemMap.put(205, "C-5");
        nikonActivePicCtrlItemMap.put(206, "C-6");
        nikonActivePicCtrlItemMap.put(207, "C-7");
        nikonActivePicCtrlItemMap.put(208, "C-8");
        nikonActivePicCtrlItemMap.put(209, "C-9");

        nikonMeteringModeMap.put(2, R.drawable.metering_exposure_center_weighted_nikon);
        nikonMeteringModeMap.put(3, R.drawable.metering_exposure_matrix_nikon);
        nikonMeteringModeMap.put(4, R.drawable.metering_exposure_spot);

        eosMeteringModeIconsMap.put(1, R.drawable.metering_exposure_spot);
        eosMeteringModeIconsMap.put(3, R.drawable.metering_exposure_evaluative_canon);
        eosMeteringModeIconsMap.put(4, R.drawable.metering_exposure_partial);
        eosMeteringModeIconsMap.put(5, R.drawable.metering_exposure_center_weighted_average_canon);

        nikonFocusMeteringModeMap.put(0x0002, "Dynamic");
        nikonFocusMeteringModeMap.put(0x8010, "Single point");
        nikonFocusMeteringModeMap.put(0x8011, "Auto area");
        nikonFocusMeteringModeMap.put(0x8012, "3D");

        nikonFocusMeteringModeIconsMap.put(0x0002, R.drawable.metering_af_dynamic_area);
        nikonFocusMeteringModeIconsMap.put(0x8010, R.drawable.metering_af_single_point);
        nikonFocusMeteringModeIconsMap.put(0x8011, R.drawable.metering_af_auto_area);
        nikonFocusMeteringModeIconsMap.put(0x8012, R.drawable.metering_af_3d_tracking);

        eosPictureStyleMap.put(0x81, "ST");
        eosPictureStyleMap.put(0x82, "PT");
        eosPictureStyleMap.put(0x83, "LS");
        eosPictureStyleMap.put(0x84, "NL");
        eosPictureStyleMap.put(0x85, "FL");
        eosPictureStyleMap.put(0x86, "MO");
        eosPictureStyleMap.put(0x21, "UD1");
        eosPictureStyleMap.put(0x22, "UD2");
        eosPictureStyleMap.put(0x23, "UD3");
    }
}
