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
package com.remoteyourcam.usb;

import java.io.File;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.remoteyourcam.usb.activities.AppSettingsActivity;
import com.remoteyourcam.usb.ptp.Camera;
import com.remoteyourcam.usb.ptp.Camera.CameraListener;
import com.remoteyourcam.usb.ptp.PtpService;
import com.remoteyourcam.usb.ptp.model.LiveViewData;
import com.remoteyourcam.usb.util.PackageUtil;
import com.remoteyourcam.usb.view.GalleryFragment;
import com.remoteyourcam.usb.view.SessionActivity;
import com.remoteyourcam.usb.view.SessionView;
import com.remoteyourcam.usb.view.TabletSessionFragment;
import com.remoteyourcam.usb.view.WebViewDialogFragment;

public class MainActivity extends SessionActivity implements CameraListener {

    private static final int DIALOG_PROGRESS = 1;
    private static final int DIALOG_NO_CAMERA = 2;

    private static class MyTabListener implements ActionBar.TabListener {

        private final Fragment fragment;

        public MyTabListener(Fragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            ft.add(R.id.fragment_container, fragment);
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            ft.remove(fragment);
        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        }

    }

    private final String TAG = MainActivity.class.getSimpleName();

    private final Handler handler = new Handler();

    private PtpService ptp;
    private Camera camera;

    private boolean isInStart;
    private boolean isInResume;
    private SessionView sessionFrag;
    private boolean isLarge;
    private AppSettings settings;

    @Override
    public Camera getCamera() {
        return camera;
    }

    @Override
    public void setSessionView(SessionView view) {
        sessionFrag = view;
    }

    @Override
    public AppSettings getSettings() {
        return settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppConfig.LOG) {
            Log.i(TAG, "onCreate");
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (!getResources().getConfiguration().isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE)) {
            getWindow()
                    .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            isLarge = true;
        }

        setContentView(R.layout.session);

        settings = new AppSettings(this);

        ActionBar bar = getActionBar();

        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setDisplayHomeAsUpEnabled(false);
        bar.addTab(bar.newTab().setText("Session").setTabListener(new MyTabListener(new TabletSessionFragment())));
        bar.addTab(bar.newTab().setText("Gallery").setTabListener(new MyTabListener(new GalleryFragment())));

        int appVersionCode = -1;
        try {
            appVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            // nop
        }

        if (settings.showChangelog(appVersionCode)) {
            showChangelog();
        }

        ptp = PtpService.Singleton.getInstance(this);
    }

    private void showChangelog() {
        FragmentTransaction changelogTx = getFragmentManager().beginTransaction();
        WebViewDialogFragment changelogFragment = WebViewDialogFragment.newInstance(R.string.whats_new,
                "file:///android_asset/changelog/changelog.html");
        changelogTx.add(changelogFragment, "changelog");
        changelogTx.commit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (AppConfig.LOG) {
            Log.i(TAG, "onNewIntent " + intent.getAction());
        }
        this.setIntent(intent);
        if (isInStart) {
            ptp.initialize(this, intent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (AppConfig.LOG) {
            Log.i(TAG, "onStart");
        }
        isInStart = true;
        ptp.setCameraListener(this);
        ptp.initialize(this, getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        isInResume = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isInResume = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (AppConfig.LOG) {
            Log.i(TAG, "onStop");
        }
        isInStart = false;
        ptp.setCameraListener(null);
        if (isFinishing()) {
            ptp.shutdown();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (AppConfig.LOG) {
            Log.i(TAG, "onDestroy");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_PROGRESS:
            return ProgressDialog.show(this, "", "Generating information. Please wait...", true);
        case DIALOG_NO_CAMERA:
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setTitle(R.string.dialog_no_camera_title);
            b.setMessage(R.string.dialog_no_camera_message);
            b.setNeutralButton(R.string.ok, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            return b.create();
        }
        return super.onCreateDialog(id);
    }

    public void onMenuFeedbackClicked(MenuItem item) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setPositiveButton(R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendDeviceInformation();
            }
        });
        b.setNegativeButton(R.string.cancel, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        b.setTitle(R.string.feedback_dialog_title);
        b.setMessage(R.string.feedback_dialog_message);
        b.show();
    }

    private void sendDeviceInformation() {
        showDialog(DIALOG_PROGRESS);
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                File dir = getExternalCacheDir();
                final File out = dir != null ? new File(dir, "deviceinfo.txt") : null;

                if (camera != null) {
                    camera.writeDebugInfo(out);
                }

                final String shortDeviceInfo = out == null && camera != null ? camera.getDeviceInfo() : "unknown";

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.this.dismissDialog(DIALOG_PROGRESS);
                        Intent sendIntent = new Intent(Intent.ACTION_SEND);
                        sendIntent.setType("text/plain");
                        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "RYC USB Feedback");
                        sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { "PUT_EMAIL_HERE" });
                        if (out != null && camera != null) {
                            sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + out.toString()));
                            sendIntent.putExtra(Intent.EXTRA_TEXT, "Any problems or feature whishes? Let us know: ");
                        } else {
                            sendIntent.putExtra(Intent.EXTRA_TEXT,
                                    "Any problems or feature whishes? Let us know: \n\n\n" + shortDeviceInfo);
                        }
                        startActivity(Intent.createChooser(sendIntent, "Email:"));
                    }
                });
            }
        });
        th.start();
    }

    public void onMenuChangelogClicked(MenuItem item) {
        showChangelog();
    }

    public void onMenuSettingsClicked(MenuItem item) {
        startActivity(new Intent(this, AppSettingsActivity.class));
    }

    public void onMenuAboutClicked(MenuItem item) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setNeutralButton(R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        View view = getLayoutInflater().inflate(R.layout.about_dialog, null);
        ((TextView) view.findViewById(R.id.about_dialog_version)).setText(getString(R.string.about_dialog_version,
                PackageUtil.getVersionName(this)));
        b.setView(view);
        b.show();
    }

    @Override
    public void onCameraStarted(Camera camera) {
        this.camera = camera;
        if (AppConfig.LOG) {
            Log.i(TAG, "camera started");
        }
        try {
            dismissDialog(DIALOG_NO_CAMERA);
        } catch (IllegalArgumentException e) {
        }
        getActionBar().setTitle(camera.getDeviceName());
        camera.setCapturedPictureSampleSize(settings.getCapturedPictureSampleSize());
        sessionFrag.cameraStarted(camera);
    }

    @Override
    public void onCameraStopped(Camera camera) {
        if (AppConfig.LOG) {
            Log.i(TAG, "camera stopped");
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.camera = null;
        sessionFrag.cameraStopped(camera);
    }

    @Override
    public void onNoCameraFound() {
        showDialog(DIALOG_NO_CAMERA);
    }

    @Override
    public void onError(String message) {
        sessionFrag.enableUi(false);
        sessionFrag.cameraStopped(null);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPropertyChanged(int property, int value) {
        sessionFrag.propertyChanged(property, value);
    }

    @Override
    public void onPropertyStateChanged(int property, boolean enabled) {
        // TODO
    }

    @Override
    public void onPropertyDescChanged(int property, int[] values) {
        sessionFrag.propertyDescChanged(property, values);
    }

    @Override
    public void onLiveViewStarted() {
        sessionFrag.liveViewStarted();
    }

    @Override
    public void onLiveViewStopped() {
        sessionFrag.liveViewStopped();
    }

    @Override
    public void onLiveViewData(LiveViewData data) {
        if (!isInResume) {
            return;
        }
        sessionFrag.liveViewData(data);
    }

    @Override
    public void onCapturedPictureReceived(int objectHandle, String filename, Bitmap thumbnail, Bitmap bitmap) {
        if (thumbnail != null) {
            sessionFrag.capturedPictureReceived(objectHandle, filename, thumbnail, bitmap);
        } else {
            Toast.makeText(this, "No thumbnail available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBulbStarted() {
        sessionFrag.setCaptureBtnText("0");
    }

    @Override
    public void onBulbExposureTime(int seconds) {
        sessionFrag.setCaptureBtnText("" + seconds);
    }

    @Override
    public void onBulbStopped() {
        sessionFrag.setCaptureBtnText("Fire");
    }

    @Override
    public void onFocusStarted() {
        sessionFrag.focusStarted();
    }

    @Override
    public void onFocusEnded(boolean hasFocused) {
        sessionFrag.focusEnded(hasFocused);
    }

    @Override
    public void onFocusPointsChanged() {
        // TODO onFocusPointsToggleClicked(null);
    }

    @Override
    public void onObjectAdded(int handle, int format) {
        sessionFrag.objectAdded(handle, format);
    }
}
