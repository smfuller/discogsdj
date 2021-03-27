import com.beust.klaxon.JsonObject

data class JsonResponse(
    val statusCode: Int,
    val json: JsonObject

    // TODO -- more valuable info to include here?
)