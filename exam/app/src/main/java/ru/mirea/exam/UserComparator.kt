
import java.io.File

data class User(val name: String, val age: Int, val email: String)

class UserComparator : Comparator<User> {
    override fun compare(u1: User, u2: User): Int {

        val ageCompare = u1.age.compareTo(u2.age)

        return if (ageCompare != 0) {
            ageCompare
        } else {
            u1.name.compareTo(u2.name)
        }
    }
}

fun main() {
    val lines = File("/Users/po_scripty/AndroidStudioProjects/MyApplication/app/src/main/java/com/example/myapplication/input.txt").readLines()

    val users = lines.map { line ->

        val parts = line.split(" ")
        User(
            name = parts[0],
            age = parts[1].toInt(),
            email = parts[2]
        )
    }

    val sortedUsers = users.sortedWith(UserComparator())

    File("/Users/po_scripty/AndroidStudioProjects/MyApplication/app/src/main/java/com/example/myapplication/output.txt").printWriter().use { writer ->
        for (user in sortedUsers) {
            writer.println("${user.name} ${user.age} ${user.email}")
        }
    }
}
