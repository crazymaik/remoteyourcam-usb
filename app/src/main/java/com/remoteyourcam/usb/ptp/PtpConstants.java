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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class PtpConstants {

    public static final int CanonVendorId = 0x04a9;
    public static final int NikonVendorId = 0x04b0;

    public static boolean isCompatibleVendor(int vendorId) {
        return vendorId == CanonVendorId || vendorId == NikonVendorId;
    }

    public static class Product {
        // TODO D60 seems not to have a unique id
        //public static final int NikonD700 = 0x041A; // Same as D300?
        public static final int NikonD300 = 0x041A;
        public static final int NikonD300S = 0x0425;
        public static final int NikonD5000 = 0x0423;
        public static final int NikonD5100 = 0x0429;
        public static final int NikonD7000 = 0x0428;
        public static final int NikonD80 = 0x0412;
        public static final int NikonD200 = 0x0410;
        public static final int NikonD3 = 0x041C;
        public static final int NikonD3S = 0x0426;
        public static final int NikonD3X = 0x0420;
        public static final int NikonD40 = 0x0414;
        public static final int NikonD90 = 0x0421;
        public static final int NikonD700 = 0x0422;
    }

    public static class Type {
        public static final int Undefined = 0;
        public static final int Command = 1;
        public static final int Data = 2;
        public static final int Response = 3;
        public static final int Event = 4;
    }

    public static String typeToString(int type) {
        return constantToString(Type.class, type);
    }

    public static class Operation {
        public static final int UndefinedOperationCode = 0x1000;
        public static final int GetDeviceInfo = 0x1001;
        public static final int OpenSession = 0x1002;
        public static final int CloseSession = 0x1003;
        public static final int GetStorageIDs = 0x1004;
        public static final int GetStorageInfo = 0x1005;
        public static final int GetNumObjects = 0x1006;
        public static final int GetObjectHandles = 0x1007;
        public static final int GetObjectInfo = 0x1008;
        public static final int GetObject = 0x1009;
        public static final int GetThumb = 0x100A;
        public static final int DeleteObject = 0x100B;
        public static final int SendObjectInfo = 0x100C;
        public static final int SendObject = 0x100D;
        public static final int InitiateCapture = 0x100E;
        public static final int FormatStore = 0x100F;
        public static final int ResetDevice = 0x1010;
        public static final int SelfTest = 0x1011;
        public static final int SetObjectProtection = 0x1012;
        public static final int PowerDown = 0x1013;
        public static final int GetDevicePropDesc = 0x1014;
        public static final int GetDevicePropValue = 0x1015;
        public static final int SetDevicePropValue = 0x1016;
        public static final int ResetDevicePropValue = 0x1017;
        public static final int TerminateOpenCapture = 0x1018;
        public static final int MoveObject = 0x1019;
        public static final int CopyObject = 0x101A;
        public static final int GetPartialObject = 0x101B;
        public static final int InitiateOpenCapture = 0x101C;

        public static final int NikonInitiateCaptureRecInSdram = 0x90C0;
        public static final int NikonAfDrive = 0x90C1;
        public static final int NikonChangeCameraMode = 0x90C2;
        public static final int NikonDeleteImagesInSdram = 0x90C3;
        public static final int NikonGetLargeThumb = 0x90C4;
        public static final int NikonGetEvent = 0x90C7;
        public static final int NikonDeviceReady = 0x90C8;
        public static final int NikonSetPreWbData = 0x90C9;
        public static final int NikonGetVendorPropCodes = 0x90CA;
        public static final int NikonAfAndCaptureInSdram = 0x90CB;
        public static final int NikonGetPicCtrlData = 0x90CC;
        public static final int NikonSetPicCtrlData = 0x90CD;
        public static final int NikonDeleteCustomPicCtrl = 0x90CE;
        public static final int NikonGetPicCtrlCapability = 0x90CF;
        public static final int NikonGetPreviewImage = 0x9200;
        public static final int NikonStartLiveView = 0x9201;
        public static final int NikonEndLiveView = 0x9202;
        public static final int NikonGetLiveViewImage = 0x9203;
        public static final int NikonMfDrive = 0x9204;
        public static final int NikonChangeAfArea = 0x9205;
        public static final int NikonAfDriveCancel = 0x9206;
        public static final int NikonInitiateCaptureRecInMedia = 0x9207;
        public static final int NikonGetObjectPropsSupported = 0x9801;
        public static final int NikonGetObjectPropDesc = 0x9802;
        public static final int NikonGetObjectPropValue = 0x9803;
        public static final int NikonGetObjectPropList = 0x9805;

        // Canon EOS
        public static final int EosTakePicture = 0x910F;
        public static final int EosSetDevicePropValue = 0x9110;
        public static final int EosSetPCConnectMode = 0x9114;
        public static final int EosSetEventMode = 0x9115;
        public static final int EosEventCheck = 0x9116;
        public static final int EosTransferComplete = 0x9117;
        public static final int EosResetTransfer = 0x9119;
        public static final int EosBulbStart = 0x9125;
        public static final int EosBulbEnd = 0x9126;
        public static final int EosGetDevicePropValue = 0x9127;
        public static final int EosRemoteReleaseOn = 0x9128;
        public static final int EosRemoteReleaseOff = 0x9129;
        public static final int EosGetLiveViewPicture = 0x9153;
        public static final int EosDriveLens = 0x9155;
    }

    public static String operationToString(int operation) {
        return constantToString(Operation.class, operation);
    }

    public static class Event {
        public static final int CancelTransaction = 0x4001;
        public static final int ObjectAdded = 0x4002;
        public static final int ObjectRemoved = 0x4003;
        public static final int StoreAdded = 0x4004;
        public static final int StoreRemoved = 0x4005;
        public static final int DevicePropChanged = 0x4006;
        public static final int ObjectInfoChanged = 0x4007;
        public static final int DeviceInfoChanged = 0x4008;
        public static final int RequestObjectTransfer = 0x4009;
        public static final int StoreFull = 0x400A;
        public static final int StorageInfoChanged = 0x400C;
        public static final int CaptureComplete = 0x400D;

        // Nikon
        public static final int NikonObjectAddedInSdram = 0xC101;
        public static final int NikonCaptureCompleteRecInSdram = 0xC102;
        public static final int NikonPreviewImageAdded = 0xC104;

        // Canon EOS
        public static final int EosObjectAdded = 0xC181; // ? dir item request transfer or dir item created
        public static final int EosDevicePropChanged = 0xC189;
        public static final int EosDevicePropDescChanged = 0xC18A;
        public static final int EosCameraStatus = 0xC18B;
        public static final int EosWillSoonShutdown = 0xC18D;
        public static final int EosBulbExposureTime = 0xc194;

    }

    public static String eventToString(int event) {
        return constantToString(Event.class, event);
    }

    public static class Response {
        public static final int Ok = 0x2001;
        public static final int GeneralError = 0x2002;
        public static final int SessionNotOpen = 0x2003;
        public static final int InvalidTransactionID = 0x2004;
        public static final int OperationNotSupported = 0x2005;
        public static final int ParameterNotSupported = 0x2006;
        public static final int IncompleteTransfer = 0x2007;
        public static final int InvalidStorageID = 0x2008;
        public static final int InvalidObjectHandle = 0x2009;
        public static final int DevicePropNotSupported = 0x200A;
        public static final int InvalidObjectFormatCode = 0x200B;
        public static final int StoreIsFull = 0x200C;
        public static final int ObjectWriteProtect = 0x200D;
        public static final int StoreReadOnly = 0x200E;
        public static final int AccessDenied = 0x200F;
        public static final int NoThumbnailPresent = 0x2010;
        public static final int PartialDeletion = 0x2012;
        public static final int StoreNotAvailable = 0x2013;
        public static final int SpecificationByFormatUnsupported = 0x2014;
        public static final int NoValidObjectInfo = 0x2015;
        public static final int DeviceBusy = 0x2019;
        public static final int InvalidParentObject = 0x201A;
        public static final int InvalidDevicePropFormat = 0x201B;
        public static final int InvalidDevicePropValue = 0x201C;
        public static final int InvalidParameter = 0x201D;
        public static final int SessionAlreadyOpen = 0x201E;
        public static final int TransferCancelled = 0x201F;
        public static final int SpecificationOfDestinationUnsupported = 0x2020;

        // Nikon ?
        public static final int HardwareError = 0xA001;
        public static final int OutOfFocus = 0xA002;
        public static final int ChangeCameraModeFailed = 0xA003;
        public static final int InvalidStatus = 0xA004;
        public static final int SetPropertyNotSupport = 0xA005;
        public static final int WbPresetError = 0xA006;
        public static final int DustReferenceError = 0xA007;
        public static final int ShutterSpeedBulb = 0xA008;
        public static final int MirrorUpSequence = 0xA009;
        public static final int CameraModeNotAdjustFnumber = 0xA00A;
        public static final int NotLiveView = 0xA00B;
        public static final int MfDriveStepEnd = 0xA00C;
        public static final int MfDriveStepInsufficiency = 0xA00E;
        public static final int InvalidObjectPropCode = 0xA801;
        public static final int InvalidObjectPropFormat = 0xA802;
        public static final int ObjectPropNotSupported = 0xA80A;

        // Canon EOS
        public static final int EosUnknown_MirrorUp = 0xA102; // ?
    }

    public static String responseToString(int response) {
        return constantToString(Response.class, response);
    }

    public static class ObjectFormat {
        public static final int UnknownNonImageObject = 0x3000;
        public static final int Association = 0x3001;
        public static final int Script = 0x3002;
        public static final int Executable = 0x3003;
        public static final int Text = 0x3004;
        public static final int HTML = 0x3005;
        public static final int DPOF = 0x3006;
        public static final int AIFF = 0x3007;
        public static final int WAV = 0x3008;
        public static final int MP3 = 0x3009;
        public static final int AVI = 0x300A;
        public static final int MPEG = 0x300B;
        public static final int ASF = 0x300C;
        public static final int UnknownImageObject = 0x3800;
        public static final int EXIF_JPEG = 0x3801;
        public static final int TIFF_EP = 0x3802;
        public static final int FlashPix = 0x3803;
        public static final int BMP = 0x3804;
        public static final int CIFF = 0x3805;
        public static final int Undefined_Reserved1 = 0x3806;
        public static final int GIF = 0x3807;
        public static final int JFIF = 0x3808;
        public static final int PCD = 0x3809;
        public static final int PICT = 0x380A;
        public static final int PNG = 0x380B;
        public static final int Undefined_Reserved2 = 0x380C;
        public static final int TIFF = 0x380D;
        public static final int TIFF_IT = 0x380E;
        public static final int JP2 = 0x380F;
        public static final int JPX = 0x3810;

        // Canon
        public static final int EosCRW = 0xb101;
        public static final int EosCRW3 = 0xb103;
        public static final int EosMOV = 0xb104;
    }

    public static String objectFormatToString(int objectFormat) {
        return constantToString(ObjectFormat.class, objectFormat);
    }

    public static class Property {
        // PTP
        public static final int UndefinedProperty = 0x5000;
        public static final int BatteryLevel = 0x5001;
        public static final int FunctionalMode = 0x5002;
        public static final int ImageSize = 0x5003;
        public static final int CompressionSetting = 0x5004;
        public static final int WhiteBalance = 0x5005;
        public static final int RGBGain = 0x5006;
        public static final int FNumber = 0x5007; // Aperture Value
        public static final int FocalLength = 0x5008;
        public static final int FocusDistance = 0x5009;
        public static final int FocusMode = 0x500A;
        public static final int ExposureMeteringMode = 0x500B;
        public static final int FlashMode = 0x500C;
        public static final int ExposureTime = 0x500D; // Shutter Speed
        public static final int ExposureProgramMode = 0x500E;
        public static final int ExposureIndex = 0x500F; // ISO Speed
        public static final int ExposureBiasCompensation = 0x5010;
        public static final int DateTime = 0x5011;
        public static final int CaptureDelay = 0x5012;
        public static final int StillCaptureMode = 0x5013;
        public static final int Contrast = 0x5014;
        public static final int Sharpness = 0x5015;
        public static final int DigitalZoom = 0x5016;
        public static final int EffectMode = 0x5017;
        public static final int BurstNumber = 0x5018;
        public static final int BurstInterval = 0x5019;
        public static final int TimelapseNumber = 0x501A;
        public static final int TimelapseInterval = 0x501B;
        public static final int FocusMeteringMode = 0x501C;
        public static final int UploadURL = 0x501D;
        public static final int Artist = 0x501E;
        public static final int CopyrightInfo = 0x501F;

        // MTP/Microsoft
        public static final int MtpDeviceFriendlyName = 0xD402;
        public static final int MtpSessionInitiatorInfo = 0xD406;
        public static final int MtpPerceivedDeviceType = 0xD407;

        // Canon EOS
        public static final int EosApertureValue = 0xD101;
        public static final int EosShutterSpeed = 0xD102;
        public static final int EosIsoSpeed = 0xD103;
        public static final int EosExposureCompensation = 0xD104;
        public static final int EosShootingMode = 0xD105;
        public static final int EosDriveMode = 0xD106;
        public static final int EosMeteringMode = 0xD107;
        public static final int EosAfMode = 0xD108;
        public static final int EosWhitebalance = 0xD109;
        public static final int EosColorTemperature = 0xD10A;
        public static final int EosPictureStyle = 0xD110;
        public static final int EosAvailableShots = 0xD11B;
        public static final int EosEvfOutputDevice = 0xD1B0;
        public static final int EosEvfMode = 0xD1B3;
        public static final int EosEvfWhitebalance = 0xD1B4;
        public static final int EosEvfColorTemperature = 0xD1B6;

        // Nikon
        public static final int NikonShutterSpeed = 0xD100;
        public static final int NikonFocusArea = 0xD108;
        public static final int NikonWbColorTemp = 0xD01E;
        public static final int NikonRecordingMedia = 0xD10B;
        public static final int NikonExposureIndicateStatus = 0xD1B1;
        public static final int NikonActivePicCtrlItem = 0xD200;
        public static final int NikonEnableAfAreaPoint = 0xD08D;
    }

    public static String propertyToString(int property) {
        return constantToString(Property.class, property);
    }

    public static class Datatype {
        public static final int int8 = 0x0001;
        public static final int uint8 = 0x002;
        public static final int int16 = 0x003;
        public static final int uint16 = 0x004;
        public static final int int32 = 0x005;
        public static final int uint32 = 0x006;
        public static final int int64 = 0x007;
        public static final int uint64 = 0x008;
        public static final int int128 = 0x009;
        public static final int uint128 = 0x00A;
        public static final int aint8 = 0x4001;
        public static final int auint8 = 0x4002;
        public static final int aint16 = 0x4003;
        public static final int auInt16 = 0x4004;
        public static final int aint32 = 0x4005;
        public static final int auint32 = 0x4006;
        public static final int aint64 = 0x4007;
        public static final int auint64 = 0x4008;
        public static final int aint128 = 0x4009;
        public static final int auint128 = 0x400A;
        public static final int string = 0x00;
    }

    public static String datatypetoString(int datatype) {
        return constantToString(Datatype.class, datatype);
    }

    public static int getDatatypeSize(int datatype) {
        switch (datatype) {
        case Datatype.int8:
        case Datatype.uint8:
            return 1;
        case Datatype.int16:
        case Datatype.uint16:
            return 2;
        case Datatype.int32:
        case Datatype.uint32:
            return 4;
        case Datatype.int64:
        case Datatype.uint64:
            return 8;
        default:
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Returns a string representation of the code field an PTP packet.
     */
    public static String codeToString(int type, int code) {
        switch (type) {
        case Type.Command:
        case Type.Data:
            return operationToString(code);
        case Type.Response:
            return responseToString(code);
        case Type.Event:
            return eventToString(code);
        default:
            return String.format("0x%04x", code);
        }
    }

    /**
     * Returns the name of the constant that has the specified {@code constant}
     * in the specified {@code clazz}.
     */
    public static String constantToString(Class<?> clazz, int constant) {
        String hexString = String.format("0x%04x", constant);
        for (Field f : clazz.getDeclaredFields()) {
            if (f.getType() != int.class || !Modifier.isStatic(f.getModifiers()) || !Modifier.isFinal(f.getModifiers())) {
                continue;
            }
            try {
                if (f.getInt(null) == constant) {
                    return f.getName() + "(" + hexString + ")";
                }
            } catch (Throwable e) {
                // nop
                e.printStackTrace();
            }
        }
        return hexString;
    }

    /**
     * Reads {@code DeviceInfo.toString} from input and rewrites codes to
     * names(codes).
     *
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
        String line = null;
        int state = 0;

        while ((line = reader.readLine()) != null) {
            if ("OperationsSupported:".equals(line)) {
                state = 1;
                writer.write(line);
            } else if ("EventsSupported:".equals(line)) {
                state = 2;
                writer.write(line);
            } else if ("DevicePropertiesSupported:".equals(line)) {
                state = 3;
                writer.write(line);
            } else if ("CaptureFormats:".equals(line)) {
                state = 4;
                writer.write(line);
            } else if ("ImageFormats:".equals(line)) {
                state = 5;
                writer.write(line);
            } else {
                if (line.startsWith("    0x") || line.matches("    .+\\)$")) {
                    if (line.startsWith("    0x")) {
                        line = line.trim().substring(2);
                    } else {
                        int bracket = line.indexOf('(');
                        line = line.substring(bracket + 3, line.length() - 1);
                    }
                    int number = Integer.parseInt(line, 16);
                    String value = null;
                    switch (state) {
                    case 1:
                        value = operationToString(number);
                        break;
                    case 2:
                        value = eventToString(number);
                        break;
                    case 3:
                        value = propertyToString(number);
                        break;
                    case 4:
                    case 5:
                        value = objectFormatToString(number);
                        break;
                    }
                    writer.write(String.format("    %s", value));
                } else {
                    writer.write(line);
                }
            }
             writer.newLine();
        }

        writer.flush();
    }
}
