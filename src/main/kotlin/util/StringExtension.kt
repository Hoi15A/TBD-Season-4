package util

private val VOWELS = listOf('a', 'e', 'i', 'o', 'u')

/**
 * Check if the string begins with a vowel.
 * Case-insensitive, if empty or not a vowel returns false.
 * [VOWELS] are a, e, i, o, u.
 */
fun String.startsWithVowel(): Boolean {
    try {
        val firstChar = this.lowercase().first()
        return VOWELS.contains(firstChar)
    } catch (ex: NoSuchElementException) {
        return false
    }
}

/**
 * Strips the token suffix for Island Exchange usage,
 * as well as any appended stars which represent
 * weapon skin tiers
 */
fun String.stripTokenSuffix(): String = replace(Regex(" Token( ★+)?\\s*$"), "")