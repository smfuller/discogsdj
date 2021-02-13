import com.beust.klaxon.*
import java.io.File

fun main() {
    val test = Parser.default()
        .parse(File("wow.json").bufferedReader()) as JsonObject

    val array = test.lookup<String>(
        "releases.basic_information.tracklist.title"
    )

    val iterate = array.listIterator()

    while(iterate.hasNext()) {
        val i = iterate.next()
        if(i.startsWith("The "))
            iterate.set(i.substring(startIndex = 4).plus(", The "))
    }

    array.sort()

    for(i in array.distinct()) {
        println(i)
    }
}

