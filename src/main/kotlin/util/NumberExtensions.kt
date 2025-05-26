package util

/**
 * Example:
 * ```kt
 * 5.secondsToTicks() // -> 100 ticks
 * ```
 */
fun Int.secondsToTicks(): Int {
    return this.times(20)
}

fun Int.timeRemainingFormatted(): String {
    if (this < 0) return "Invalid time supplied"

    val hours = this / 60
    val mins = this % 60

    return when {
        hours > 0 && mins > 0 -> "${hours}hr ${mins}m"
        hours > 0 -> "${hours}hr"
        mins > 0 -> "${mins}m"
        else -> "0m"
    }
}