package com.ikudot.mysdk;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.ikudot.mylibrary.NdkLibrary;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast.makeText(this, NdkLibrary.stringFromJNI(), Toast.LENGTH_SHORT).show();
    }
}