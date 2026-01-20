package com.sinc.mobile.data.mapper

import com.sinc.mobile.data.network.dto.AppControlDto
import com.sinc.mobile.data.network.dto.ConfigurationDto
import com.sinc.mobile.data.network.dto.FeaturesDto
import com.sinc.mobile.data.network.dto.InitResponseDto
import com.sinc.mobile.data.network.dto.UserContextDto
import com.sinc.mobile.domain.model.AppControl
import com.sinc.mobile.domain.model.Configuration
import com.sinc.mobile.domain.model.Features
import com.sinc.mobile.domain.model.InitData
import com.sinc.mobile.domain.model.UserContext

fun InitResponseDto.toDomain(): InitData {
    return InitData(
        appControl = appControl?.toDomain(),
        userContext = userContext?.toDomain(),
        features = features?.toDomain(),
        configuration = configuration?.toDomain()
    )
}

fun AppControlDto.toDomain() = AppControl(
    minVersion = minVersion,
    latestVersion = latestVersion,
    updateRequired = updateRequired,
    storeUrl = storeUrl,
    maintenanceMode = maintenanceMode,
    message = message
)

fun UserContextDto.toDomain() = UserContext(
    id = id,
    name = name,
    email = email,
    role = role,
    setupCompleted = setupCompleted,
    productorId = productorId,
    defaultFarmId = defaultFarmId
)

fun FeaturesDto.toDomain() = Features(
    moduleStock = moduleStock,
    moduleLabors = moduleLabors,
    moduleSales = moduleSales,
    allowOfflineSync = allowOfflineSync
)

fun ConfigurationDto.toDomain() = Configuration(
    syncIntervalMinutes = syncIntervalMinutes,
    catalogsVersion = catalogsVersion
)
