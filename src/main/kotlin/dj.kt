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
    val collectionURI = "https://api.discogs.com/users/${username}/collection?per_page=20"

    val collectionList = getNextPageJson(getJson(client, collectionURI), arrayListOf())

    val artists: MutableList<String> = mutableListOf()
    val records: MutableList<String> = mutableListOf()
    val albumList: MutableList<Album> = mutableListOf()

    for(i in collectionList) {
        i.lookup<String>(
            "releases.basic_information"
        ).let {

            // TODO: find a way to clean up these unchecked casts!
            artists.addAll(it.string("artists_sort") as MutableList<String>)
            records.addAll(it.string("title") as MutableList<String>)
        }
    }

    artists.replaceAll {e -> e.replace(Regex(" \\([0-9]\\)"), "")}

    for (i in 0 until records.size)
        albumList.add(Album(artists[i], records[i]))

    val s = SpotifyConnector(client, albumList)
    when(s.createPlaylist()) {
        201 -> {
            println("Playlist created! Looking for albums to add to it...")
            for (album in albumList) {
                s.getSearchJson(client, s.searchURI, album.artist, album.title)
                    .lookup<String>("albums.items.uri")
                    .value.let {
                        if (it.size > 0) {
                        }
                }
            }
        }
        401 -> println("Your bearer token has expired...")
        else -> println("If you're seeing this message, something has gone very wrong")
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

