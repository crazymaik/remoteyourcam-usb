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

import java.io.File;
import java.util.List;

import android.graphics.Bitmap;

import com.remoteyourcam.usb.ptp.model.LiveViewData;
import com.remoteyourcam.usb.ptp.model.ObjectInfo;

public interface Camera {

    public static class Property {
        public static final int ShutterSpeed = 1;
        public static final int ApertureValue = 2;
        public static final int IsoSpeed = 3;
        public static final int Whitebalance = 4;
        public static final int ShootingMode = 5;
        public static final int BatteryLevel = 6;
        public static final int AvailableShots = 7;
        public static final int ColorTemperature = 8;
        public static final int FocusMode = 9;
        public static final int PictureStyle = 10;
        public static final int ExposureMeteringMode = 11;
        public static final int FocusMeteringMode = 12;
        public static final int CurrentFocusPoint = 13;
        public static final int CurrentExposureIndicator = 14;
        public static final int FocusPoints = 15;
        public static final int ExposureCompensation = 16;
    }

    public static class DriveLens {
        public static final int Near = 1;
        public static final int Far = 2;
        public static final int Soft = 1;
        public static final int Medium = 2;
        public static final int Hard = 3;
    }

    public interface CameraListener {

        void onCameraStarted(Camera camera);

        void onCameraStopped(Camera camera);

        void onNoCameraFound();

        void onError(String message);

        void onPropertyChanged(int property, int value);

        void onPropertyStateChanged(int property, boolean enabled);

        void onPropertyDescChanged(int property, int[] values);

        void onLiveViewStarted();

        void onLiveViewData(LiveViewData data);

        void onLiveViewStopped();

        void onCapturedPictureReceived(int objectHandle, String filename, Bitmap thumbnail, Bitmap bitmap);

        void onBulbStarted();

        void onBulbExposureTime(int seconds);

        void onBulbStopped();

        void onFocusStarted();

        void onFocusEnded(boolean hasFocused);

        void onFocusPointsChanged();

        void onObjectAdded(int handle, int format);
    }

    // callbacks aren't on UI thread
    public interface WorkerListener {
        void onWorkerStarted();

        void onWorkerEnded();
    }

    // callbacks aren't on UI thread
    public interface StorageInfoListener {
        void onStorageFound(int handle, String label);

        void onAllStoragesFound();

        void onImageHandlesRetrieved(int[] handles);
    }

    public interface RetrieveImageInfoListener {
        void onImageInfoRetrieved(int objectHandle, ObjectInfo objectInfo, Bitmap thumbnail);
    }

    public interface RetrieveImageListener {
        void onImageRetrieved(int objectHandle, Bitmap image);
    }

    void setWorkerListener(WorkerListener listener);

    void setCapturedPictureSampleSize(int sampleSize);

    String getDeviceName();

    boolean isSessionOpen();

    int getProperty(int property);

    boolean getPropertyEnabledState(int property);

    int[] getPropertyDesc(int property);

    void setProperty(int property, int value);

    String propertyToString(int property, int value);

    Integer propertyToIcon(int property, int value);

    String getBiggestPropertyValue(int property);

    void focus();

    boolean isAutoFocusSupported();

    void capture();

    boolean isLiveViewSupported();

    boolean isLiveViewAfAreaSupported();

    boolean isHistogramSupported();

    boolean isLiveViewOpen();

    void setLiveView(boolean enabled);

    void getLiveViewPicture(LiveViewData reuse);

    /**
     * Sets the center point of the auto focus frame in Live view.
     *
     * @param posx
     *            range 0.0 (left) - 1.0 (right)
     * @param posy
     *            range 0.0 (top) - 1.0 (bottom)
     */
    void setLiveViewAfArea(float posx, float posy);

    boolean isDriveLensSupported();

    void driveLens(int driveDirection, int pulses);

    boolean isSettingPropertyPossible(int property);

    void writeDebugInfo(File out);

    String getDeviceInfo();

    List<FocusPoint> getFocusPoints();

    void retrievePicture(int objectHandle);

    void retrieveStorages(StorageInfoListener listener);

    void retrieveImageHandles(StorageInfoListener listener, int storageId, int objectFormat);

    void retrieveImageInfo(RetrieveImageInfoListener listener, int objectHandle);

    void retrieveImage(RetrieveImageListener listener, int objectHandle);
}
