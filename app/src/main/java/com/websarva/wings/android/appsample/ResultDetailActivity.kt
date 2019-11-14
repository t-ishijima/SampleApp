package com.websarva.wings.android.appsample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_result_detail.*

class ResultDetailActivity : AppCompatActivity() {
    private val _helper = DatabaseHelper(this@ResultDetailActivity)
    private var _section_id = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_detail)

        val secName = intent.getStringExtra("secName")
        val db = _helper.writableDatabase
        val sql = "SELECT * FROM sections WHERE section_name = ?"
        val params = arrayOf(secName)
        val cursor = db.rawQuery(sql, params)
        while(cursor.moveToNext()) {
            val index = cursor.getColumnIndex("_id")
            _section_id = cursor.getInt(index)
        }
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
        val btMap = findViewById<Button>(R.id.btMap)
        tvSecName.setText(secName)
        tvValue.setText(value)
        tvTemp.setText(temp)
        tvHeightDif.setText(heightDif)
        tvDistance.setText(distance)
        tvWalkingTime.setText(walkingTime)
        tvBustimeDif.setText(timeDif)
        btMap.setOnClickListener(ButtonListener())
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private inner class ButtonListener : View.OnClickListener {
        override fun onClick(view: View) {
            val section_id_Str = _section_id.toString()
            val intent = Intent(applicationContext, MapActivity::class.java)
            intent.putExtra("section_id_Str", section_id_Str)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun formatHeightDif(value: String) : String {
        return ((Math.floor(value.toDouble() * 100)) / 100).toString() + "m"
    }

    private fun formatTime(value: String) : String {
        return (Math.round(value.toFloat()) / 60).toString() + "分"
    }

    override fun onDestroy() {
        _helper.close()
        super.onDestroy()
    }
}
