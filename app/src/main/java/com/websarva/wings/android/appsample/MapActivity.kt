package com.websarva.wings.android.appsample

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.jar.Manifest

class MapActivity : AppCompatActivity() {

    private lateinit var googleMap : GoogleMap
    private var _latitude = 35.681236
    private var _longitude = 139.767125

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync {
            googleMap = it
        }
        val btStamp = findViewById<Button>(R.id.btStamp)
        btStamp.setOnClickListener(StampButtonListener())
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
            putMarkers(googleMap, _latitude, _longitude)
            val section_id = intent.getStringExtra("section_id_Str").toInt()
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
            putMarkers(googleMap, _latitude, _longitude)
            val section_id = intent.getStringExtra("section_id_Str").toInt()
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

    private fun putMarkers(map: GoogleMap, latitude: Double, longitude: Double) {
        val latLng = LatLng(_latitude, _longitude)
        val marker = MarkerOptions()
            .position(latLng)
            .draggable(false)
        val descriptor = BitmapDescriptorFactory.defaultMarker(
            BitmapDescriptorFactory.HUE_BLUE
        )
        marker.icon(descriptor)
        map.addMarker(marker)
    }
}
