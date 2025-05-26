//class ExpenseList {
//    private val expenses = mutableListOf<Expense>()
//    fun addExpense(expense: Expense) {
//        expenses.add(expense)
//    }
//    fun displayExpenses() {
//        if (expenses.isEmpty()) {
//            println("No expenses added.")
//        } else {
//            for (expense in expenses) {
//                expense.displayExpense()
//            }
//        }
//    }
//    fun getTotalPriceByCategory(): Map<String, Int> {
//        val categoryTotals = mutableMapOf<String, Int>()
//        for (expense in expenses) {
//            val category = expense.category
//            val price = expense.price
//            if (categoryTotals.containsKey(category)) {
//                val currentTotal = categoryTotals[category]!!
//                categoryTotals[category] = currentTotal + price
//            } else {
//                categoryTotals[category] = price
//            }
//        }
//        return categoryTotals
//    }
//}
