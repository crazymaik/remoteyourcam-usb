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

import android.content.Context;
import android.content.SharedPreferences;

public class AppSettings {

    private final SharedPreferences prefs;

    public AppSettings(Context context) {
        prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE);
    }

    public boolean showChangelog(int nextNumber) {
        int last = prefs.getInt("internal.last_changelog_number", -1);
        if (last == -1 || nextNumber > last) {
            prefs.edit().putInt("internal.last_changelog_number", nextNumber).apply();
        }
        return last != -1 && nextNumber > last;
    }

    public boolean isGalleryOrderReversed() {
        return prefs.getBoolean("internal.gallery.reverse_order", false);
    }

    public void setGalleryOrderReversed(boolean reversed) {
        prefs.edit().putBoolean("internal.gallery.reverse_order", reversed).apply();
    }

    public int getShowCapturedPictureDuration() {
        return getIntFromStringPreference("liveview.captured_picture_duration", -1);
    }

    public boolean isShowCapturedPictureNever() {
        return getShowCapturedPictureDuration() == -2;
    }

    public boolean isShowCapturedPictureDurationManual() {
        return getShowCapturedPictureDuration() == -1;
    }

    public int getNumPicturesInStream() {
        return getIntFromStringPreference("picturestream.num_pictures", 6);
    }

    public boolean isShowFilenameInStream() {
        return prefs.getBoolean("picturestream.show_filename", true);
    }

    public int getCapturedPictureSampleSize() {
        return getIntFromStringPreference("memory.picture_sample_size", 2);
    }

    private int getIntFromStringPreference(String key, int defaultValue) {
        try {
            String value = prefs.getString(key, null);
            if (value != null) {
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            // nop
        }
        return defaultValue;
    }
}
