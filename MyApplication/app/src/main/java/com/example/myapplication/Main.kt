import java.time.LocalDate

// Создаем объект ExpenseList для управления расходами
fun main(args: Array<String>) {
    val expenseList = ExpenseList()

    // Добавление расходов в список
    val expensesToAdd = arrayOf(
        Expense(350, "Health", LocalDate.parse("2024-09-01")),
        Expense(120, "Groceries", LocalDate.parse("2024-09-02")),
        Expense(500, "Rent", LocalDate.parse("2024-09-03")),
        Expense(150, "Entertainment", LocalDate.parse("2024-09-04")),
        Expense(75, "Groceries", LocalDate.parse("2024-09-05")),
        Expense(600, "Health", LocalDate.parse("2024-09-06")),
        Expense(50, "Transport", LocalDate.parse("2024-09-07")),
        Expense(300, "Rent", LocalDate.parse("2024-09-08")),
        Expense(90, "Entertainment", LocalDate.parse("2024-09-09")),
        Expense(200, "Groceries", LocalDate.parse("2024-09-10")),
        Expense(800, "Travel", LocalDate.parse("2024-09-15"))
    )

    for (expense in expensesToAdd) {
        expenseList.addExpense(expense)
    }

    println("All expenses:")
    expenseList.displayExpenses()

    println("\nTotal price by category:")
    val totalByCategory = expenseList.getTotalPriceByCategory()
    for ((category, total) in totalByCategory) {
        println("$category: $total")
    }
}
