// Класс ExpenseList управляет списком всех расходов
class ExpenseList {

    // Список для хранения всех расходов
    private val expenses = mutableListOf<Expense>()

    // Метод для добавления нового расхода в список
    fun addExpense(expense: Expense) {
        expenses.add(expense)
    }

    // Метод для вывода всех расходов
    fun displayExpenses() {
        if (expenses.isEmpty()) {
            println("No expenses added.")
        } else {
            // Используем цикл for для перебора всех расходов в списке
            for (expense in expenses) {
                expense.displayExpense() // Выводим информацию о каждом расходе
            }
        }
    }

    // Метод для подсчета суммы всех расходов по каждой категории
    fun getTotalPriceByCategory(): Map<String, Int> {
        // Создаем пустую карту для хранения итогов по категориям
        val categoryTotals = mutableMapOf<String, Int>()

        // Перебираем каждый расход в списке
        for (expense in expenses) {
            val category = expense.category // Получаем категорию текущего расхода
            val price = expense.price       // Получаем сумму текущего расхода

            // Проверяем, есть ли уже эта категория в карте
            if (categoryTotals.containsKey(category)) {
                // Если категория уже есть, увеличиваем сумму
                val currentTotal = categoryTotals[category]!!
                categoryTotals[category] = currentTotal + price
            } else {
                // Если категории нет, добавляем ее с текущей суммой
                categoryTotals[category] = price
            }
        }

        // Возвращаем карту с итоговыми суммами по категориям
        return categoryTotals
    }
}
