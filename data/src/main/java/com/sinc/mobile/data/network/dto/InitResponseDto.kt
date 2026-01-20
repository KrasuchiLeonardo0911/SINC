package com.sinc.mobile.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InitResponseDto(
    @SerialName("app_control") val appControl: AppControlDto?,
    @SerialName("user_context") val userContext: UserContextDto?,
    val features: FeaturesDto?,
    val configuration: ConfigurationDto?
)

@Serializable
data class AppControlDto(
    @SerialName("min_version") val minVersion: String?,
    @SerialName("latest_version") val latestVersion: String?,
    @SerialName("update_required") val updateRequired: Boolean?,
    @SerialName("store_url") val storeUrl: String?,
    @SerialName("maintenance_mode") val maintenanceMode: Boolean?,
    val message: String?
)

@Serializable
data class UserContextDto(
    val id: Int?,
    val name: String?,
    val email: String?,
    val role: String?,
    @SerialName("setup_completed") val setupCompleted: Boolean?,
    @SerialName("productor_id") val productorId: Int?,
    @SerialName("default_farm_id") val defaultFarmId: Int?
)

@Serializable
data class FeaturesDto(
    @SerialName("module_stock") val moduleStock: Boolean?,
    @SerialName("module_labors") val moduleLabors: Boolean?,
    @SerialName("module_sales") val moduleSales: Boolean?,
    @SerialName("allow_offline_sync") val allowOfflineSync: Boolean?
)

@Serializable
data class ConfigurationDto(
    @SerialName("sync_interval_minutes") val syncIntervalMinutes: Int?,
    @SerialName("catalogs_version") val catalogsVersion: String?
)
