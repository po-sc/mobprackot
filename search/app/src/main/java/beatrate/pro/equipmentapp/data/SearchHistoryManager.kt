package beatrate.pro.equipmentapp.data

import android.content.Context
import android.content.Context.MODE_PRIVATE

object SearchHistoryManager {
    private const val PREF      = "search_history_prefs"
    private const val KEY_LIST  = "history_csv"
    private const val MAX       = 10
    private const val SEP       = "|"

    private fun load(ctx: Context) =
        ctx.getSharedPreferences(PREF, MODE_PRIVATE)
            .getString(KEY_LIST, "")!!
            .split(SEP).filter { it.isNotBlank() }.toMutableList()

    private fun save(ctx: Context, list: List<String>) =
        ctx.getSharedPreferences(PREF, MODE_PRIVATE)
            .edit().putString(KEY_LIST, list.joinToString(SEP)).apply()

    fun get(ctx: Context): List<String> = load(ctx)

    fun add(ctx: Context, q: String) {
        if (q.isBlank()) return
        val list = load(ctx)
        list.remove(q)
        list.add(0, q)
        if (list.size > MAX) list.removeLast()
        save(ctx, list)
    }

    fun clear(ctx: Context) = save(ctx, emptyList())
}