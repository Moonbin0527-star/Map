package com.example.map

import android.content.Context
import android.view.View
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.LatLng
import com.amap.api.navi.INaviInfoCallback
import com.amap.api.navi.model.AMapNaviLocation


class NaviResultHandler(private val context: Context, private val viewModel: MainViewModel): INaviInfoCallback {

    private val mPathPoints: MutableList<LatLng> = ArrayList()
    private var mStartTime: Long = 0
    private var mTotalDistance = 0f

    override fun onCalculateRouteSuccess(p0: IntArray?) {}

    override fun onCalculateRouteFailure(p0: Int) {}

    override fun onStopSpeaking() {}

    override fun onReCalculateRoute(p0: Int) {}

    override fun onExitPage(p0: Int) {}

    override fun onStrategyChanged(p0: Int) {}

    override fun onArrivedWayPoint(p0: Int) {}

    override fun onMapTypeChanged(p0: Int) {}

    override fun onNaviDirectionChanged(p0: Int) {}

    override fun onDayAndNightModeChanged(p0: Int) {}

    override fun onBroadcastModeChanged(p0: Int) {}

    override fun onScaleAutoChanged(p0: Boolean) {}

    override fun getCustomMiddleView(): View {
        return View(context)
    }

    override fun getCustomNaviView(): View {
        return View(context)
    }

    override fun getCustomNaviBottomView(): View {
        return View(context)
    }

    override fun onInitNaviFailure() {}

    override fun onGetNavigationText(p0: String?) {}

    // 导航开始回调
    override fun onStartNavi(naviType: Int) {
        mStartTime = System.currentTimeMillis()
        mPathPoints.clear()
        mTotalDistance = 0f
    }

    // 实时位置更新回调
    override fun onLocationChange(location: AMapNaviLocation?) {
        if (location != null && location.coord != null) {
            // 记录路径点
            val point = LatLng(
                location.coord.latitude,
                location.coord.longitude
            )
            // 累加行驶距离
            mPathPoints.lastOrNull()?.let {
                val distance = AMapUtils.calculateLineDistance(
                    it,
                    point
                )
                mTotalDistance += distance
            }
            mPathPoints.add(point)
        }
    }

    // 到达目的地回调
    override fun onArriveDestination(isSimulator: Boolean) {
        // 计算耗时
        val duration = System.currentTimeMillis() - mStartTime
        val naviInfo = NaviResultInfo(
            path = mPathPoints,
            duration = duration,
            distance = mTotalDistance
        )
        viewModel.lastNaviInfo = naviInfo
    }


}
