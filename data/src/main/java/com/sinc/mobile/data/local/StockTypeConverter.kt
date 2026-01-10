package com.sinc.mobile.data.local

import androidx.room.TypeConverter
import com.sinc.mobile.data.model.UnidadProductivaStock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class StockTypeConverter {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromUnidadProductivaStockList(unidades: List<UnidadProductivaStock>?): String? {
        if (unidades == null) {
            return null
        }
        return json.encodeToString(unidades)
    }

    @TypeConverter
    fun toUnidadProductivaStockList(jsonString: String?): List<UnidadProductivaStock>? {
        if (jsonString == null) {
            return null
        }
        return json.decodeFromString<List<UnidadProductivaStock>>(jsonString)
    }
}
