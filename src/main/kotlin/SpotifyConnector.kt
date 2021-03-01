import com.beust.klaxon.*
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SpotifyConnector {
    private val SPOTIFY_CLIENT_ID: String = System.getenv("SPOTIFY_CLIENT_ID")              //TODO?
    private val SPOTIFY_CLIENT_SECRET: String = System.getenv("SPOTIFY_CLIENT_SECRET")      //TODO?
    private val SPOTIFY_MY_BEARER_TOKEN: String = System.getenv("SPOTIFY_MY_BEARER_TOKEN")
    private val SPOTIFY_REFRESH_TOKEN: String = System.getenv("SPOTIFY_REFRESH_TOKEN")      //TODO?
    private val SPOTIFY_USERNAME: String = System.getenv("SPOTIFY_USERNAME")


    // TODO: refactor URI handling
    val playlistURI = "https://api.spotify.com/v1/users/$SPOTIFY_USERNAME/playlists"
    val searchURI = "https://api.spotify.com/v1/search"
    val playlistUpdateURI = "https://api.spotify.com/v1/playlists"


    // TODO: clean up all JSON methods (lots of duplicate code)
    fun getSearchJson(client: HttpClient, uri: String, artist: String, album: String): JsonObject {
        val request = HttpRequest
            .newBuilder(URI.create("$uri?q=${("$artist+$album").replace(" ", "+")}&type=album"))
            .header("accept", "application.json")
            .header(
                "Authorization",
                "Bearer $SPOTIFY_MY_BEARER_TOKEN"
            )
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val responseStream: InputStream = response.body().byteInputStream()

        return Parser.default().parse(responseStream) as JsonObject
    }

    fun createPlaylist(): JsonResponse {
        val now = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a")
        )

        val playlistJson = """
        {
            "name": "DiscogsDJ - $now",
            "description": "This is a playlist",
            "public": false
        }
        """.trimIndent()
        val request = HttpRequest
            .newBuilder(URI.create(playlistURI))
            .header("accept", "application.json")
            .header(
                "Authorization",
                "Bearer $SPOTIFY_MY_BEARER_TOKEN"
            )
            .POST(HttpRequest.BodyPublishers.ofString(playlistJson))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return JsonResponse(
            response.statusCode(),
            Parser.default().parse(response.body().byteInputStream()) as JsonObject
        )
    }

    fun addToPlaylist(uris: MutableList<String>, playlist: String?): JsonResponse {
        val bodyJson = """
            {
              "uris": $uris
            }
        """.trimIndent()


        println(bodyJson)
        val request = HttpRequest
            .newBuilder(URI.create("$playlistUpdateURI/$playlist/tracks"))
            .header("accept", "application.json")
            .header(
                "Authorization",
                "Bearer $SPOTIFY_MY_BEARER_TOKEN"
            )
            .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        println(response)
        return JsonResponse(
            response.statusCode(),
            Parser.default().parse(response.body().byteInputStream()) as JsonObject
        )
    }

    fun getTracks(uri: String): JsonArray<String> {
        val request = HttpRequest
            .newBuilder(URI.create("https://api.spotify.com/v1/albums/${uri
                .replace("spotify:album:", "")}/tracks"))
            .header("accept", "application.json")
            .header(
                "Authorization",
                "Bearer $SPOTIFY_MY_BEARER_TOKEN"
            )
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val responseStream: InputStream = response.body().byteInputStream()

        return (Parser.default().parse(responseStream) as JsonObject).lookup("items.uri")
    }
}