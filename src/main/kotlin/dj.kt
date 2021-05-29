import com.beust.klaxon.*
import java.net.http.HttpClient

val client: HttpClient = HttpClient.newHttpClient()

fun main() {

    val startTime = System.nanoTime()
    val collectionURI = "https://api.discogs.com/users/${DISCOGS_USERNAME}/collection?per_page=20"
    val discogs = DiscogsConnector()

    val discogsJson = discogs.getJson(client, collectionURI)
    val collectionList = discogs.getNextPageJson(discogsJson, arrayListOf())

    val artists: MutableList<String> = mutableListOf()
    val records: MutableList<String> = mutableListOf()
    val albumSet: MutableSet<Album> = mutableSetOf()

    for(i in collectionList) {
        i.lookup<String>(
            "releases.basic_information"
        ).let {

            @Suppress("UNCHECKED_CAST")
            artists += it.string("artists_sort") as MutableList<String>

            @Suppress("UNCHECKED_CAST")
            records += it.string("title") as MutableList<String>
        }
    }

    // TODO: some albums are missing due to titles not matching between Spotify and Discogs
    artists.replaceAll {e -> e.replace(Regex(" \\([0-9]\\)"), "")}

    for (i in 0 until records.size)
        albumSet.add(Album(artists[i], records[i]))

    val s = SpotifyConnector()
    val playlistResponse = s.createPlaylist()
    val playlistId = playlistResponse.json.string("id")

    when(playlistResponse.statusCode) {
        201 -> {
            val albumURIs = mutableListOf<String>()
            var trackURIs = mutableListOf<String>()

            println("\nPlaylist $playlistId created! Looking for songs to add to it...")
            for (album in albumSet) {
                s.getSearchJson(client, s.searchURI, album.artist, album.title)
                    .lookup<String>("albums.items.uri")
                    .value.let {
                        if (it.size > 0) {
                            albumURIs.add(it[0])
                        }
                }
            }

            for (uri in albumURIs) {
                for (track in s.getTracks(uri)) {
                    trackURIs.add("\"$track\"")
                }
            }

            println("${trackURIs.size} songs found. Adding...")

            while(trackURIs.size > 100) {
                s.addToPlaylist(trackURIs.subList(0, 99), playlistId)
                trackURIs = trackURIs.subList(100, trackURIs.size-1)
                println("${trackURIs.size} left...")
            }

            s.addToPlaylist(trackURIs, playlistId)

        }
        // TODO -- refresh bearer token within program
        401 -> println("Your bearer token has expired...")
        else -> println("If you're seeing this message, something has gone very wrong")
    }

    val endTime = System.nanoTime()
    println("Done in ${(endTime - startTime) / 1000000000} seconds")
}



