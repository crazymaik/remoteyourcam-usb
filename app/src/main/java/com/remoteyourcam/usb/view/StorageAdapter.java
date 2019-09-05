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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class StorageAdapter extends BaseAdapter {

    private final List<Integer> ids = new ArrayList<Integer>();
    private final List<String> labels = new ArrayList<String>();
    private final LayoutInflater inflater;

    public StorageAdapter(Context context) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void add(int handle, String label) {
        ids.add(handle);
        labels.add(label);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return ids.size();
    }

    @Override
    public String getItem(int position) {
        return labels.get(position);
    }

    public int getItemHandle(int position) {
        return ids.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) convertView;

        if (view == null) {
            view = (TextView) inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        view.setText(labels.get(position));

        return view;
    }
}
