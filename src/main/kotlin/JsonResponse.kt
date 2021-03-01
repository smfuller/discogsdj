import com.beust.klaxon.JsonObject

data class JsonResponse(
    val statusCode: Int,
    val json: JsonObject
)