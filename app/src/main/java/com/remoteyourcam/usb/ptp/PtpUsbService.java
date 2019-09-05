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

import java.util.Map;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.util.Log;

import com.remoteyourcam.usb.AppConfig;
import com.remoteyourcam.usb.ptp.Camera.CameraListener;
import com.remoteyourcam.usb.ptp.PtpCamera.State;

public class PtpUsbService implements PtpService {

    private final String TAG = PtpUsbService.class.getSimpleName();

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private final BroadcastReceiver permissonReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                unregisterPermissionReceiver(context);
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        connect(context, device);
                    } else {
                        //TODO report
                    }
                }
            }
        }
    };

    private final Handler handler = new Handler();
    private final UsbManager usbManager;
    private PtpCamera camera;
    private CameraListener listener;

    Runnable shutdownRunnable = new Runnable() {
        @Override
        public void run() {
            shutdown();
        }
    };

    public PtpUsbService(Context context) {
        this.usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
    }

    @Override
    public void setCameraListener(CameraListener listener) {
        this.listener = listener;
        if (camera != null) {
            camera.setListener(listener);
        }
    }

    @Override
    public void initialize(Context context, Intent intent) {
        handler.removeCallbacks(shutdownRunnable);
        if (camera != null) {
            if (AppConfig.LOG) {
                Log.i(TAG, "initialize: camera available");
            }
            if (camera.getState() == State.Active) {
                if (listener != null) {
                    listener.onCameraStarted(camera);
                }
                return;
            }
            if (AppConfig.LOG) {
                Log.i(TAG, "initialize: camera not active, satet " + camera.getState());
            }
            camera.shutdownHard();
        }
        UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if (device != null) {
            if (AppConfig.LOG) {
                Log.i(TAG, "initialize: got device through intent");
            }
            connect(context, device);
        } else {
            if (AppConfig.LOG) {
                Log.i(TAG, "initialize: looking for compatible camera");
            }
            device = lookupCompatibleDevice(usbManager);
            if (device != null) {
                registerPermissionReceiver(context);
                PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(
                        ACTION_USB_PERMISSION), 0);
                usbManager.requestPermission(device, mPermissionIntent);
            } else {
                listener.onNoCameraFound();
            }
        }
    }

    @Override
    public void shutdown() {
        if (AppConfig.LOG) {
            Log.i(TAG, "shutdown");
        }
        if (camera != null) {
            camera.shutdown();
            camera = null;
        }
    }

    @Override
    public void lazyShutdown() {
        if (AppConfig.LOG) {
            Log.i(TAG, "lazy shutdown");
        }
        handler.postDelayed(shutdownRunnable, 4000);
    }

    private void registerPermissionReceiver(Context context) {
        if (AppConfig.LOG) {
            Log.i(TAG, "register permission receiver");
        }
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        context.registerReceiver(permissonReceiver, filter);
    }

    private void unregisterPermissionReceiver(Context context) {
        if (AppConfig.LOG) {
            Log.i(TAG, "unregister permission receiver");
        }
        context.unregisterReceiver(permissonReceiver);
    }

    private UsbDevice lookupCompatibleDevice(UsbManager manager) {
        Map<String, UsbDevice> deviceList = manager.getDeviceList();
        for (Map.Entry<String, UsbDevice> e : deviceList.entrySet()) {
            UsbDevice d = e.getValue();
            if (d.getVendorId() == PtpConstants.CanonVendorId || d.getVendorId() == PtpConstants.NikonVendorId) {
                return d;
            }
        }
        return null;
    }

    private boolean connect(Context context, UsbDevice device) {
        if (camera != null) {
            camera.shutdown();
            camera = null;
        }
        for (int i = 0, n = device.getInterfaceCount(); i < n; ++i) {
            UsbInterface intf = device.getInterface(i);

            if (intf.getEndpointCount() != 3) {
                continue;
            }

            UsbEndpoint in = null;
            UsbEndpoint out = null;

            for (int e = 0, en = intf.getEndpointCount(); e < en; ++e) {
                UsbEndpoint endpoint = intf.getEndpoint(e);
                if (endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                    if (endpoint.getDirection() == UsbConstants.USB_DIR_IN) {
                        in = endpoint;
                    } else if (endpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                        out = endpoint;
                    }
                }
            }

            if (in == null || out == null) {
                continue;
            }

            if (AppConfig.LOG) {
                Log.i(TAG, "Found compatible USB interface");
                Log.i(TAG, "Interface class " + intf.getInterfaceClass());
                Log.i(TAG, "Interface subclass " + intf.getInterfaceSubclass());
                Log.i(TAG, "Interface protocol " + intf.getInterfaceProtocol());
                Log.i(TAG, "Bulk out max size " + out.getMaxPacketSize());
                Log.i(TAG, "Bulk in max size " + in.getMaxPacketSize());
            }

            if (device.getVendorId() == PtpConstants.CanonVendorId) {
                PtpUsbConnection connection = new PtpUsbConnection(usbManager.openDevice(device), in, out,
                        device.getVendorId(), device.getProductId());
                camera = new EosCamera(connection, listener, new WorkerNotifier(context));
            } else if (device.getVendorId() == PtpConstants.NikonVendorId) {
                PtpUsbConnection connection = new PtpUsbConnection(usbManager.openDevice(device), in, out,
                        device.getVendorId(), device.getProductId());
                camera = new NikonCamera(connection, listener, new WorkerNotifier(context));
            }

            return true;
        }

        if (listener != null) {
            listener.onError("No compatible camera found");
        }

        return false;
    }
}
