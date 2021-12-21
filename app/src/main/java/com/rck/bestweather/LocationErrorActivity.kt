package com.rck.bestweather

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class LocationErrorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_error)
        val button = findViewById<Button>(R.id.button)
    }

    fun goBack(view: android.view.View) {
        finish()
    }
}