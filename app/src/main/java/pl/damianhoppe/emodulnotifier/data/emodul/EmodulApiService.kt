package pl.damianhoppe.emodulnotifier.data.emodul

import pl.damianhoppe.emodulnotifier.utils.AuthorizationHeader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmodulApiService @Inject constructor(
    private val emodulApi: EmodulApi
) {

    fun getFuelSupplyLevel(token: String, userId: String, moduleId: String): Int {
        val moduleDetails =
            emodulApi.fetchModuleDetails(AuthorizationHeader.Bearer(token), userId, moduleId)
                .execute()
        val json = moduleDetails.body()!!
        val tiles = json.asJsonObject.get("tiles").asJsonArray
        val fuelSupplyTile = tiles.first { it.asJsonObject.get("id").asInt == 4060 }!!.asJsonObject
        return fuelSupplyTile.get("params").asJsonObject.get("percentage").asInt
    }
}