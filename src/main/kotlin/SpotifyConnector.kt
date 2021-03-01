import com.beust.klaxon.*
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SpotifyConnector (client: HttpClient, albums: MutableList<Album>){
    val SPOTIFY_CLIENT_ID: String = System.getenv("SPOTIFY_CLIENT_ID")
    val SPOTIFY_CLIENT_SECRET: String = System.getenv("SPOTIFY_CLIENT_SECRET")
    val SPOTIFY_MY_BEARER_TOKEN: String = System.getenv("SPOTIFY_MY_BEARER_TOKEN")
    val SPOTIFY_REFRESH_TOKEN: String = System.getenv("SPOTIFY_REFRESH_TOKEN")
    val SPOTIFY_USERNAME: String = System.getenv("SPOTIFY_USERNAME")

    val playlistURI = "https://api.spotify.com/v1/users/$SPOTIFY_USERNAME/playlists"
    val searchURI = "https://api.spotify.com/v1/search"

    fun getSearchJson(client: HttpClient, uri: String, artist: String, album: String): JsonObject {
        val request = HttpRequest
            .newBuilder(URI.create("$uri?q=${("$artist+$album").replace(" ", "+")}&type=album"))
            .header("accept", "application.json")
            .header(
                "Authorization",
                "Bearer $SPOTIFY_MY_BEARER_TOKEN"
            )
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString());
        val responseStream: InputStream = response.body().byteInputStream()

        return Parser.default().parse(responseStream) as JsonObject
    }

    fun createPlaylist(): Int {
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
        return response.statusCode()
    }
//
//    fun addToPlaylist(albumURI: String): Int {
//        val request = HttpRequest
//            .newBuilder(URI.create(playlistURI))
//            .header("accept", "application.json")
//            .header(
//                "Authorization",
//                "Bearer $SPOTIFY_MY_BEARER_TOKEN"
//            )
//            .POST(HttpRequest.BodyPublishers.ofString())
//            .build()
//
//        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
//        return response.statusCode()
//    }
}