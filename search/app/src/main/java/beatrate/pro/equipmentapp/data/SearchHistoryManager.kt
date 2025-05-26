package beatrate.pro.equipmentapp.data

import android.content.Context
import android.content.Context.MODE_PRIVATE

object SearchHistoryManager {

    private const val PREF      = "search_history_prefs"
    private const val KEY_LIST  = "history_csv"
    private const val MAX_ITEMS = 10
    private const val SEP       = "|"

    private fun loadList(ctx: Context): MutableList<String> =
        ctx.getSharedPreferences(PREF, MODE_PRIVATE)
            .getString(KEY_LIST, "")!!
            .split(SEP)
            .filter { it.isNotBlank() }
            .toMutableList()

    private fun saveList(ctx: Context, list: List<String>) {
        ctx.getSharedPreferences(PREF, MODE_PRIVATE)
            .edit()
            .putString(KEY_LIST, list.joinToString(SEP))
            .apply()
    }

    /** вернуть список (уже без пустых строк) */
    fun getHistory(ctx: Context): List<String> = loadList(ctx)

    /** добавить запрос (уникально, в начало) */
    fun addQuery(ctx: Context, query: String) {
        if (query.isBlank()) return
        val list = loadList(ctx)
        list.remove(query)                // убираем дубликаты
        list.add(0, query)                // кладём в начало
        if (list.size > MAX_ITEMS) list.removeLast()
        saveList(ctx, list)
    }

    /** очистить историю */
    fun clear(ctx: Context) = saveList(ctx, emptyList())
}