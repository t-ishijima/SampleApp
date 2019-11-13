package com.websarva.wings.android.appsample

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat

class SearchResultActivity : AppCompatActivity() {
    private val _valueList: MutableList<MutableMap<String, Any>> = mutableListOf()
    private var _counter = 0
    private val _walkingTimes = mutableListOf<String>()
    private val _distanceTexts = mutableListOf<String>()
    private val _from = mutableListOf<String>()
    private val _to = mutableListOf<String>()
    private val _heightDifs = mutableListOf<Float>()
    private val _timeDifs = mutableListOf<Int>()
    private var _busStopNames = mutableListOf<String>()
    private var _departureIndex = 0
    private var _temp = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_result)

        // 前のアクティビティからフォームに入力された出発地・到着地のデータを受け取る
        val departureStr = intent.getStringExtra("departureStr")
        val arrivalStr = intent.getStringExtra("arrivalStr")
        _temp = intent.getStringExtra("temp")
        val busStopNamesStr = intent.getStringExtra("busStopNamesStr")
        // 文字列データをJSONArrayに変換
        val busStopNames = JSONArray(intent.getStringExtra("busStopNamesStr"))
        // 時刻表のデータにはJSONArrayが使えなかったので一旦そのまま変数に代入
        var busStopTimetable = intent.getStringExtra("busStopTimetableStr")
        // busStopNamesのJSONArrayをListに変換するための入れ物を用意
        val arrayList = mutableListOf<String>()
        // JSONArrayをListに変換(indexOfを使えるようにするため)
        for(i in 0..(busStopNames.length()-1)){
            arrayList.add(busStopNames.get(i).toString())
        }
        // JSONArrayがbusStopTimetableに使えなかったので[]を除去してsplitでリスト化
        busStopTimetable = busStopTimetable.replace("[", "")
        busStopTimetable = busStopTimetable.replace("]", "")
        val TimetbleArray = busStopTimetable.split(",")
        // 前のアクティビティで入力された出発地・到着地のindex番号(バス停番号)を求める
         _departureIndex = arrayList.indexOf(departureStr)
        val arrivalIndex = arrayList.indexOf(arrivalStr)
//        _range = departureIndex..arrivalIndex-1
        _busStopNames = arrayList
        val dupArrayList = arrayList
        // 出発地と到着地の間にある区間の数だけWebAPIに接続したいのでループを回す
        for(i in _departureIndex..(arrivalIndex-1)){
            val receiver = MapDataReceiver()
            // 真砂坂上と富坂上がそのままではGoogle Maps APIを使用できないので近場で置き換え
            if (arrayList[i] == "真砂坂上"){
                receiver.execute("本郷真砂パークハウス", "文京シビックホール")
            } else if(arrayList[i+1] == "真砂坂上") {
                receiver.execute(arrayList[i], "本郷真砂パークハウス")
            } else if(arrayList[i] == "春日駅前") {
                receiver.execute("文京シビックホール", "富坂上公衆トイレ")
            } else if(arrayList[i+1] == "春日駅前") {
                receiver.execute("本郷真砂パークハウス", "文京シビックホール")
            } else if(arrayList[i] == "富坂上") {
                receiver.execute("富坂上公衆トイレ", arrayList[i+1])
            } else if(arrayList[i+1] == "富坂上"){
                receiver.execute("文京シビックホール", "富坂上公衆トイレ")
            } else {
                receiver.execute(arrayList[i], arrayList[i+1])
            }
            // バス停間のバス移動時間も求める
            _timeDifs.add(getTimeDif(TimetbleArray[i], TimetbleArray[i+1]))
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    // Google Maps APIに接続してそれぞれのバス停の緯度経度、区間の徒歩時間、区間距離を求める
    private inner class MapDataReceiver() : AsyncTask<String, String, String>() {
        override fun doInBackground(vararg params: String): String {
            val TOKEN = "AIzaSyAovx0o3cTzyPimFYE2ROppc6LCqt75p1g"
            val departureStr = params[0]
            val arrivalStr = params[1]
            val urlStr =
                "https://maps.googleapis.com/maps/api/directions/json?origin=${departureStr}&destination=${arrivalStr}&mode=walking&key=${TOKEN}"
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
            val jObject = JSONObject(result)
            val routes = jObject.getString("routes")
            val jArray = JSONArray(routes)
            val bounds = jArray.getJSONObject(0)
            val legs = bounds.getString("legs")
            val jsonArray = JSONArray(legs)
            val jsonObject = jsonArray.getJSONObject(0)
            val distance = jsonObject.getString("distance")
            val distanceObj = JSONObject(distance)
            _distanceTexts.add(distanceObj.getString("value"))
            val distanceTexts = _distanceTexts
            val duration = jsonObject.getString("duration")
            val durationObj = JSONObject(duration)
            _walkingTimes.add(durationObj.getString("value"))
            val walkingTimes = _walkingTimes
            val endLocation = jsonObject.getString("end_location")
            val startLocation = jsonObject.getString("start_location")
            val endObj = JSONObject(endLocation)
            val startObj = JSONObject(startLocation)
            val endLat = endObj.getString("lat")
            val endLng = endObj.getString("lng")
            val startLat = startObj.getString("lat")
            val startLng = startObj.getString("lng")
            val StartElevationReceiver = StartElevationReceiver()
            StartElevationReceiver.execute(startLng, startLat, endLng, endLat)
        }
    }

    // 区間ごとの出発地となるバス停の標高を求める
    private inner class StartElevationReceiver() : AsyncTask<String, String, String>() {
        var _endLng = ""
        var _endLat = ""
        override fun doInBackground(vararg params: String): String {
            val startLng = params[0]
            val startLat = params[1]
            _endLng = params[2]
            _endLat = params[3]
            val urlStr =
                "http://cyberjapandata2.gsi.go.jp/general/dem/scripts/getelevation.php?lon=${startLng}&lat=${startLat}&outtype=JSON"
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
            _from.add(rootJSON.getString("elevation"))
            val EndElevationReceiver = EndElevationReceiver()
            EndElevationReceiver.execute(_endLng, _endLat)
        }
    }

    // 区間ごとの到着地となるバス停の標高を求める
    private inner class EndElevationReceiver() : AsyncTask<String, String, String>() {
        var _endLng = ""
        var _endLat = ""
        override fun doInBackground(vararg params: String): String {
            _endLng = params[0]
            _endLat = params[1]
            val urlStr =
                "http://cyberjapandata2.gsi.go.jp/general/dem/scripts/getelevation.php?lon=${_endLng}&lat=${_endLat}&outtype=JSON"
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
            _to.add(rootJSON.getString("elevation"))
            val counter = _counter
            val goal = _to
            val start = _from
            val heightDif = _to[_counter].toFloat() - _from[_counter].toFloat()
            _heightDifs.add(heightDif)
            val value = getValue(_walkingTimes[_counter].toInt(), _timeDifs[_counter])
            val secName = _busStopNames[_departureIndex + _counter] + "-" + _busStopNames[_departureIndex + _counter+1]
            _valueList.add(mutableMapOf("secName" to secName, "value" to value))
            val valueList = _valueList
            _counter = _counter + 1
            val from = arrayOf("secName", "value")
            val to = intArrayOf(android.R.id.text1, android.R.id.text2)
            val adapter = SimpleAdapter(applicationContext, _valueList, android.R.layout.simple_list_item_2, from, to)
            val lvValue = findViewById<ListView>(R.id.lvValue)
            lvValue.adapter = adapter
            lvValue.onItemClickListener = ListItemClickListener()
        }
    }

    private inner class ListItemClickListener : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            val item = parent.getItemAtPosition(position) as MutableMap<String, String>
            val itemSecName = item["secName"]
            val itemValue = item["value"].toString()
            val intent = Intent(applicationContext, ResultDetailActivity::class.java)
            intent.putExtra("secName", itemSecName)
            intent.putExtra("value", itemValue)
            intent.putExtra("heightDif", _heightDifs[position].toString())
            intent.putExtra("distanceText", _distanceTexts[position])
            intent.putExtra("walkingTime", _walkingTimes[position])
            intent.putExtra("timeDif", _timeDifs[position].toString())
            val heightDifs = _heightDifs
            val distanceTexts = _distanceTexts
            val walkingTimes = _walkingTimes
            val heightDif = _heightDifs[position]
            val timeDif = _timeDifs[position]
            val temp = _temp
            intent.putExtra("temp", _temp)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
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
    // 各区間のバス移動時間を秒単位で返却する関数
    private fun getTimeDif(time1: String, time2: String): Int {
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        val date1 = sdf.parse("2019/01/01 ${time1}:00")
        val date2 = sdf.parse("2019/01/01 ${time2}:00")
        val dateTimeTo = date2.time
        val dateTimeFrom = date1.time
        val Longdif = (dateTimeTo - dateTimeFrom) / 1000
        val dif = Longdif.toInt()
        // 茗荷谷駅前~窪町小学校が誤データ?なのか0になるため
        if (dif == 0) {
            return 60
        } else {
            return dif
        }
    }

    private fun getValue(facter1: Int, facter2: Int): Int {
        return facter1 + facter2
    }
}
