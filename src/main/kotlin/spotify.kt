import com.beust.klaxon.*
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


val SPOTIFY_CLIENT_ID: String = System.getenv("SPOTIFY_CLIENT_ID")
val SPOTIFY_CLIENT_SECRET: String = System.getenv("SPOTIFY_CLIENT_SECRET")
val SPOTIFY_MY_BEARER_TOKEN: String = System.getenv("SPOTIFY_MY_BEARER_TOKEN")
val SPOTIFY_REFRESH_TOKEN: String = System.getenv("SPOTIFY_REFRESH_TOKEN")
val SPOTIFY_USERNAME: String = System.getenv("SPOTIFY_USERNAME")
val spotifyClient: HttpClient = HttpClient.newHttpClient()

fun main() {
    val spotifyURI = "https://api.spotify.com/v1/users/$SPOTIFY_USERNAME/playlists"

    println(createPlaylist(spotifyClient, spotifyURI))
}

fun getSpotifyJson(client: HttpClient, uri: String): JsonObject {
    val request = HttpRequest
        .newBuilder(URI.create(uri))
        .header("accept", "application.json")
        .header("Authorization",
            "Bearer $SPOTIFY_MY_BEARER_TOKEN")
        .build()
    val response = client.send(request, HttpResponse.BodyHandlers.ofString());
    val responseStream: InputStream = response.body().byteInputStream()

    return Parser.default().parse(responseStream) as JsonObject
}

fun createPlaylist(client: HttpClient, uri: String): String {
    val now = LocalDateTime.now().format(DateTimeFormatter
        .ofPattern("yyyy-MM-dd hh:mm:ss a"))

    val playlistJson = """
        {
            "name": "DiscogsDJ - $now",
            "description": "This is a playlist",
            "public": false
        }
    """.trimIndent()
    val request = HttpRequest
        .newBuilder(URI.create(uri))
        .header("accept", "application.json")
        .header("Authorization",
            "Bearer $SPOTIFY_MY_BEARER_TOKEN")
        .POST(HttpRequest.BodyPublishers.ofString(playlistJson))
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}