package com.sinc.mobile.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sinc.mobile.data.model.UnidadProductivaStock

class StockTypeConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromUnidadProductivaStockList(unidades: List<UnidadProductivaStock>?): String? {
        if (unidades == null) {
            return null
        }
        return gson.toJson(unidades)
    }

    @TypeConverter
    fun toUnidadProductivaStockList(json: String?): List<UnidadProductivaStock>? {
        if (json == null) {
            return null
        }
        val type = object : TypeToken<List<UnidadProductivaStock>>() {}.type
        return gson.fromJson(json, type)
    }
}
