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

public class PropertyData {

    public static interface OnPropertyStateChangedListener {
        void onPropertyDescChanged(PropertyData property);
    }

    public static interface OnPropertyValueChangedListener {
        void onPropertyValueChanged(PropertyData property, int value);
    }

    private final int property;

    int[] values = new int[0];
    boolean enabled;
    int currentValue;
    int currentIndex;
    String[] labels;
    Integer[] icons;

    private OnPropertyStateChangedListener descChangedListener;
    private OnPropertyValueChangedListener valueChangedListener;

    public PropertyData(int property) {
        this.property = property;
        this.currentValue = -1;
        this.currentIndex = -1;
    }

    public void setOnPropertyDescChangedListener(OnPropertyStateChangedListener listener) {
        this.descChangedListener = listener;
    }

    public void setOnPropertyValueChangedListener(OnPropertyValueChangedListener listener) {
        this.valueChangedListener = listener;
    }

    public void setDescription(int[] values, String[] labels, Integer[] icons) {
        this.values = values;
        this.labels = labels;
        this.icons = icons;
        this.currentIndex = -1;
        if (descChangedListener != null) {
            descChangedListener.onPropertyDescChanged(this);
        }
    }

    public void setValue(int value) {
        this.currentValue = value;
        this.currentIndex = -1;
        if (valueChangedListener != null) {
            valueChangedListener.onPropertyValueChanged(this, value);
        }
    }

    public void setValueByIndex(int index) {
        this.currentValue = values[index];
        this.currentIndex = index;
    }

    public int calculateCurrentIndex() {
        if (currentIndex == -1) {
            for (int i = 0; i < values.length; ++i) {
                if (values[i] == currentValue) {
                    currentIndex = i;
                    break;
                }
            }
        }
        return currentIndex;
    }
}