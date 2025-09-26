package util

import java.security.MessageDigest
import java.time.*
import java.time.format.DateTimeParseException


fun sha1(data: ByteArray): String {
    val algorithm = "SHA-1"
    return calculateDigestForAlgorithm(algorithm, data)
}

fun sha256(data: ByteArray): String {
    val algorithm = "SHA-256"
    return calculateDigestForAlgorithm(algorithm, data)
}

private fun calculateDigestForAlgorithm(algorithm: String, data: ByteArray): String {
    val digest = MessageDigest.getInstance(algorithm)
    val hashedBytes = digest.digest(data)
    return hashedBytes.joinToString("") { "%02x".format(it) }
}

fun dateTimeDifference(end: String): String {
    return try {
        val now = Instant.now()
        val endTime = Instant.parse(end)
        val duration = Duration.between(now, endTime)
        if(duration.isNegative) {
            "<red>This has somehow achieved time travel..."
        } else {
            "${duration.toDaysPart()}d, ${duration.toHoursPart()}h, ${duration.toMinutesPart()}m"
        }
    } catch (e: DateTimeParseException) {
        "<red>Invalid time format."
    }
}