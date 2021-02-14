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

fun main() {

    print("Verify your username to get your collection >> ")
    val username = readLine()

    val client = HttpClient.newHttpClient()
    val request = HttpRequest
        .newBuilder(URI.create("https://api.discogs.com/users/${username}/collection"))
        .header("accept", "application.json")
        .header("Authorization",
            "OAuth oauth_consumer_key=\"${DISCOGS_CONSUMER_KEY}\"," +
                    "oauth_token=\"${DISCOGS_MY_ACCESS_TOKEN}\"," +
                    "oauth_signature_method=\"HMAC-SHA1\"," +
                    "oauth_version=\"1.0\"")
        .build()
    val response = client.send(request, HttpResponse.BodyHandlers.ofString());
    val responseStream: InputStream = response.body().byteInputStream()
    val json = Parser.default().parse(responseStream) as JsonObject
    val tracklist = json.lookup<String>(
        "releases.basic_information.tracklist.title"
    )
    val iterate = tracklist.listIterator()
    while(iterate.hasNext()) {
        val i = iterate.next()
        if(i.startsWith("The "))
            iterate.set(i.substring(startIndex = 4).plus(", The "))
    }

    tracklist.sort()
    for(i in tracklist.distinct()) {
        println(i)
    }
}

