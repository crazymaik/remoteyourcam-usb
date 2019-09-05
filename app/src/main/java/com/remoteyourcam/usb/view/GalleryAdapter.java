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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.remoteyourcam.usb.R;

public class GalleryAdapter extends BaseAdapter {

    public static class ViewHolder {
        int objectHandle;
        AspectRatioImageView image1;
        TextView filename;
        TextView dimension;
        TextView date;
        boolean done;
    }

    private int handles[] = new int[0];
    private final LayoutInflater inflater;
    private int thumbWidth;
    private int thumbHeight;
    private boolean reversed;
    private final GalleryFragment galleryFragment;

    public GalleryAdapter(Context context, GalleryFragment galleryFragment) {
        this.galleryFragment = galleryFragment;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setHandles(int handles[]) {
        this.handles = handles;
        notifyDataSetChanged();
    }

    public void setReverseOrder(boolean reversed) {
        if (this.reversed == reversed) {
            return;
        }
        this.reversed = reversed;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return handles.length;
    }

    public int getItemHandle(int position) {
        return handles[reversed ? handles.length - position - 1 : position];
    }

    @Override
    public Integer getItem(int position) {
        return handles[reversed ? handles.length - position - 1 : position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = inflater.inflate(R.layout.gallery_list_item, parent, false);
            ViewHolder holder = new ViewHolder();
            view.setTag(holder);
            holder.image1 = (AspectRatioImageView) view.findViewById(R.id.image1);
            holder.filename = (TextView) view.findViewById(R.id.filename_field);
            holder.dimension = (TextView) view.findViewById(R.id.dimension_field);
            holder.date = (TextView) view.findViewById(R.id.date_field);
        }

        ViewHolder holder = (ViewHolder)view.getTag();

        holder.image1.setImageBitmap(null);
        holder.image1.setExpectedDimensions(thumbWidth, thumbHeight);
        holder.objectHandle = getItemHandle(position);
        holder.filename.setText("");
        holder.dimension.setText("");
        holder.date.setText("");
        holder.done = false;

        galleryFragment.onNewListItemCreated(holder);

        return view;
    }

    public void setThumbDimensions(int width, int height) {
        this.thumbWidth = width;
        this.thumbHeight = height;
    }

}
