package com.websarva.wings.android.appsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_result_detail.*

class ResultDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_detail)

        val secName = intent.getStringExtra("secName")
        val value = intent.getStringExtra("value") + "ポイント"
        val temp = Math.floor(intent.getStringExtra("temp").toDouble()).toString() + "度"
        val heightDif = formatHeightDif(intent.getStringExtra("heightDif"))
        val distance = intent.getStringExtra("distanceText") + "m"
        val walkingTime = formatTime(intent.getStringExtra("walkingTime"))
        val timeDif = formatTime(intent.getStringExtra("timeDif"))
        val tvSecName = findViewById<TextView>(R.id.tvSecName)
        val tvValue = findViewById<TextView>(R.id.tvValue)
        val tvTemp = findViewById<TextView>(R.id.tvTemp)
        val tvHeightDif = findViewById<TextView>(R.id.tvHeightDif)
        val tvDistance = findViewById<TextView>(R.id.tvDistance)
        val tvWalkingTime = findViewById<TextView>(R.id.tvWalkingTime)
        val tvBustimeDif = findViewById<TextView>(R.id.tvBustimedif)
        tvSecName.setText(secName)
        tvValue.setText(value)
        tvTemp.setText(temp)
        tvHeightDif.setText(heightDif)
        tvDistance.setText(distance)
        tvWalkingTime.setText(walkingTime)
        tvBustimeDif.setText(timeDif)
    }

    private fun formatHeightDif(value: String) : String {
        return ((Math.floor(value.toDouble() * 100)) / 100).toString() + "m"
    }

    private fun formatTime(value: String) : String {
        return (Math.round(value.toFloat()) / 60).toString() + "分"
    }
}
