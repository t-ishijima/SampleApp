package com.websarva.wings.android.appsample


import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private val _helper = DatabaseHelper(this@MainActivity)
    var _busStopNamesStr = ""
    var _busStopTimetableStr = ""
    var _temp = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val receiver = OpenDataReceiver()
        receiver.execute()
        val btSearch = findViewById<Button>(R.id.btSearch)
        btSearch.setOnClickListener(SearchListener())
        Toast.makeText(applicationContext, "ボタンはデータロード後押せるようになります！", Toast.LENGTH_LONG).show()
    }

    private inner class SearchListener : View.OnClickListener {
        override fun onClick(view: View) {
            val etDeparture = findViewById<EditText>(R.id.etDeparture)
            val etArrival = findViewById<EditText>(R.id.etArrival)
            val departureStr = etDeparture.text.toString()
            val arrivalStr = etArrival.text.toString()
            val intent = Intent(applicationContext, SearchResultActivity::class.java)
            intent.putExtra("departureStr", departureStr)
            intent.putExtra("arrivalStr", arrivalStr)
            intent.putExtra("busStopNamesStr", _busStopNamesStr)
            intent.putExtra("busStopTimetableStr", _busStopTimetableStr)
            intent.putExtra("temp", _temp)
            startActivity(intent)
        }
    }

    private inner class OpenDataReceiver() : AsyncTask<String, String, String>() {
        override fun doInBackground(vararg params: String): String {
            val TOKEN = "7c65e1e1f0ce05a6f5563fc18bd07676bd4996e704ed76c3a96f8c7f97f746f0"
            val urlStr =
                "https://api-tokyochallenge.odpt.org/api/v4/odpt:BusTimetable?dc:title=%E9%83%BD%EF%BC%90%EF%BC%92%20%E5%A4%A7%E5%A1%9A%E9%A7%85%E5%89%8D%E8%A1%8C&odpt:operator=odpt.Operator:Toei&acl:consumerKey=${TOKEN}"
            val url = URL(urlStr)
            val con = url.openConnection() as HttpURLConnection
            con.requestMethod = "GET"
            con.connect()
            val stream = con.inputStream
            val result = is2String(stream)
            con.disconnect()
            stream.close()
            return result
        }

        override fun onPostExecute(result: String) {
            val jArray = JSONArray(result)
            val jObject = jArray.getJSONObject(0)
            val busTimetable = jObject.getString("odpt:busTimetableObject")
            val busTimetableObjects = JSONArray(busTimetable)
            val busStopNames: MutableList<String> = mutableListOf()
            val busStopTimetable: MutableList<String> = mutableListOf()
            for(i in 0..(busTimetableObjects.length()-1)) {
                val BusTimetableObject = busTimetableObjects.getJSONObject(i)
                busStopNames.add(BusTimetableObject.getString("odpt:note").replace(":\\d+:\\d+".toRegex(),""))
                busStopTimetable.add(BusTimetableObject.getString("odpt:arrivalTime"))
            }
            val busStopSize = busStopNames.size
            val secNames: MutableList<String> = mutableListOf()
            for(i in 0..(busStopNames.size - 2)) {
                secNames.add(busStopNames[i] + "-" + busStopNames[i + 1])
            }
            // すでに区間テーブルが作成されているかどうかを確認する処理
            val db = _helper.writableDatabase
            val sql = "SELECT * FROM sections WHERE _id = 1"
            val cursor = db.rawQuery(sql, null)
            // データベースから取得した値を格納する変数の用意。データがなかった時のために初期値も用意
            var secName = ""
            while(cursor.moveToNext()) {
                val idx = cursor.getColumnIndex("section_name")
                secName = cursor.getString(idx)
            }
            val range = 1..secNames.size
            if (secName == "") {
                for(i in range){
                    val sqlInsert = "INSERT INTO sections (_id, section_name, stamp_qty) VALUES (?, ?, ?)"
                    val stmt = db.compileStatement(sqlInsert)
                    stmt.bindLong(1, i.toLong())
                    stmt.bindString(2, secNames[i - 1])
                    stmt.bindLong(3, 0.toLong())
                    val id = stmt.executeInsert()
                }
            }

            _busStopNamesStr = busStopNames.toString()
            _busStopTimetableStr = busStopTimetable.toString()
            val name = _busStopNamesStr
            val bus = _busStopTimetableStr
            val receiver = WeatherReceiver()
            receiver.execute()
        }
    }

    // 天気API 東京の天気と気温を求める
    private inner class WeatherReceiver() : AsyncTask<String, String, String>() {
        override fun doInBackground(vararg params: String): String {
            val TOKEN = "15a15d832d17cbb66c5fc51b7798192d"
            val urlStr =
                "http://api.openweathermap.org/data/2.5/weather?q=Tokyo&appid=${TOKEN}"
            val url = URL(urlStr)
            val con = url.openConnection() as HttpURLConnection
            con.requestMethod = "GET"
            con.connect()
            val stream = con.inputStream
            val result = is2String(stream)
            con.disconnect()
            stream.close()
            return result
        }

        override fun onPostExecute(result: String) {
            val rootJSON = JSONObject(result)
            val main = rootJSON.getJSONObject("main")
            _temp = (main.getDouble("temp") - 273.16).toString()
            val temp = _temp
            val btSearch = findViewById<Button>(R.id.btSearch)
            btSearch.isEnabled = true
        }
    }

    private fun is2String(stream: InputStream): String {
        val sb = StringBuilder()
        val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
        var line = reader.readLine()
        while (line != null) {
            sb.append(line)
            line = reader.readLine()
        }
        reader.close()
        return sb.toString()
    }

    override fun onDestroy() {
        _helper.close()
        super.onDestroy()
    }
}
