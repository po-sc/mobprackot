package beatrate.pro.equipmentapp.data

class EquipmentRepository(
    private val api: EquipmentApi = RetrofitInstance.api
) {
    suspend fun fetchEquipment(): List<EquipmentItem> = api.getEquipment()
}