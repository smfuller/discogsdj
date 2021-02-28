import com.beust.klaxon.*
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse


val SPOTIFY_CLIENT_ID: String = System.getenv("SPOTIFY_CLIENT_ID")
val SPOTIFY_CLIENT_SECRET: String = System.getenv("SPOTIFY_CLIENT_SECRET")
val SPOTIFY_MY_BEARER_TOKEN = System.getenv("SPOTIFY_MY_BEARER_TOKEN")
val SPOTIFY_REFRESH_TOKEN: String = System.getenv("SPOTIFY_REFRESH_TOKEN")
val spotifyClient: HttpClient = HttpClient.newHttpClient()

fun main() {
    val spotifyURI = "https://api.spotify.com/v1/tracks?ids=3n3Ppam7vgaVa1iaRUc9Lp%2C3twNvmDtFQtAd5gMKedhLD"
    val spotifyJson = getSpotifyJson(spotifyClient, spotifyURI)
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