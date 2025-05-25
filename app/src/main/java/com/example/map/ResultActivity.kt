package com.example.map

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.PolylineOptions
import java.util.concurrent.TimeUnit


class ResultActivity : ComponentActivity() {

    private lateinit var mapView: MapView
    private lateinit var aMap: AMap
    private lateinit var duration: TextView
    private lateinit var distance: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        mapView = findViewById(R.id.map)
        mapView.onCreate(savedInstanceState)
        aMap = mapView.map
        duration = findViewById(R.id.duration)
        distance = findViewById(R.id.distance)


        // 获取数据
        val path = intent.getParcelableArrayListExtra<LatLng>("path")
        val duration = intent.getLongExtra("duration", 0)
        val distance = intent.getFloatExtra("distance", 0f)


        // 绘制路径
        drawPath(path)
        showInfo(duration, distance)
    }

    private fun drawPath(path: List<LatLng>?) {
        if (path.isNullOrEmpty()) return

        // 添加路径线
        aMap.addPolyline(
            PolylineOptions()
                .addAll(path)
                .width(12f)
                .color(Color.BLUE)
        )

        // 添加起点/终点标记
        aMap.addMarker(
            MarkerOptions()
                .position(path[0])
                .title("起点")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )

        aMap.addMarker(
            MarkerOptions()
                .position(path[path.size - 1])
                .title("终点")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )

        // 调整视角
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(path[0], 15f))
    }

    private fun showInfo(durationMillis: Long, distanceMeters: Float) {
        // 格式化时间
        val time = java.lang.String.format(
            "耗时: %d分钟",
            TimeUnit.MILLISECONDS.toMinutes(durationMillis)
        )

        // 格式化距离
        val dist = String.format("距离: %.1f公里", distanceMeters / 1000)

        duration.text = time
        distance.text = dist
    }

    override fun onResume() {
        mapView.onResume()
        super.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }
}
