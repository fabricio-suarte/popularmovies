package com.udacity.suarte.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.udacity.suarte.popularmovies.fragment.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null) {

            //We can replace the default activity content by the settings fragments.
            this.getFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, new SettingsFragment())
                    .commit();
        }
    }

}
