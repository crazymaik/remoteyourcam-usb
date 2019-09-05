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
import java.util.List;
import java.util.Set;

import com.remoteyourcam.usb.ptp.PtpConstants.Operation;
import com.remoteyourcam.usb.ptp.commands.SimpleCommand;
import com.remoteyourcam.usb.ptp.commands.eos.EosEventCheckCommand;
import com.remoteyourcam.usb.ptp.commands.eos.EosGetLiveViewPictureCommand;
import com.remoteyourcam.usb.ptp.commands.eos.EosOpenSessionAction;
import com.remoteyourcam.usb.ptp.commands.eos.EosSetLiveViewAction;
import com.remoteyourcam.usb.ptp.commands.eos.EosSetPropertyCommand;
import com.remoteyourcam.usb.ptp.commands.eos.EosTakePictureCommand;
import com.remoteyourcam.usb.ptp.model.LiveViewData;

public class EosCamera extends PtpCamera {

    public EosCamera(PtpUsbConnection connection, CameraListener listener, WorkerListener workerListener) {
        super(connection, listener, workerListener);

        addPropertyMapping(Camera.Property.ShutterSpeed, PtpConstants.Property.EosShutterSpeed);
        addPropertyMapping(Camera.Property.ApertureValue, PtpConstants.Property.EosApertureValue);
        addPropertyMapping(Camera.Property.IsoSpeed, PtpConstants.Property.EosIsoSpeed);
        addPropertyMapping(Camera.Property.Whitebalance, PtpConstants.Property.EosWhitebalance);
        addPropertyMapping(Camera.Property.ShootingMode, PtpConstants.Property.EosShootingMode);
        addPropertyMapping(Camera.Property.AvailableShots, PtpConstants.Property.EosAvailableShots);
        addPropertyMapping(Camera.Property.ColorTemperature, PtpConstants.Property.EosColorTemperature);
        addPropertyMapping(Camera.Property.FocusMode, PtpConstants.Property.EosAfMode);
        addPropertyMapping(Camera.Property.PictureStyle, PtpConstants.Property.EosPictureStyle);
        addPropertyMapping(Camera.Property.ExposureMeteringMode, PtpConstants.Property.EosMeteringMode);
        addPropertyMapping(Camera.Property.ExposureCompensation, PtpConstants.Property.EosExposureCompensation);

        histogramSupported = true;
    }

    @Override
    protected void onOperationCodesReceived(Set<Integer> operations) {
        if (operations.contains(Operation.EosGetLiveViewPicture)) {
            liveViewSupported = true;
        }
        if (operations.contains(Operation.EosBulbStart) && operations.contains(Operation.EosBulbEnd)) {
            bulbSupported = true;
        }
        if (operations.contains(Operation.EosDriveLens)) {
            driveLensSupported = true;
        }
        if (operations.contains(Operation.EosRemoteReleaseOn) && operations.contains(Operation.EosRemoteReleaseOff)) {
            //autoFocusSupported = true;
        }
    }

    public void onEventDirItemCreated(int objectHandle, int storageId, int objectFormat, String filename) {
        onEventObjectAdded(objectHandle, objectFormat);
    }

    @Override
    protected void openSession() {
        queue.add(new EosOpenSessionAction(this));
    }

    @Override
    protected void queueEventCheck() {
        queue.add(new EosEventCheckCommand(this));
    }

    @Override
    public void focus() {
        //queue.add(new SimpleCommand(this, Operation.EosRemoteReleaseOn, 1, 0));
    }

    @Override
    public void capture() {
        if (isBulbCurrentShutterSpeed()) {
            queue.add(new SimpleCommand(this, cameraIsCapturing ? Operation.EosBulbEnd : Operation.EosBulbStart));
        } else {
            queue.add(new EosTakePictureCommand(this));
        }
    }

    @Override
    public void setProperty(int property, int value) {
        if (properties.containsKey(property)) {
            queue.add(new EosSetPropertyCommand(this, virtualToPtpProperty.get(property), value));
        }
    }

    @Override
    public void setLiveView(boolean enabled) {
        if (liveViewSupported) {
            queue.add(new EosSetLiveViewAction(this, enabled));
        }
    }

    @Override
    public void getLiveViewPicture(LiveViewData data) {
        if (liveViewOpen) {
            queue.add(new EosGetLiveViewPictureCommand(this, data));
        }
    }

    @Override
    protected boolean isBulbCurrentShutterSpeed() {
        Integer value = ptpProperties.get(PtpConstants.Property.EosShutterSpeed);
        return bulbSupported && value != null && value == PtpPropertyHelper.EOS_SHUTTER_SPEED_BULB;
    }

    @Override
    public void driveLens(int driveDirection, int pulses) {
        if (driveLensSupported && liveViewOpen) {
            int value = driveDirection == Camera.DriveLens.Near ? 0 : 0x8000;
            switch (pulses) {
            case DriveLens.Hard:
                value |= 0x0003;
                break;
            case DriveLens.Medium:
                value |= 0x0002;
                break;
            case DriveLens.Soft:
            default:
                value |= 0x0001;
                break;
            }
            queue.add(new SimpleCommand(this, PtpConstants.Operation.EosDriveLens, value));
        }
    }

    @Override
    public boolean isSettingPropertyPossible(int property) {
        Integer mode = ptpProperties.get(PtpConstants.Property.EosShootingMode);
        Integer wb = ptpProperties.get(PtpConstants.Property.WhiteBalance);
        if (mode == null) {
            return false;
        }
        switch (property) {
        case Property.ShutterSpeed:
            return mode == 3 || mode == 1;
        case Property.ApertureValue:
            return mode == 3 || mode == 2;
        case Property.IsoSpeed:
        case Property.Whitebalance:
        case Property.ExposureMeteringMode:
            return mode >= 0 && mode <= 6;
        case Property.FocusPoints:
            return false;
        case Property.ExposureCompensation:
            return mode == 0 || mode == 1 || mode == 2 || mode == 5 || mode == 6;
        case Property.ColorTemperature:
            return wb != null && wb == 9;
        default:
            return true;
        }
    }

    @Override
    public void setLiveViewAfArea(float posx, float posy) {
        // TODO Auto-generated method stub
    }

    @Override
    public List<FocusPoint> getFocusPoints() {
        return new ArrayList<FocusPoint>();
    }
}
