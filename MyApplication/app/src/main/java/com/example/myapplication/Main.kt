//import java.time.LocalDate
//
//// Создаем объект ExpenseList для управления расходами
//fun main(args: Array<String>) {
//    val expenseList = ExpenseList()
//
//    // Добавление расходов в список
//    val expensesToAdd = arrayOf(
//        Expense(350, "Health", LocalDate.parse("2024-09-01")),
//        Expense(120, "Groceries", LocalDate.parse("2024-09-02")),
//        Expense(500, "Rent", LocalDate.parse("2024-09-03")),
//        Expense(150, "Entertainment", LocalDate.parse("2024-09-04")),
//        Expense(75, "Groceries", LocalDate.parse("2024-09-05")),
//        Expense(600, "Health", LocalDate.parse("2024-09-06")),
//        Expense(50, "Transport", LocalDate.parse("2024-09-07")),
//        Expense(300, "Rent", LocalDate.parse("2024-09-08")),
//        Expense(90, "Entertainment", LocalDate.parse("2024-09-09")),
//        Expense(200, "Groceries", LocalDate.parse("2024-09-10")),
//        Expense(800, "Travel", LocalDate.parse("2024-09-15"))
//    )
//
//    for (expense in expensesToAdd) {
//        expenseList.addExpense(expense)
//    }
//
//    println("All expenses:")
//    expenseList.displayExpenses()
//
//    println("\nTotal price by category:")
//    val totalByCategory = expenseList.getTotalPriceByCategory()
//    for ((category, total) in totalByCategory) {
//        println("$category: $total")
//    }
//}




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
