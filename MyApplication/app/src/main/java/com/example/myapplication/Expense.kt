import java.time.LocalDate

// Класс Expense хранит информацию о конкретном расходе
class Expense(price: Int, category: String, date: LocalDate) {

    // Поля (свойства) класса Expense
    var price: Int = price         // Сумма расхода
    var category: String = category // Категория расхода
    var date: LocalDate = date     // Дата расхода
    var desc: String = ""

    constructor(price: Int, category: String, date: LocalDate, desc: String) : this(price, category, date) {
        this.desc = desc
        var price: Int = price         // Сумма расхода
        var category: String = category // Категория расхода
        var date: LocalDate = date     // Дата расхода
        var desc: String = ""
    }
    // Метод для вывода информации о конкретном расходе
    fun displayExpense() {
        println("$date: $category, Price: $$price")
    }
}
