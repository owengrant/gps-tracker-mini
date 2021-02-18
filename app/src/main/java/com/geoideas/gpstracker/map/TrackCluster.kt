package com.geoideas.gpstracker.map

import com.geoideas.gpstracker.repository.room.entity.Point
import com.google.android.gms.maps.model.LatLng
import java.util.concurrent.ForkJoinPool
import java.util.stream.Collectors.toList

class TrackCluster (
    private val track: Track,
    private val distance: Float = 15f,
    private val range: Int = 20,
    private val speed: Int = 3,
    private val sec: Int = 3
) {

    /*
        Cluster polylines based on speed, time and distance
     */
    open fun cluster() : List<Point> {
        val speeds = track.speeds()
        val points = track.points
        val pointsWithCluster = mutableListOf<Point>()
        val middlePoints = mutableListOf<Point>()
        var count = 0
        var rangeLimit = 0
        var middlePoint: Point
        for ((i,speed) in speeds.withIndex()) {
            if(i < rangeLimit) continue
            val to = if(i + range < speeds.size) i + range else i + (speeds.size - i) - 1
            val index = to-(range/2)
            if((speed == 0f && checkClusterRange(i, to)) && index >= 0) {
                middlePoint = points[index]
                middlePoints.add(middlePoint)
                pointsWithCluster.add(middlePoint)
                rangeLimit = i + range
                count++
            }
            else pointsWithCluster.add(points[i])
        }
        return distanceCluster(middlePoints, pointsWithCluster)
    }

    private fun checkClusterRange(i: Int, to: Int) = speedCluster(i, to) && timeCluster(i, to)

    private fun speedCluster(start: Int, end: Int) : Boolean {
        val speeds = track.speeds().subList(start, end)
        if(speeds.isEmpty()) return false
        val speedCount = speeds.filter { it <= speed }.count()
        return ((speedCount/speeds.size) * 100) >= 75
    }

    private fun timeCluster(start: Int, end: Int) : Boolean{
        val durations = track.durations().subList(start, end)
        return durations.sum() <= (range * sec * 3)
    }

    private fun distanceCluster(anchors: List<Point>, points: List<Point>) : List<Point> {
        val stream = if(points.size <= 1000) points.stream() else points.parallelStream()
        val callable = { stream.filter { distanceFilter(it, anchors)} .collect(toList()) }
        return if(stream.isParallel) ForkJoinPool().submit(callable).get() else callable()
    }

    private fun distanceFilter(point: Point, anchors: List<Point>) : Boolean {
        for(a in anchors) {
            if(MapUtils.distance(LatLng(point.latitude, point.longitude), LatLng(a.latitude, a.longitude)) <= distance)
                return false
        }
        return true
    }

}