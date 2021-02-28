import com.beust.klaxon.*
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse


val DISCOGS_CONSUMER_KEY: String = System.getenv("DISCOGS_CONSUMER_KEY")
val DISCOGS_CONSUMER_SECRET: String = System.getenv("DISCOGS_CONSUMER_SECRET")
val DISCOGS_MY_ACCESS_TOKEN: String = System.getenv("DISCOGS_MY_ACCESS_TOKEN")
val DISCOGS_MY_ACCESS_SECRET: String = System.getenv("DISCOGS_MY_ACCESS_SECRET")
val client: HttpClient = HttpClient.newHttpClient()

fun main() {

    print("Verify your username to get your collection >> ")
    val username = readLine()
    val wantsURI = "https://api.discogs.com/users/${username}/wants?per_page=30"
    val collectionURI = "https://api.discogs.com/users/${username}/collection"

    val fullJson = arrayListOf<JsonObject>()

    val wantsJson = getJson(client, wantsURI)
    val collectionJson = getJson(client, collectionURI)

    val tracklist = collectionJson.lookup<String>(
        "releases.basic_information.tracklist.title"
    )

    // pagination testing
    getNextPageJson(collectionJson, fullJson)
    for(i in fullJson) {
        i.lookup<String>(
            "releases.basic_information.title"
        ).let { for(j in it) println(j) }
    }
}

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
    val response = client.send(request, HttpResponse.BodyHandlers.ofString());
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

