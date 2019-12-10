package com.sunxy.groovyplugin;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.v("sunxy--", getString("onCreate"));
    }

    private String getString(String input){
        return "input: " + input + " ,output ";
    }
}
