import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

val TOKEN: String = System.getenv("SPOTIFY_MY_ACCESS_TOKEN")
val spotifyClient: HttpClient = HttpClient.newHttpClient()

fun main() {
    val spotifyURI = "https://api.spotify.com/v1/tracks?ids=3n3Ppam7vgaVa1iaRUc9Lp%2C3twNvmDtFQtAd5gMKedhLD"
    print(getSpotifyJson(spotifyClient, spotifyURI).toJsonString(true))


}

fun getSpotifyJson(client: HttpClient, uri: String): JsonObject {
    val request = HttpRequest
        .newBuilder(URI.create(uri))
        .header("accept", "application.json")
        .header("Authorization",
            "Bearer $TOKEN")
        .build()
    val response = client.send(request, HttpResponse.BodyHandlers.ofString());
    val responseStream: InputStream = response.body().byteInputStream()

    return Parser.default().parse(responseStream) as JsonObject
}