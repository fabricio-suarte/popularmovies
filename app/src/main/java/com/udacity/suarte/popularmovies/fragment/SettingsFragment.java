package com.udacity.suarte.popularmovies.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.udacity.suarte.popularmovies.R;

/**
 * The settings fragment, as recommended by Google API Guide
 */
public class SettingsFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Lets load the preferences file here!
        this.addPreferencesFromResource(R.xml.preferences);

        this.loadSummary();
    }

    @Override
    public void onResume() {
        super.onResume();

        //Register the listener according to Google API docs
        this.getPreferenceManager()
                .getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        //Unregister the listener (following the Google API docs pattern)
        this.getPreferenceManager()
                .getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    //region SharedPreferences.OnSharedPreferenceChangeListener implementation

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if(sharedPreferences == null || key == null)
            return;

        Preference preference = this.getPreferenceManager()
                .findPreference(key);

        if(preference == null)
            return;

        if(preference instanceof ListPreference) {

            String newValue = sharedPreferences.getString(key, "");

            //Let's update the summary for this "ListPreference" object
            ListPreference listPreference = (ListPreference) preference;

            int valueIndex = listPreference.findIndexOfValue(newValue);

            if(valueIndex >= 0)
                listPreference.setSummary(listPreference.getEntries()[valueIndex]);
        }
        else if(preference instanceof CheckBoxPreference) {
            //do nothing. But the condition must exists... otherwise it would be catch
            //by the else.
        }
        else {

            String newValue = sharedPreferences.getString(key, "");

            //For any other preferences, set the summary to value's simple string
            preference.setSummary(newValue);
        }
    }

    //endregion

    //region private aux methods

    //Loads / sets the summary when the fragment is loaded
    private void loadSummary() {

        SharedPreferences preferences = this.getPreferenceManager()
                .getSharedPreferences();

        String key;

        key = this.getString(R.string.pref_sort_criteria_key);
        this.onSharedPreferenceChanged(preferences, key);

        key = this.getString(R.string.pref_max_number_of_pages_key);
        this.onSharedPreferenceChanged(preferences, key);
    }

    //endregion
}
