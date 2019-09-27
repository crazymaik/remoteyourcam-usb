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

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.remoteyourcam.usb.R;

public class WebViewDialogFragment extends DialogFragment {

    public static WebViewDialogFragment newInstance(int title, String url) {
        Bundle args = new Bundle();
        args.putInt("title", title);
        args.putString("url", url);
        WebViewDialogFragment f = new WebViewDialogFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getArguments().getInt("title"));
        View view = inflater.inflate(R.layout.webview_dialog, container, false);
        WebView webview = (WebView) view.findViewById(R.id.webview1);
        webview.loadUrl(getArguments().getString("url"));
        view.findViewById(android.R.id.button1).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return view;
    }
}
