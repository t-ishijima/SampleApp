package com.websarva.wings.android.appsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class ResultDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_detail)

        val secName = intent.getStringExtra("secName")
        val value = intent.getStringExtra("value")
        val temp = intent.getStringExtra("temp").toDouble()
        val heightDif = intent.getStringExtra("heightDif")
        val distanceText = intent.getStringExtra("distanceText")
        val walkingTime = intent.getStringExtra("walkingTime")
        val timeDif = intent.getStringExtra("timeDif")
    }
}
