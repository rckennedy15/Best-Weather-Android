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

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlin.math.roundToInt
import kotlin.properties.Delegates

class ResultsActivity : AppCompatActivity(), OnMapReadyCallback {
    private var latitude by Delegates.notNull<Double>()
    private var longitude by Delegates.notNull<Double>()
    private lateinit var name: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)
        val extras = intent.extras
        if (extras != null) {
            name = extras.getString("name")!!
            val temp = extras.getDouble("temp")
            val clouds = extras.getDouble("clouds")
            latitude = extras.getDouble("lat")
            longitude = extras.getDouble("lon")
            val nText = findViewById<TextView>(R.id.name)
            val tText = findViewById<TextView>(R.id.temp)
            val cText = findViewById<TextView>(R.id.clouds)
            val fahrenTemp = (temp - 273.15) * 9/5 + 32
            nText.text = name
            tText.text = "${fahrenTemp.roundToInt()} °F"
            cText.text = "${clouds.roundToInt()}%"
        }
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 10F));

    }

    fun startDirections(view: View) {
        val encodedName = Uri.encode(name)
        val gmmIntentUri =
            Uri.parse("google.navigation:q=$encodedName")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }
}