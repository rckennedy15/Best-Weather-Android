/**
© Copyright Ryan Kennedy

This file is part of Best Weather.

Best Weather is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Best Weather is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Best Weather.  If not, see <https://www.gnu.org/licenses/>
 */

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