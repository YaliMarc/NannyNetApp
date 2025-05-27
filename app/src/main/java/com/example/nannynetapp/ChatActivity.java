package com.example.nannynetapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nannynetapp.ChatFragment;

/**
 * The type Chat activity.
 */
public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ChatFragment())
                    .commit();
        }
    }
}
