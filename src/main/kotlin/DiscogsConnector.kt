import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.beust.klaxon.lookup
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

val DISCOGS_CONSUMER_KEY: String = System.getenv("DISCOGS_CONSUMER_KEY")
val DISCOGS_CONSUMER_SECRET: String = System.getenv("DISCOGS_CONSUMER_SECRET")
val DISCOGS_MY_ACCESS_TOKEN: String = System.getenv("DISCOGS_MY_ACCESS_TOKEN")
val DISCOGS_MY_ACCESS_SECRET: String = System.getenv("DISCOGS_MY_ACCESS_SECRET")
val DISCOGS_USERNAME: String = System.getenv("DISCOGS_USERNAME")

class DiscogsConnector {
    fun getJson(client: HttpClient, uri: String): JsonObject {
        val request = HttpRequest
            .newBuilder(URI.create(uri))
            .header("accept", "application.json")
            .header("Authorization",
                "OAuth oauth_consumer_key=\"${DISCOGS_CONSUMER_KEY}\"," +
                        "oauth_token=\"${DISCOGS_MY_ACCESS_TOKEN}\"," +
                        "oauth_signature_method=\"HMAC-SHA1\"," +
                        "oauth_version=\"1.0\"")
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val responseStream: InputStream = response.body().byteInputStream()

        return Parser.default().parse(responseStream) as JsonObject
    }

    fun getNextPageJson(json: JsonObject, list: ArrayList<JsonObject>): ArrayList<JsonObject> {
        println("Adding a json object...")
        list.add(json)
        val nextPage = json.lookup<String?>(
            "pagination.urls.next"
        )

        // keep going if there are more pages
        nextPage.value[0]?.let {
            getNextPageJson(getJson(client, it), list)
        }

        return list
    }

}