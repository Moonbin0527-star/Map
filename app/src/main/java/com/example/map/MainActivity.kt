package com.example.map

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.os.PersistableBundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.MapsInitializer
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.maps.model.Poi
import com.amap.api.navi.AmapNaviPage
import com.amap.api.navi.AmapNaviParams
import com.amap.api.navi.AmapNaviType
import com.amap.api.navi.AmapPageType


class MainActivity : ComponentActivity(), AMap.OnMapClickListener, AMapLocationListener{

    private lateinit var aMap: AMap
    private lateinit var mLocationClient: AMapLocationClient
    private lateinit var mapView: MapView
    private lateinit var buttonStartNavi: Button
    private lateinit var buttonNaviInfo: Button
    private val viewModel by viewModels<MainViewModel>()
    private val naviCallback by lazy {
        NaviResultHandler(this, viewModel)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()

        MapsInitializer.updatePrivacyShow(this, true, true)
        MapsInitializer.updatePrivacyAgree(this, true)

        setContentView(R.layout.main_activity)

        //获取地图控件引用
        mapView = findViewById(R.id.map)
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mapView.onCreate(savedInstanceState)
        buttonStartNavi = findViewById(R.id.btn_start)
        buttonNaviInfo = findViewById(R.id.btn_navi_info)

        initMap()
        initLocation()
        initClicks()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun requestPermissions() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    // Precise location access granted.
                }
                permissions.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    // Only approximate location access granted.
                }
                else -> {
                    // No location access granted.
                }
            }
        }

        // Before you perform the actual permission request, check whether your app
        // already has the permissions, and whether your app needs to show a permission
        // rationale dialog. For more details, see Request permissions:
        // https://developer.android.com/training/permissions/requesting#request-permission
        locationPermissionRequest.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun initMap() {
        aMap = mapView.map.apply {
            setOnMapClickListener(this@MainActivity)
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isMyLocationButtonEnabled = true
        }
        initLocationStyle()
    }

    private fun initLocationStyle() {
        val locationStyle = MyLocationStyle().apply {
            interval(2000L)
            myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE)
        }
        aMap.myLocationStyle = locationStyle
        aMap.isMyLocationEnabled = true
    }

    override fun onMapClick(latLng: LatLng) {
        viewModel.destination = latLng
        aMap.addMarker(MarkerOptions().position(latLng).title("目的地"))
    }

    private fun initLocation() {
        mLocationClient = AMapLocationClient(this)
        val locationOption = AMapLocationClientOption()
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy) // 高精度模式
        locationOption.setInterval(2000) // 定位间隔（毫秒）
        locationOption.setNeedAddress(true) // 返回地址信息
        mLocationClient.setLocationOption(locationOption)
        mLocationClient.setLocationListener(this)
        mLocationClient.startLocation()
    }

    private fun initClicks() {
        buttonStartNavi.setOnClickListener {
            viewModel.destination?.let {
                startNavigation()
            } ?: run { Toast.makeText(this, "请先点击地图选择目的地", Toast.LENGTH_SHORT).show() }
        }
        buttonNaviInfo.setOnClickListener {
            viewModel.lastNaviInfo?.let {
                val intent = Intent(
                    this,
                    ResultActivity::class.java
                )
                intent.putParcelableArrayListExtra("path", ArrayList<Parcelable>(it.path))
                intent.putExtra("duration", it.duration)
                intent.putExtra("distance", it.distance)
                this.startActivity(intent)
            } ?: run { Toast.makeText(this, "尚未有上次导航信息", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun startNavigation() {
        if (viewModel.destination != null && viewModel.currentLocation != null) {
            // 起点
            val start = Poi(null, LatLng(viewModel.currentLocation!!.latitude, viewModel.currentLocation!!.longitude), null)
            // 终点
            val end = Poi(null, viewModel.destination, null)
            // 组件参数配置
            val params = AmapNaviParams(start, emptyList(), end, AmapNaviType.DRIVER, AmapPageType.ROUTE)
            // 启动导航组件
            AmapNaviPage.getInstance().showRouteActivity(applicationContext, params, naviCallback)
        }
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

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        mapView.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onLocationChanged(location: AMapLocation?) {
        location?.let {
            // 定位成功
            val latitude = it.latitude
            val longitude = it.longitude

            // 更新小蓝点位置
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 15f))
            viewModel.currentLocation = location
        }
    }
}

