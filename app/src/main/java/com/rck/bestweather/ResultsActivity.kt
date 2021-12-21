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

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class ResultsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)
        val extras = intent.extras
        if (extras != null) {
            val name = extras.getString("name")
            val temp = extras.getDouble("temp")
            val clouds = extras.getDouble("clouds")
            val nText = findViewById<TextView>(R.id.name)
            val tText = findViewById<TextView>(R.id.temp)
            val cText = findViewById<TextView>(R.id.clouds)
            val fahrenTemp = (temp - 273.15) * 9/5 + 32
            nText.text = name
            tText.text = fahrenTemp.toString()
            cText.text = clouds.toString()
        }
    }
}