package com.sinc.mobile.domain.model

data class InitData(
    val appControl: AppControl?,
    val userContext: UserContext?,
    val features: Features?,
    val configuration: Configuration?
)

data class AppControl(
    val minVersion: String?,
    val latestVersion: String?,
    val updateRequired: Boolean?,
    val storeUrl: String?,
    val maintenanceMode: Boolean?,
    val message: String?
)

data class UserContext(
    val id: Int?,
    val name: String?,
    val email: String?,
    val role: String?,
    val setupCompleted: Boolean?,
    val productorId: Int?,
    val defaultFarmId: Int?
)

data class Features(
    val moduleStock: Boolean?,
    val moduleLabors: Boolean?,
    val moduleSales: Boolean?,
    val allowOfflineSync: Boolean?
)

data class Configuration(
    val syncIntervalMinutes: Int?,
    val catalogsVersion: String?
)
