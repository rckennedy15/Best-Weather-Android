package com.rck.bestweather

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat

class SettingsActivity : AppCompatActivity() {
    companion object {
        const val KEY_PREF_PRIORITIZE_TEMP = "prioritizeTemp"
        const val KEY_PREF_PREFER_HIGH_TEMP = "preferHighTemp"
        const val KEY_PREF_PREFER_HIGH_CLOUD_COVER = "preferHighCloudCover"

        const val KEY_PREF_ALLOW_THUNDERSTORMS = "allowThunderstorms"
        const val KEY_PREF_ALLOW_DRIZZLE = "allowDrizzle"
        const val KEY_PREF_ALLOW_RAIN = "allowRain"
        const val KEY_PREF_ALLOW_SNOW = "allowSnow"
        const val KEY_PREF_ALLOW_ATMOSPHERIC = "allowAtmospheric"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }
}