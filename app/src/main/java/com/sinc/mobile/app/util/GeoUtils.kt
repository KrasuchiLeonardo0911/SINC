package com.sinc.mobile.app.util

import com.sinc.mobile.domain.model.DomainGeoPoint

object GeoUtils {

    /**
     * Checks if a given point is inside a given polygon.
     * Uses the Ray-Casting algorithm.
     *
     * @param point The point to check.
     * @param polygon The list of vertices forming the polygon.
     * @return True if the point is inside the polygon, false otherwise.
     */
    fun isPointInPolygon(point: DomainGeoPoint, polygon: List<DomainGeoPoint>): Boolean {
        if (polygon.isEmpty()) {
            return false
        }

        var intersects = 0
        for (i in polygon.indices) {
            val p1 = polygon[i]
            val p2 = polygon[(i + 1) % polygon.size]

            // Check if the point is on a vertex
            if (point.longitude == p1.longitude && point.latitude == p1.latitude) {
                return true
            }

            // Check if the point is on a horizontal boundary
            if (p1.latitude == p2.latitude && point.latitude == p1.latitude) {
                if ((point.longitude > p1.longitude && point.longitude < p2.longitude) ||
                    (point.longitude > p2.longitude && point.longitude < p1.longitude)) {
                    return true
                }
            }

            // Ray-casting check
            if ((p1.latitude < point.latitude && p2.latitude >= point.latitude) ||
                (p2.latitude < point.latitude && p1.latitude >= point.latitude)) {

                // Calculate the x-intersection of the line segment with the horizontal ray
                val xIntersection = (point.latitude - p1.latitude) * (p2.longitude - p1.longitude) /
                        (p2.latitude - p1.latitude) + p1.longitude

                if (xIntersection > point.longitude) {
                    intersects++
                }
            }
        }
        // If the number of intersections is odd, the point is inside.
        return (intersects % 2 == 1)
    }
}
