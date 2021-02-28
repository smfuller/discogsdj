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
    val collectionURI = "https://api.discogs.com/users/${username}/collection?per_page=10"

    val collectionJson = getJson(client, collectionURI)

    // pagination testing
    val collectionList = getNextPageJson(collectionJson, arrayListOf())

    val albums = mutableMapOf<String, String>()
    val albumList: MutableList<Album> = mutableListOf()

    for(i in collectionList) {
        i.lookup<String>(
            "releases.basic_information"
        ).let {
            albums.putAll(it.lookup<String>("artists.name").zip(
                it.lookup<String>("title")
            ))
        }
    }

    for ((artist, album) in albums) {
        albumList.add(Album(artist, album))
    }

    for(i in albumList) {
        println("${i.artist} -- ${i.title}")
    }


//    for(i in collectionList) {
//        i.lookup<String>(
//            "releases.basic_information"
//        ).let {
//
//            val artist = it.lookup<String>("artists.name")
//            val album = it.lookup<String>("title")
//            println("$artist -- $album")
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

