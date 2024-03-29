package com.websarva.wings.android.appsample

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.util.jar.Manifest

class MapActivity : AppCompatActivity() {

    private lateinit var googleMap : GoogleMap
    private val _helper = DatabaseHelper(this@MapActivity)
    private var _latitude = 35.681236
    private var _longitude = 139.767125

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync {
            googleMap = it
            zoomTo(googleMap, intent.getStringExtra("section_id_Str").toLong())
            showMarkers(googleMap, intent.getStringExtra("section_id_Str").toLong())
        }
        val btStamp = findViewById<ImageButton>(R.id.btStamp)
        btStamp.setOnClickListener(StampButtonListener())
        supportActionBar?.setDisplayHomeAsUpEnabled((true))
    }

    private inner class StampButtonListener : View.OnClickListener {

        override fun onClick(view: View) {
            //LocationManagerオブジェクトを取得。
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            //位置情報が更新された際のリスナオブジェクトを生成。
            val locationListener = GPSLocationListener()
            //ACCESS_FINE_LOCATIONの許可が下りていないなら…
            if(ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //ACCESS_FINE_LOCATIONの許可を求めるダイアログを表示。その際、リクエストコードを1000に設定。
                val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
                ActivityCompat.requestPermissions(this@MapActivity, permissions, 1000)
                //onCreate()メソッドを終了。
                return
            }
            //位置情報の追跡を開始。
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
            val section_id = intent.getStringExtra("section_id_Str").toLong()
            putMarkers(googleMap, _latitude, _longitude, section_id)
            val db = _helper.writableDatabase
            val sqlCount = "SELECT COUNT(*) AS cnt FROM stamps"
            val cursor = db.rawQuery(sqlCount, null)
            cursor.moveToFirst()
            val count = cursor.getInt(cursor.getColumnIndex("cnt"))
            cursor.close()

            val sqlInsert = "INSERT INTO stamps (_id, latitude, longitude, section_id) VALUES (?, ?, ?, ?)"
            val stmt = db.compileStatement(sqlInsert)
            stmt.bindLong(1, (count + 1).toLong())
            stmt.bindDouble(2, _latitude)
            stmt.bindDouble(3, _longitude)
            stmt.bindLong(4, section_id)
            stmt.executeInsert()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        //ACCESS_FINE_LOCATIONに対するパーミションダイアログでかつ許可を選択したなら…
        if(requestCode == 1000 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //LocationManagerオブジェクトを取得。
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            //位置情報が更新された際のリスナオブジェクトを生成。
            val locationListener = GPSLocationListener()
            //再度ACCESS_FINE_LOCATIONの許可が下りていないかどうかのチェックをし、降りていないなら処理を中止。
            if(ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            //位置情報の追跡を開始。
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
            val section_id = intent.getStringExtra("section_id_Str").toLong()
            putMarkers(googleMap, _latitude, _longitude, section_id)
            val db = _helper.writableDatabase
            val sqlCount = "SELECT COUNT(*) AS cnt FROM stamps"
            val cursor = db.rawQuery(sqlCount, null)
            cursor.moveToFirst()
            val count = cursor.getInt(cursor.getColumnIndex("cnt"))
            cursor.close()

            val sqlInsert = "INSERT INTO stamps (_id, latitude, longitude, section_id) VALUES (?, ?, ?, ?)"
            val stmt = db.compileStatement(sqlInsert)
            stmt.bindLong(1, (count + 1).toLong())
            stmt.bindDouble(2, _latitude)
            stmt.bindDouble(3, _longitude)
            stmt.bindLong(4, section_id)
            stmt.executeInsert()
        }
    }

    private inner class GPSLocationListener : LocationListener {
        override fun onLocationChanged(location: Location) {
            _latitude = location.latitude
            _longitude = location.longitude
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        }

        override fun onProviderDisabled(provider: String) {

        }

        override fun onProviderEnabled(provider: String) {

        }
    }

    // スタンプボタンを押した時にマップにスタンプが押され、区間テーブルのスタンプ数の個数が更新されるメソッド
    private fun putMarkers(map: GoogleMap, latitude: Double, longitude: Double, section_id: Long) {
        // スタンプを押す処理
        val latLng = LatLng(_latitude, _longitude)
        val marker = MarkerOptions()
            .position(latLng)
            .draggable(false)
        val descriptor = BitmapDescriptorFactory.fromResource(R.drawable.stamp)
        marker.icon(descriptor)
        map.addMarker(marker)
        // 区間テーブルを更新する処理
        val db = _helper.writableDatabase
        // 区間がどれだけスタンプを所有しているかのSQL
        val sqlStampQty = "SELECT * FROM sections WHERE _id = ${section_id}"
        val cursor = db.rawQuery(sqlStampQty, null)
        var stamp_qty = 0
        while (cursor.moveToNext()) {
            stamp_qty = cursor.getInt(cursor.getColumnIndex("stamp_qty"))
        }
        val sqlDelete = "DELETE FROM sections WHERE _id = ?"
        var stmt = db.compileStatement(sqlDelete)
        stmt.bindLong(1, section_id)
        stmt.executeUpdateDelete()
        val sqlInsert = "INSERT INTO sections (_id, section_name, stamp_qty) VALUES (?, ?, ?)"
        stmt = db.compileStatement(sqlInsert)
        stmt.bindLong(1, section_id)
        stmt.bindString(2, intent.getStringExtra("section_name"))
        stmt.bindLong(3, (stamp_qty + 1).toLong())
        stmt.executeInsert()
    }
    // 位置情報に応じてカメラを移動させ、ズームさせるメソッド
    private fun zoomTo(map: GoogleMap, section_id: Long) {
        val db = _helper.writableDatabase
        val sqlStamps = "SELECT * FROM stamps WHERE section_id = ${section_id}"
        val latitudes = mutableListOf<Double>()
        val longitudes = mutableListOf<Double>()
        val cursor = db.rawQuery(sqlStamps, null)
        while (cursor.moveToNext()) {
            val latitude = cursor.getDouble(cursor.getColumnIndex("latitude"))
            val longitude = cursor.getDouble(cursor.getColumnIndex("longitude"))
            latitudes.add(latitude)
            longitudes.add(longitude)
        }
        val locations = latitudes.zip(longitudes)
        if (locations.size == 1) {
            val latLng = LatLng(locations[0].first, locations[0].second)
            val move = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
            map.moveCamera(move)
        } else if (locations.size > 1) {
            val bounds = LatLngBounds.Builder()
            locations.forEach { location ->
                bounds.include(LatLng(location.first, location.second))
            }

            val padding = (50 * resources.displayMetrics.density).toInt()
            val move = CameraUpdateFactory.newLatLngBounds(bounds.build(),
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.heightPixels,
                padding)

            map.moveCamera(move)
        }
    }
    // データベースに格納されている該当区間に属するスタンプをマップに表示
    private fun showMarkers(map: GoogleMap, section_id: Long) {
        val db = _helper.writableDatabase
        val sqlShowStamps = "SELECT * FROM stamps WHERE section_id = ${section_id}"
        val latitudes = mutableListOf<Double>()
        val longitudes = mutableListOf<Double>()
        val cursor = db.rawQuery(sqlShowStamps, null)
        while (cursor.moveToNext()) {
            val latitude = cursor.getDouble(cursor.getColumnIndex("latitude"))
            val longitude = cursor.getDouble(cursor.getColumnIndex("longitude"))
            latitudes.add(latitude)
            longitudes.add(longitude)
        }
        val locations = latitudes.zip(longitudes)
        val lineOptions = PolylineOptions()
        locations.forEach { location ->
            val latLng = LatLng(location.first, location.second)
            lineOptions.add(latLng)
            val marker = MarkerOptions()
                .position(latLng)
                .draggable(false)
            val descriptor = BitmapDescriptorFactory.fromResource(R.drawable.stamp)
            marker.icon(descriptor)
            map.addMarker(marker)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        _helper.close()
        super.onDestroy()
    }
}
