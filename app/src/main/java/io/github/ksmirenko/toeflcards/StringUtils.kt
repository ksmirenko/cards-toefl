package io.github.ksmirenko.toeflcards

/**
 * Text data conversion utils.
 *
 * @author Kirill Smirenko
 */
object StringUtils {
    private const val separator = "_,_"

    fun <T> listToString(list : List<T>) : String {
        val sb = StringBuilder()
        list.forEach { str ->
            sb.append(separator)
            sb.append(str)
        }
        sb.delete(0, separator.length - 1)
        return sb.toString()
    }

    @Suppress("unused")
    fun stringToIntList(str : String) : List<Int> {
        val arr = str.split(separator.toRegex()).dropLastWhile { it.isEmpty() }.map { it.toInt() }
        return arr
    }

    /**
     * Converts [str] of Utils.listToString format to SQL request ready format.
     */
    fun stringToSqlReadyString(str : String) = "(" + str.replace("_", "") + ")"
}
