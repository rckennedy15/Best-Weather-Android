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