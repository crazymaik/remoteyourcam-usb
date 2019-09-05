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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.remoteyourcam.usb.ptp.PtpConstants.Operation;
import com.remoteyourcam.usb.ptp.commands.GetDevicePropDescCommand;
import com.remoteyourcam.usb.ptp.commands.InitiateCaptureCommand;
import com.remoteyourcam.usb.ptp.commands.RetrieveAddedObjectInfoAction;
import com.remoteyourcam.usb.ptp.commands.SimpleCommand;
import com.remoteyourcam.usb.ptp.commands.nikon.NikonAfDriveCommand;
import com.remoteyourcam.usb.ptp.commands.nikon.NikonCloseSessionAction;
import com.remoteyourcam.usb.ptp.commands.nikon.NikonEventCheckCommand;
import com.remoteyourcam.usb.ptp.commands.nikon.NikonGetLiveViewImageAction;
import com.remoteyourcam.usb.ptp.commands.nikon.NikonGetLiveViewImageCommand;
import com.remoteyourcam.usb.ptp.commands.nikon.NikonOpenSessionAction;
import com.remoteyourcam.usb.ptp.commands.nikon.NikonStartLiveViewAction;
import com.remoteyourcam.usb.ptp.commands.nikon.NikonStopLiveViewAction;
import com.remoteyourcam.usb.ptp.model.DevicePropDesc;
import com.remoteyourcam.usb.ptp.model.LiveViewData;

public class NikonCamera extends PtpCamera {

    private Set<Integer> supportedOperations;
    private int[] vendorPropCodes = new int[0];
    private int enableAfAreaPoint;
    private boolean gotNikonShutterSpeed;
    private boolean liveViewStoppedInternal;

    public NikonCamera(PtpUsbConnection connection, CameraListener listener, WorkerListener workerListener) {
        super(connection, listener, workerListener);

        histogramSupported = false;
    }

    @Override
    protected void onOperationCodesReceived(Set<Integer> operations) {
        supportedOperations = operations;
        if (operations.contains(Operation.NikonGetLiveViewImage) && operations.contains(Operation.NikonStartLiveView)
                && operations.contains(Operation.NikonEndLiveView)) {
            liveViewSupported = true;
        }
        if (operations.contains(Operation.NikonMfDrive)) {
            driveLensSupported = true;
        }
        if (operations.contains(Operation.NikonChangeAfArea)) {
            liveViewAfAreaSupported = true;
        }
        if (operations.contains(Operation.NikonAfDrive)) {
            autoFocusSupported = true;
        }
    }

    @Override
    public void onPropertyChanged(int property, int value) {
        super.onPropertyChanged(property, value);
        if (property == PtpConstants.Property.NikonEnableAfAreaPoint) {
            enableAfAreaPoint = value;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.onFocusPointsChanged();
                    }
                }
            });
        }
    }

    @Override
    public void onPropertyDescChanged(int property, DevicePropDesc desc) {
        if (!gotNikonShutterSpeed) {
            if (property == PtpConstants.Property.NikonShutterSpeed) {
                // some cameras have this properties with only 2/3 values
                if (desc.description.length <= 4) {
                    return;
                }
                addPropertyMapping(Camera.Property.ShutterSpeed, PtpConstants.Property.NikonShutterSpeed);
                gotNikonShutterSpeed = true;
            } else if (property == PtpConstants.Property.ExposureTime) {
                addPropertyMapping(Camera.Property.ShutterSpeed, PtpConstants.Property.ExposureTime);
                gotNikonShutterSpeed = true;
            }
        }
        super.onPropertyDescChanged(property, desc);
    }

    private void onPropertyCodesReceived(Set<Integer> properties) {
        if (properties.contains(PtpConstants.Property.NikonShutterSpeed)) {
            queue.add(new GetDevicePropDescCommand(this, PtpConstants.Property.NikonShutterSpeed));
        }
        if (properties.contains(PtpConstants.Property.ExposureTime)) {
            queue.add(new GetDevicePropDescCommand(this, PtpConstants.Property.ExposureTime));
        }

        addPropertyMapping(Camera.Property.ApertureValue, PtpConstants.Property.FNumber);
        addPropertyMapping(Camera.Property.IsoSpeed, PtpConstants.Property.ExposureIndex);
        addPropertyMapping(Camera.Property.Whitebalance, PtpConstants.Property.WhiteBalance);
        addPropertyMapping(Camera.Property.ColorTemperature, PtpConstants.Property.NikonWbColorTemp);
        addPropertyMapping(Camera.Property.ShootingMode, PtpConstants.Property.ExposureProgramMode);
        addPropertyMapping(Camera.Property.BatteryLevel, PtpConstants.Property.BatteryLevel);
        //addPropertyMapping(Camera.Property.AvailableShots, PtpConstants.Property);
        addPropertyMapping(Camera.Property.FocusMode, PtpConstants.Property.FocusMode);
        addPropertyMapping(Camera.Property.PictureStyle, PtpConstants.Property.NikonActivePicCtrlItem);
        addPropertyMapping(Camera.Property.ExposureMeteringMode, PtpConstants.Property.ExposureMeteringMode);
        addPropertyMapping(Camera.Property.FocusMeteringMode, PtpConstants.Property.FocusMeteringMode);
        addPropertyMapping(Camera.Property.CurrentFocusPoint, PtpConstants.Property.NikonFocusArea);
        addPropertyMapping(Camera.Property.CurrentExposureIndicator, PtpConstants.Property.NikonExposureIndicateStatus);
        addPropertyMapping(Camera.Property.ExposureCompensation, PtpConstants.Property.ExposureBiasCompensation);

        if (properties.contains(PtpConstants.Property.NikonEnableAfAreaPoint)) {
            addInternalProperty(PtpConstants.Property.NikonEnableAfAreaPoint);
        }

        for (Integer property : properties) {
            if (ptpToVirtualProperty.containsKey(property) || ptpInternalProperties.contains(property)) {
                queue.add(new GetDevicePropDescCommand(this, property));
            }
        }
    }

    @Override
    protected void openSession() {
        queue.add(new NikonOpenSessionAction(this));
    }

    @Override
    protected void closeSession() {
        queue.add(new NikonCloseSessionAction(this));
    }

    @Override
    protected void queueEventCheck() {
        queue.add(new NikonEventCheckCommand(this));
    }

    @Override
    public void onSessionOpened() {
        super.onSessionOpened();
        Set<Integer> properties = new HashSet<Integer>();
        for (int i = 0; i < deviceInfo.devicePropertiesSupported.length; ++i) {
            properties.add(deviceInfo.devicePropertiesSupported[i]);
        }
        for (int i = 0; i < vendorPropCodes.length; ++i) {
            properties.add(vendorPropCodes[i]);
        }
        onPropertyCodesReceived(properties);
    }

    public void setVendorPropCodes(int[] vendorPropCodes) {
        this.vendorPropCodes = vendorPropCodes;
    }

    public void onEventObjectAdded(int objectHandle) {
        queue.add(new RetrieveAddedObjectInfoAction(this, objectHandle));
    }

    public void onEventCaptureComplete() {
        //TODO
    }

    public boolean hasSupportForOperation(int operation) {
        return supportedOperations.contains(operation);
    }

    @Override
    public void driveLens(int driveDirection, int pulses) {
        queue.add(new SimpleCommand(this, Operation.NikonMfDrive, driveDirection == DriveLens.Far ? 0x02 : 0x01,
                pulses * 300));
    }

    @Override
    protected boolean isBulbCurrentShutterSpeed() {
        //TODO
        return false;
    }

    public void onLiveViewStoppedInternal() {
        liveViewStoppedInternal = true;
    }

    @Override
    public void setLiveView(boolean enabled) {
        liveViewStoppedInternal = false;
        if (enabled) {
            queue.add(new NikonStartLiveViewAction(this));
        } else {
            queue.add(new NikonStopLiveViewAction(this, true));
        }
    }

    @Override
    public void getLiveViewPicture(LiveViewData reuse) {
        if (liveViewSupported && liveViewStoppedInternal) {
            liveViewStoppedInternal = false;
            queue.add(new NikonGetLiveViewImageAction(this, reuse));
        } else {
            queue.add(new NikonGetLiveViewImageCommand(this, reuse));
        }
    }

    @Override
    public boolean isSettingPropertyPossible(int property) {
        Integer mode = ptpProperties.get(PtpConstants.Property.ExposureProgramMode);
        Integer wb = ptpProperties.get(PtpConstants.Property.WhiteBalance);
        if (mode == null) {
            return false;
        }
        switch (property) {
        case Property.ShutterSpeed:
            return mode == 4 || mode == 1;
        case Property.ApertureValue:
            return mode == 3 || mode == 1;
        case Property.IsoSpeed: //TODO this should only be disabled for DIP when isoautosetting is on
        case Property.Whitebalance:
        case Property.ExposureMeteringMode:
        case Property.ExposureCompensation:
            return mode < 0x8010;
        case Property.FocusPoints:
            return true;
        case Property.ColorTemperature:
            return wb != null && wb == 0x8012;
        default:
            return true;
        }
    }

    @Override
    public void focus() {
        queue.add(new NikonAfDriveCommand(this));
    }

    @Override
    public void capture() {
        if (liveViewOpen) {
            queue.add(new NikonStopLiveViewAction(this, false));
        }
        queue.add(new InitiateCaptureCommand(this));
    }

    private int wholeWidth;
    private int wholeHeight;
    private int afAreaWidth;
    private int afAreaHeight;

    @Override
    public void onLiveViewReceived(LiveViewData data) {
        super.onLiveViewReceived(data);
        if (data != null) {
            wholeWidth = data.nikonWholeWidth;
            wholeHeight = data.nikonWholeHeight;
            afAreaWidth = data.nikonAfFrameWidth;
            afAreaHeight = data.nikonAfFrameHeight;
        }
    }

    @Override
    public void setLiveViewAfArea(float posx, float posy) {
        if (supportedOperations.contains(Operation.NikonChangeAfArea)) {
            float centerx = Math.min(wholeWidth - (afAreaWidth >> 1), Math.max(afAreaWidth >> 1, posx * wholeWidth));
            float centery = Math
                    .min(wholeHeight - (afAreaHeight >> 1), Math.max(afAreaHeight >> 1, posy * wholeHeight));
            queue.add(new SimpleCommand(this, PtpConstants.Operation.NikonChangeAfArea, (int) centerx, (int) centery));
        }
    }

    @Override
    public List<FocusPoint> getFocusPoints() {
        List<FocusPoint> points = new ArrayList<FocusPoint>();
        switch (productId) {
        case PtpConstants.Product.NikonD40:
        /* TODO no productId for D60 case PtpConstants.Product.NikonD60: */{
            points.add(new FocusPoint(0, 0.5f, 0.5f, 0.04f));
            points.add(new FocusPoint(0, 0.30f, 0.5f, 0.04f));
            points.add(new FocusPoint(0, 0.70f, 0.5f, 0.04f));
            return points;
        }
        case PtpConstants.Product.NikonD200:
        case PtpConstants.Product.NikonD80: {
            points.add(new FocusPoint(0, 0.5f, 0.5f, 0.04f));
            points.add(new FocusPoint(1, 0.5f, 0.29f, 0.04f));
            points.add(new FocusPoint(2, 0.5f, 0.71f, 0.04f));
            points.add(new FocusPoint(3, 0.33f, 0.5f, 0.04f));
            points.add(new FocusPoint(4, 0.67f, 0.5f, 0.04f));
            points.add(new FocusPoint(5, 0.22f, 0.5f, 0.04f));
            points.add(new FocusPoint(6, 0.78f, 0.5f, 0.04f));
            points.add(new FocusPoint(7, 0.33f, 0.39f, 0.04f));
            points.add(new FocusPoint(8, 0.67f, 0.39f, 0.04f));
            points.add(new FocusPoint(9, 0.33f, 0.61f, 0.04f));
            points.add(new FocusPoint(10, 0.67f, 0.61f, 0.04f));
            return points;
        }
        case PtpConstants.Product.NikonD5000:
        case PtpConstants.Product.NikonD90: {
            points.add(new FocusPoint(1, 0.5f, 0.5f, 0.04f));
            points.add(new FocusPoint(2, 0.5f, 0.3f, 0.04f));
            points.add(new FocusPoint(3, 0.5f, 0.7f, 0.04f));
            points.add(new FocusPoint(4, 0.33f, 0.5f, 0.04f));
            points.add(new FocusPoint(5, 0.33f, 0.35f, 0.04f));
            points.add(new FocusPoint(6, 0.33f, 0.65f, 0.04f));
            points.add(new FocusPoint(7, 0.22f, 0.5f, 0.04f));
            points.add(new FocusPoint(8, 0.67f, 0.5f, 0.04f));
            points.add(new FocusPoint(9, 0.67f, 0.35f, 0.04f));
            points.add(new FocusPoint(10, 0.67f, 0.65f, 0.04f));
            points.add(new FocusPoint(11, 0.78f, 0.5f, 0.04f));
            return points;
        }
        case PtpConstants.Product.NikonD300:
            //case PtpConstants.Product.NikonD700: same id as d300
        case PtpConstants.Product.NikonD300S:
        case PtpConstants.Product.NikonD3:
        case PtpConstants.Product.NikonD3S:
        case PtpConstants.Product.NikonD3X: {
            points.add(new FocusPoint(1, 0.5f, 0.5f, 0.035f));
            points.add(new FocusPoint(3, 0.5f, 0.36f, 0.035f));
            points.add(new FocusPoint(5, 0.5f, 0.64f, 0.035f));

            points.add(new FocusPoint(21, 0.65f, 0.5f, 0.035f));
            points.add(new FocusPoint(23, 0.65f, 0.4f, 0.035f));
            points.add(new FocusPoint(25, 0.65f, 0.6f, 0.035f));
            points.add(new FocusPoint(31, 0.75f, 0.5f, 0.035f));

            points.add(new FocusPoint(39, 0.35f, 0.5f, 0.035f));
            points.add(new FocusPoint(41, 0.35f, 0.4f, 0.035f));
            points.add(new FocusPoint(43, 0.35f, 0.6f, 0.035f));
            points.add(new FocusPoint(49, 0.25f, 0.5f, 0.035f));

            if (enableAfAreaPoint == 0) {
                //TODO has more points when EnableAFAreaPoint is 0
            }
            return points;
        }
        case PtpConstants.Product.NikonD7000: {
            points.add(new FocusPoint(1, 0.5f, 0.5f, 0.035f));
            points.add(new FocusPoint(3, 0.5f, 0.32f, 0.035f));
            points.add(new FocusPoint(5, 0.5f, 0.68f, 0.035f));

            points.add(new FocusPoint(19, 0.68f, 0.5f, 0.035f));
            points.add(new FocusPoint(20, 0.68f, 0.4f, 0.035f));
            points.add(new FocusPoint(21, 0.68f, 0.6f, 0.035f));
            points.add(new FocusPoint(25, 0.80f, 0.5f, 0.035f));

            points.add(new FocusPoint(31, 0.32f, 0.5f, 0.035f));
            points.add(new FocusPoint(32, 0.32f, 0.4f, 0.035f));
            points.add(new FocusPoint(33, 0.32f, 0.6f, 0.035f));
            points.add(new FocusPoint(37, 0.20f, 0.5f, 0.035f));

            if (enableAfAreaPoint == 0) {
                //TODO has more points when EnableAFAreaPoint is 0
            }
            return points;
        }
        default:
            return points;
        }
    }
}
