package beatrate.pro.equipmentapp.data      // ← ваш пакет

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)          // ← обязательно!
data class EquipmentItem(
    val id: String,
    val name: String,
    val image: String
)