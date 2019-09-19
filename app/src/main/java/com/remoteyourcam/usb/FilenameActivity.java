package com.remoteyourcam.usb;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class FilenameActivity  extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_filenames);

        TextView tv = findViewById(R.id.filenames);
        ArrayList<String> filenames = getIntent().getStringArrayListExtra("filenames");
        for (String filename: filenames) {
            tv.setText(tv.getText() + filename + "\n");
        }
    }


}
