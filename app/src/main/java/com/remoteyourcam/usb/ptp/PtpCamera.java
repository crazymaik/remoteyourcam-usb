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
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import android.graphics.Bitmap;
import android.hardware.usb.UsbRequest;
import android.os.Handler;
import android.util.Log;

import org.acra.ErrorReporter;

import com.remoteyourcam.usb.AppConfig;
import com.remoteyourcam.usb.ptp.commands.CloseSessionCommand;
import com.remoteyourcam.usb.ptp.commands.Command;
import com.remoteyourcam.usb.ptp.commands.GetDeviceInfoCommand;
import com.remoteyourcam.usb.ptp.commands.GetDevicePropValueCommand;
import com.remoteyourcam.usb.ptp.commands.GetObjectHandlesCommand;
import com.remoteyourcam.usb.ptp.commands.GetStorageInfosAction;
import com.remoteyourcam.usb.ptp.commands.InitiateCaptureCommand;
import com.remoteyourcam.usb.ptp.commands.OpenSessionCommand;
import com.remoteyourcam.usb.ptp.commands.RetrieveImageAction;
import com.remoteyourcam.usb.ptp.commands.RetrieveImageInfoAction;
import com.remoteyourcam.usb.ptp.commands.RetrievePictureAction;
import com.remoteyourcam.usb.ptp.commands.SetDevicePropValueCommand;
import com.remoteyourcam.usb.ptp.model.DeviceInfo;
import com.remoteyourcam.usb.ptp.model.DevicePropDesc;
import com.remoteyourcam.usb.ptp.model.LiveViewData;

public abstract class PtpCamera implements Camera {

    public interface IO {
        void handleCommand(Command command);
    }

    enum State {
        // initial state
        Starting,
        // open session
        Active,
        // someone has asked to close session
        Stoping,
        // thread has stopped
        Stopped,
        // error happened
        Error
    }

    private static final String TAG = PtpCamera.class.getSimpleName();

    private final WorkerThread workerThread = new WorkerThread();
    private final PtpUsbConnection connection;

    protected final Handler handler = new Handler();
    protected final LinkedBlockingQueue<PtpAction> queue = new LinkedBlockingQueue<PtpAction>();
    protected CameraListener listener;
    protected State state;

    private int transactionId;
    protected DeviceInfo deviceInfo;

    protected boolean histogramSupported;
    protected boolean liveViewSupported;
    protected boolean liveViewAfAreaSupported;
    protected boolean liveViewOpen;

    protected boolean bulbSupported;

    protected boolean driveLensSupported;

    protected boolean autoFocusSupported;

    protected boolean cameraIsCapturing;

    protected final Map<Integer, Integer> virtualToPtpProperty = new HashMap<Integer, Integer>();
    protected final Map<Integer, Integer> ptpToVirtualProperty = new HashMap<Integer, Integer>();

    // current property values and descriptions
    protected final Map<Integer, DevicePropDesc> ptpPropertyDesc = new HashMap<Integer, DevicePropDesc>();
    protected final Map<Integer, Integer> ptpProperties = new HashMap<Integer, Integer>();
    protected final Map<Integer, Integer> properties = new HashMap<Integer, Integer>();
    private final Map<Integer, int[]> propertyDescriptions = new HashMap<Integer, int[]>();
    protected final Set<Integer> ptpInternalProperties = new HashSet<Integer>();

    private final int vendorId;
    protected final int productId;

    private WorkerListener workerListener;
    private int pictureSampleSize;

    public PtpCamera(PtpUsbConnection connection, CameraListener listener, WorkerListener workerListener) {
        this.connection = connection;
        this.listener = listener;
        this.workerListener = workerListener;
        this.pictureSampleSize = 2;
        state = State.Starting;
        vendorId = connection.getVendorId();
        productId = connection.getProductId();
        queue.add(new GetDeviceInfoCommand(this));
        openSession();
        workerThread.start();
        if (AppConfig.LOG) {
            Log.i(TAG, String.format("Starting session for %04x %04x", vendorId, productId));
        }
    }

    protected void addPropertyMapping(int virtual, int ptp) {
        ptpToVirtualProperty.put(ptp, virtual);
        virtualToPtpProperty.put(virtual, ptp);
    }

    protected void addInternalProperty(int ptp) {
        ptpInternalProperties.add(ptp);
    }

    public void setListener(CameraListener listener) {
        this.listener = listener;
    }

    public void shutdown() {
        state = State.Stoping;
        workerThread.lastEventCheck = System.currentTimeMillis() + 1000000L;
        queue.clear();
        if (liveViewOpen) {
            //TODO
            setLiveView(false);
        }
        closeSession();
    }

    public void shutdownHard() {
        state = State.Stopped;
        synchronized (workerThread) {
            workerThread.stop = true;
        }
        if (connection != null) {
            connection.close();
            //TODO possible NPE, need to join workerThread
            //connection = null;
        }
    }

    public State getState() {
        return state;
    }

    public int nextTransactionId() {
        return transactionId++;
    }

    public int currentTransactionId() {
        return transactionId;
    }

    public void resetTransactionId() {
        transactionId = 0;
    }

    public int getProductId() {
        return productId;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        if (AppConfig.LOG) {
            Log.i(TAG, deviceInfo.toString());
        }
        if (AppConfig.USE_ACRA) {
            try {
                ErrorReporter.getInstance().putCustomData("deviceInfo", deviceInfo.toString());
            } catch (Throwable e) {
                // no fail
            }
        }
        this.deviceInfo = deviceInfo;

        Set<Integer> operations = new HashSet<Integer>();
        for (int i = 0; i < deviceInfo.operationsSupported.length; ++i) {
            operations.add(deviceInfo.operationsSupported[i]);
        }

        onOperationCodesReceived(operations);
    }

    public void enqueue(final Command cmd, int delay) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (state == State.Active) {
                    queue.add(cmd);
                }
            }
        }, delay);
    }

    /**
     * Deriving classes should override this method to get the set of supported
     * operations of the camera. Based on this information functionality has to
     * be enabled/disabled.
     */
    protected abstract void onOperationCodesReceived(Set<Integer> operations);

    public int getPtpProperty(int property) {
        Integer value = ptpProperties.get(property);
        return value != null ? value : 0;
    }

    public void onSessionOpened() {
        state = State.Active;
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onCameraStarted(PtpCamera.this);
                }
            }
        });
    }

    public void onSessionClosed() {
        shutdownHard();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onCameraStopped(PtpCamera.this);
                }
            }
        });
    }

    public void onPropertyChanged(int property, final int value) {
        Log.i(TAG, "p " + property + " " + value);
        ptpProperties.put(property, value);
        final Integer virtual = ptpToVirtualProperty.get(property);
        if (AppConfig.LOG) {
            Log.d(TAG, String.format("onPropertyChanged %s %s(%d)", PtpConstants.propertyToString(property),
                    virtual != null ? propertyToString(virtual, value) : "", value));
        }
        if (virtual != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    properties.put(virtual, value);
                    if (listener != null) {
                        listener.onPropertyChanged(virtual, value);
                    }
                }
            });
        }
    }

    public void onPropertyDescChanged(int property, final int[] values) {
        //if (BuildConfig.LOG) {
            Log.d(TAG,
                    String.format("onPropertyDescChanged %s:\n%s", PtpConstants.propertyToString(property),
                            Arrays.toString(values)));
        //}
        final Integer virtual = ptpToVirtualProperty.get(property);
        if (virtual != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    propertyDescriptions.put(virtual, values);
                    if (listener != null) {
                        listener.onPropertyDescChanged(virtual, values);
                    }
                }
            });
        }
    }

    public void onPropertyDescChanged(int property, DevicePropDesc desc) {
        ptpPropertyDesc.put(property, desc);
        onPropertyDescChanged(property, desc.description);
    }

    public void onLiveViewStarted() {
        liveViewOpen = true;
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onLiveViewStarted();
                }
            }
        });
    }

    public void onLiveViewRestarted() {
        liveViewOpen = true;
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onLiveViewStarted();
                }
            }
        });
    }

    public void onLiveViewStopped() {
        liveViewOpen = false;
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onLiveViewStopped();
                }
            }
        });
    }

    public void onLiveViewReceived(final LiveViewData data) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onLiveViewData(data);
                }
            }
        });
    }

    public void onPictureReceived(final int objectHandle, final String filename, final Bitmap thumbnail,
            final Bitmap bitmap) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onCapturedPictureReceived(objectHandle, filename, thumbnail, bitmap);
                }
            }
        });
    }

    public void onEventCameraCapture(boolean started) {
        cameraIsCapturing = started;
        if (isBulbCurrentShutterSpeed()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        if (cameraIsCapturing) {
                            listener.onBulbStarted();
                        } else {
                            listener.onBulbStopped();
                        }
                    }
                }
            });
        }
    }

    public void onEventDevicePropChanged(int property) {
        if ((ptpToVirtualProperty.containsKey(property) || ptpInternalProperties.contains(property))
                && ptpPropertyDesc.containsKey(property)) {
            DevicePropDesc desc = ptpPropertyDesc.get(property);
            queue.add(new GetDevicePropValueCommand(this, property, desc.datatype));
        }
    }

    public void onEventObjectAdded(final int handle, final int format) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onObjectAdded(handle, format);
                }
            }
        });
    }

    public void onBulbExposureTime(final int seconds) {
        if (seconds >= 0 && seconds <= 360000) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.onBulbExposureTime(seconds);
                    }
                }
            });
        }
    }

    public void onFocusStarted() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onFocusStarted();
                }
            }
        });
    }

    public void onFocusEnded(final boolean hasFocused) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onFocusEnded(hasFocused);
                }
            }
        });
    }

    public void onDeviceBusy(PtpAction action, boolean requeue) {
        if (AppConfig.LOG) {
            Log.i(TAG, "onDeviceBusy, sleeping a bit");
        }
        if (requeue) {
            action.reset();
            queue.add(action);
        }
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            // nop
        }
    }

    public void onPtpWarning(final String message) {
        if (AppConfig.LOG) {
            Log.i(TAG, "onPtpWarning: " + message);
        }
    }

    public void onPtpError(final String message) {
        if (AppConfig.LOG) {
            Log.e(TAG, "onPtpError: " + message);
        }
        state = State.Error;
        if (state == State.Active) {
            shutdown();
        } else {
            shutdownHard();
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onError(message);
                }
            }
        });
    }

    private void onUsbError(final String message) {
        if (AppConfig.LOG) {
            Log.e(TAG, "onUsbError: " + message);
        }
        queue.clear();
        shutdownHard();
        state = State.Error;
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onError(String.format("Error in USB communication: %s", message));
                }
            }
        });
    }

    protected abstract void queueEventCheck();

    protected abstract boolean isBulbCurrentShutterSpeed();

    private class WorkerThread extends Thread implements IO {
        public boolean stop;

        private int maxPacketOutSize;
        private int maxPacketInSize;
        private long lastEventCheck;
        private UsbRequest r1;
        private UsbRequest r2;
        private UsbRequest r3;
        private final int bigInSize = 0x4000;
        // buffers for async data io, size bigInSize
        private ByteBuffer bigIn1;
        private ByteBuffer bigIn2;
        private ByteBuffer bigIn3;
        // buffer for small packets like command and response
        private ByteBuffer smallIn;
        // buffer containing full data out packet for processing
        private int fullInSize = 0x4000;
        private ByteBuffer fullIn;

        @Override
        public void run() {

            notifyWorkStarted();

            maxPacketOutSize = connection.getMaxPacketOutSize();
            maxPacketInSize = connection.getMaxPacketInSize();

            if (maxPacketOutSize <= 0 || maxPacketOutSize > 0xffff) {
                onUsbError(String.format("Usb initialization error: out size invalid %d", maxPacketOutSize));
                return;
            }

            if (maxPacketInSize <= 0 || maxPacketInSize > 0xffff) {
                onUsbError(String.format("usb initialization error: in size invalid %d", maxPacketInSize));
                return;
            }

            smallIn = ByteBuffer.allocate(Math.max(maxPacketInSize, maxPacketOutSize));
            smallIn.order(ByteOrder.LITTLE_ENDIAN);

            bigIn1 = ByteBuffer.allocate(bigInSize);
            bigIn1.order(ByteOrder.LITTLE_ENDIAN);
            bigIn2 = ByteBuffer.allocate(bigInSize);
            bigIn2.order(ByteOrder.LITTLE_ENDIAN);
            bigIn3 = ByteBuffer.allocate(bigInSize);
            bigIn3.order(ByteOrder.LITTLE_ENDIAN);

            fullIn = ByteBuffer.allocate(fullInSize);
            fullIn.order(ByteOrder.LITTLE_ENDIAN);

            r1 = connection.createInRequest();
            r2 = connection.createInRequest();
            r3 = connection.createInRequest();

            while (true) {
                synchronized (this) {
                    if (stop) {
                        break;
                    }
                }

                if (lastEventCheck + AppConfig.EVENTCHECK_PERIOD < System.currentTimeMillis()) {
                    lastEventCheck = System.currentTimeMillis();
                    PtpCamera.this.queueEventCheck();
                }

                PtpAction action = null;
                try {
                    action = queue.poll(1000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    // nop
                }

                if (action != null) {
                    action.exec(this);
                }
            }
            r3.close();
            r2.close();
            r1.close();

            notifyWorkEnded();
        }

        @Override
        public void handleCommand(Command command) {
            if (AppConfig.LOG) {
                Log.i(TAG, "handling command " + command.getClass().getSimpleName());
            }

            ByteBuffer b = smallIn;
            b.position(0);
            command.encodeCommand(b);

            int outLen = b.position();

            int res = connection.bulkTransferOut(b.array(), outLen, AppConfig.USB_TRANSFER_TIMEOUT);
            if (res < outLen) {
                onUsbError(String.format("Code CP %d %d", res, outLen));
                return;
            }

            if (command.hasDataToSend()) {
                b = ByteBuffer.allocate(connection.getMaxPacketOutSize());
                b.order(ByteOrder.LITTLE_ENDIAN);
                command.encodeData(b);
                outLen = b.position();
                res = connection.bulkTransferOut(b.array(), outLen, AppConfig.USB_TRANSFER_TIMEOUT);
                if (res < outLen) {
                    onUsbError(String.format("Code DP %d %d", res, outLen));
                    return;
                }
            }

            while (!command.hasResponseReceived()) {
                int maxPacketSize = maxPacketInSize;
                ByteBuffer in = smallIn;
                in.position(0);

                res = 0;
                while (res == 0) {
                    res = connection.bulkTransferIn(in.array(), maxPacketSize, AppConfig.USB_TRANSFER_TIMEOUT);
                }
                if (res < 12) {
                    onUsbError(String.format("Couldn't read header, only %d bytes available!", res));
                    return;
                }

                int read = res;
                int length = in.getInt();
                ByteBuffer infull = null;

                if (read < length) {
                    if (length > fullInSize) {
                        fullInSize = (int) (length * 1.5);
                        fullIn = ByteBuffer.allocate(fullInSize);
                        fullIn.order(ByteOrder.LITTLE_ENDIAN);
                    }
                    infull = fullIn;
                    infull.position(0);
                    infull.put(in.array(), 0, read);
                    maxPacketSize = bigInSize;

                    int nextSize = Math.min(maxPacketSize, length - read);
                    int nextSize2 = Math.max(0, Math.min(maxPacketSize, length - read - nextSize));
                    int nextSize3 = 0;

                    r1.queue(bigIn1, nextSize);

                    if (nextSize2 > 0) {
                        r2.queue(bigIn2, nextSize2);
                    }

                    while (read < length) {

                        nextSize3 = Math.max(0, Math.min(maxPacketSize, length - read - nextSize - nextSize2));

                        if (nextSize3 > 0) {
                            bigIn3.position(0);
                            r3.queue(bigIn3, nextSize3);
                        }

                        if (nextSize > 0) {
                            connection.requestWait();
                            System.arraycopy(bigIn1.array(), 0, infull.array(), read, nextSize);
                            read += nextSize;
                        }

                        nextSize = Math.max(0, Math.min(maxPacketSize, length - read - nextSize2 - nextSize3));

                        if (nextSize > 0) {
                            bigIn1.position(0);
                            r1.queue(bigIn1, nextSize);
                        }

                        if (nextSize2 > 0) {
                            connection.requestWait();
                            System.arraycopy(bigIn2.array(), 0, infull.array(), read, nextSize2);
                            read += nextSize2;
                        }

                        nextSize2 = Math.max(0, Math.min(maxPacketSize, length - read - nextSize - nextSize3));

                        if (nextSize2 > 0) {
                            bigIn2.position(0);
                            r2.queue(bigIn2, nextSize2);
                        }

                        if (nextSize3 > 0) {
                            connection.requestWait();
                            System.arraycopy(bigIn3.array(), 0, infull.array(), read, nextSize3);
                            read += nextSize3;
                        }
                    }
                } else {
                    infull = in;
                }

                infull.position(0);
                try {
                    command.receivedRead(infull);
                    infull = null;
                } catch (RuntimeException e) {
                    // TODO user could send us some data here
                    if (AppConfig.LOG) {
                        Log.e(TAG, "Exception " + e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                    onPtpError(String.format("Error parsing %s with length %d", command.getClass().getSimpleName(),
                            length));
                }
            }
        }

        private void notifyWorkStarted() {
            WorkerListener l = workerListener;
            if (l != null) {
                l.onWorkerStarted();
            }
        }

        private void notifyWorkEnded() {
            WorkerListener l = workerListener;
            if (l != null) {
                l.onWorkerEnded();
            }
        }
    }

    protected void openSession() {
        queue.add(new OpenSessionCommand(this));
    }

    protected void closeSession() {
        queue.add(new CloseSessionCommand(this));
    }

    @Override
    public void setWorkerListener(WorkerListener listener) {
        workerListener = listener;
    }

    @Override
    public String getDeviceName() {
        return deviceInfo != null ? deviceInfo.model : "";
    }

    @Override
    public boolean isSessionOpen() {
        return state == State.Active;
    }

    @Override
    public int getProperty(final int property) {
        if (properties.containsKey(property)) {
            return properties.get(property);
        }
        return 0x7fffffff;
    }

    @Override
    public boolean getPropertyEnabledState(int property) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int[] getPropertyDesc(int property) {
        if (propertyDescriptions.containsKey(property)) {
            return propertyDescriptions.get(property);
        }
        return new int[0];
    }

    @Override
    public void setProperty(int property, int value) {
        final Integer ptpProperty = virtualToPtpProperty.get(property);
        if (ptpProperty != null && ptpPropertyDesc.containsKey(ptpProperty)) {
            queue.add(new SetDevicePropValueCommand(this, ptpProperty, value, ptpPropertyDesc.get(ptpProperty).datatype));
        }
    }

    @Override
    public String propertyToString(int property, int value) {
        Integer ptpProperty = virtualToPtpProperty.get(property);
        if (ptpProperty != null) {
            String text = PtpPropertyHelper.mapToString(productId, ptpProperty, value);
            return text != null ? text : "?";
        } else {
            return "";
        }
    }

    @Override
    public Integer propertyToIcon(int property, int value) {
        Integer ptpProperty = virtualToPtpProperty.get(property);
        if (ptpProperty != null) {
            Integer iconId = PtpPropertyHelper.mapToDrawable(ptpProperty, value);
            return iconId != null ? iconId : null;
        } else {
            return null;
        }
    }

    @Override
    public String getBiggestPropertyValue(int property) {
        Integer ptpProperty = virtualToPtpProperty.get(property);
        if (ptpProperty != null) {
            return PtpPropertyHelper.getBiggestValue(ptpProperty);
        } else {
            return "";
        }
    }

    @Override
    public void capture() {
        queue.add(new InitiateCaptureCommand(this));
    }

    @Override
    public boolean isAutoFocusSupported() {
        return autoFocusSupported;
    }

    @Override
    public boolean isLiveViewSupported() {
        return liveViewSupported;
    }

    @Override
    public boolean isLiveViewAfAreaSupported() {
        return liveViewAfAreaSupported;
    }

    @Override
    public boolean isHistogramSupported() {
        return histogramSupported;
    }

    @Override
    public boolean isLiveViewOpen() {
        return liveViewOpen;
    }

    @Override
    public boolean isDriveLensSupported() {
        return driveLensSupported;
    }

    @Override
    public String getDeviceInfo() {
        return deviceInfo != null ? deviceInfo.toString() : "unknown";
    }

    @Override
    public void writeDebugInfo(File out) {
        try {
            FileWriter writer = new FileWriter(out);
            writer.append(deviceInfo.toString());
            writer.close();
        } catch (IOException e) {
        }
    }

    @Override
    public void retrievePicture(int objectHandle) {
        queue.add(new RetrievePictureAction(this, objectHandle, pictureSampleSize));
    }

    @Override
    public void retrieveStorages(StorageInfoListener listener) {
        queue.add(new GetStorageInfosAction(this, listener));
    }

    @Override
    public void retrieveImageHandles(StorageInfoListener listener, int storageId, int objectFormat) {
        queue.add(new GetObjectHandlesCommand(this, listener, storageId, objectFormat));
    }

    @Override
    public void retrieveImageInfo(RetrieveImageInfoListener listener, int objectHandle) {
        queue.add(new RetrieveImageInfoAction(this, listener, objectHandle));
    }

    @Override
    public void retrieveImage(RetrieveImageListener listener, int objectHandle) {
        queue.add(new RetrieveImageAction(this, listener, objectHandle, pictureSampleSize));
    }

    @Override
    public void setCapturedPictureSampleSize(int sampleSize) {
        this.pictureSampleSize = sampleSize;
    }

}
