package com.example.myapplication

data class User(val name: String, val age: Int, val email: String)

class UserComparator : Comparator<User> {
    override fun compare(u1: User, u2: User): Int {
        // Сначала сравниваем по возрасту
        val ageCompare = u1.age.compareTo(u2.age)
        // Если возраста совпадают, сравниваем по имени
        return if (ageCompare != 0) {
            ageCompare
        } else {
            u1.name.compareTo(u2.name)
        }
    }
}

fun main() {
    val list = listOf(
        User("Sam", 20, "sam@dot.com"),
        User("Max", 21, "max@name.com"),
        User("Alex", 21, "alex@name.com"),
        User("Alex", 19, "alex20@name.com")
    )
    // Выводим список, отсортированный с помощью нашего компаратора
    println(list.sortedWith(UserComparator()))
}
