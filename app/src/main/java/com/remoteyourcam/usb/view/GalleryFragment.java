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
package com.remoteyourcam.usb.view;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.remoteyourcam.usb.R;
import com.remoteyourcam.usb.ptp.Camera;
import com.remoteyourcam.usb.ptp.PtpConstants;
import com.remoteyourcam.usb.ptp.model.LiveViewData;
import com.remoteyourcam.usb.ptp.model.ObjectInfo;
import com.remoteyourcam.usb.view.GalleryAdapter.ViewHolder;

public class GalleryFragment extends SessionFragment implements Camera.StorageInfoListener,
        Camera.RetrieveImageInfoListener, ListView.OnScrollListener,
        OnItemClickListener {

    private final Handler handler = new Handler();
    private Spinner storageSpinner;
    private StorageAdapter storageAdapter;
    private GridView galleryView;
    private GalleryAdapter galleryAdapter;
    private CheckBox orderCheckbox;

    SimpleDateFormat formatParser;
    boolean gotThumbWidth;
    private int currentScrollState;
    private TextView emptyView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        formatParser = new SimpleDateFormat("yyyyMMdd'T'HHmmss.S");
        currentScrollState = OnScrollListener.SCROLL_STATE_IDLE;

        View view = inflater.inflate(R.layout.gallery_frag, container, false);

        storageSpinner = (Spinner) view.findViewById(R.id.storage_spinner);
        storageAdapter = new StorageAdapter(getActivity());
        storageSpinner.setAdapter(storageAdapter);

        emptyView = (TextView) view.findViewById(android.R.id.empty);
        emptyView.setText(getString(R.string.gallery_loading));

        galleryView = (GridView) view.findViewById(android.R.id.list);
        galleryAdapter = new GalleryAdapter(getActivity(), this);
        galleryAdapter.setReverseOrder(getSettings().isGalleryOrderReversed());
        galleryView.setAdapter(galleryAdapter);
        galleryView.setOnScrollListener(this);
        galleryView.setEmptyView(emptyView);
        galleryView.setOnItemClickListener(this);

        orderCheckbox = (CheckBox) view.findViewById(R.id.reverve_order_checkbox);
        orderCheckbox.setChecked(getSettings().isGalleryOrderReversed());
        orderCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onReverseOrderStateChanged(isChecked);
            }
        });

        enableUi(false);

        ((SessionActivity) getActivity()).setSessionView(this);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (camera() != null) {
            cameraStarted(camera());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        getSettings().setGalleryOrderReversed(orderCheckbox.isChecked());
    }

    protected void onReverseOrderStateChanged(boolean isChecked) {
        galleryAdapter.setReverseOrder(isChecked);
    }

    @Override
    public void enableUi(boolean enabled) {
        storageSpinner.setEnabled(enabled);
        galleryView.setEnabled(enabled);
        orderCheckbox.setEnabled(enabled);
        if (!enabled) {
            emptyView.setText(getString(R.string.gallery_no_camera_connected));
        }
    }

    @Override
    public void cameraStarted(Camera camera) {
        enableUi(true);
        camera.retrieveStorages(this);
        emptyView.setText(getString(R.string.gallery_loading));
    }

    @Override
    public void cameraStopped(Camera camera) {
        enableUi(false);
        galleryAdapter.setHandles(new int[0]);
    }

    @Override
    public void propertyChanged(int property, int value) {
    }

    @Override
    public void propertyDescChanged(int property, int[] values) {
    }

    @Override
    public void setCaptureBtnText(String text) {
    }

    @Override
    public void focusStarted() {
    }

    @Override
    public void focusEnded(boolean hasFocused) {
    }

    @Override
    public void liveViewStarted() {
    }

    @Override
    public void liveViewStopped() {
    }

    @Override
    public void liveViewData(LiveViewData data) {
    }

    @Override
    public void capturedPictureReceived(int objectHandle, String filename, Bitmap thumbnail, Bitmap bitmap) {
    }

    @Override
    public void objectAdded(int handle, int format) {
    }

    @Override
    public void onStorageFound(final int handle, final String label) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!inStart) {
                    return;
                }
                storageAdapter.add(handle, label);
            }
        });
    }

    @Override
    public void onAllStoragesFound() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!inStart || camera() == null) {
                    return;
                }
                if (storageAdapter.getCount() == 0) {
                    emptyView.setText(getString(R.string.gallery_empty));
                    return;
                } else if (storageAdapter.getCount() == 1) {
                    storageSpinner.setEnabled(false);
                }
                storageSpinner.setSelection(0);
                camera().retrieveImageHandles(GalleryFragment.this, storageAdapter.getItemHandle(0),
                        PtpConstants.ObjectFormat.EXIF_JPEG);
            }
        });
    }

    @Override
    public void onImageHandlesRetrieved(final int[] handles) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!inStart) {
                    return;
                }
                if (handles.length == 0) {
                    emptyView.setText(getString(R.string.gallery_empty));
                }
                galleryAdapter.setHandles(handles);
            }
        });
    }

    @Override
    public void onImageInfoRetrieved(final int objectHandle, final ObjectInfo objectInfo, final Bitmap thumbnail) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Camera camera = camera();
                if (!inStart || camera == null) {
                    return;
                }
                if (!gotThumbWidth && thumbnail != null) {
                    gotThumbWidth = true;
                    galleryAdapter.setThumbDimensions(thumbnail.getWidth(), thumbnail.getHeight());
                }
                for (int i = 0; i < galleryView.getChildCount(); ++i) {

                    View child = galleryView.getChildAt(i);
                    if (child == null) {
                        continue;
                    }
                    GalleryAdapter.ViewHolder holder = (GalleryAdapter.ViewHolder) child.getTag();
                    if (holder.objectHandle == objectHandle) {
                        holder.image1.setImageBitmap(thumbnail);
                        holder.filename.setText(objectInfo.filename);
                        holder.dimension.setText(String.format("%dx%d", objectInfo.imagePixWidth,
                                objectInfo.imagePixHeight));
                        if (!"".equals(objectInfo.captureDate)) {
                            try {
                                Date date = formatParser.parse(objectInfo.captureDate);
                                holder.date.setText(date.toLocaleString());
                            } catch (ParseException e) {
                            }
                        }
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        currentScrollState = scrollState;
        switch (scrollState) {
        case ListView.OnScrollListener.SCROLL_STATE_IDLE: {
            Camera camera = camera();
            if (!inStart || camera == null) {
                break;
            }
            for (int i = 0; i < galleryView.getChildCount(); ++i) {

                View child = view.getChildAt(i);
                if (child == null) {
                    continue;
                }
                GalleryAdapter.ViewHolder holder = (GalleryAdapter.ViewHolder) child.getTag();
                if (!holder.done) {
                    holder.done = true;
                    camera.retrieveImageInfo(this, holder.objectHandle);
                }
            }

            break;
        }
        }

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }

    public void onNewListItemCreated(ViewHolder holder) {
        if (currentScrollState == SCROLL_STATE_IDLE) {
            Camera camera = camera();
            if (camera == null) {
                return;
            }
            holder.done = true;
            camera.retrieveImageInfo(this, holder.objectHandle);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, PictureFragment.newInstance(galleryAdapter.getItemHandle(position)), null);
        ft.addToBackStack(null);
        ft.commit();
    }
}
