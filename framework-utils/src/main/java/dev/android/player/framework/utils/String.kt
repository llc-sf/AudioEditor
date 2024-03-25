package dev.android.player.framework.utils


/**
 * a.mp3 -> mp3
 */
fun String.fileName2Extension(): String {
    val lastIndexOf = lastIndexOf(".")
    return if (lastIndexOf == -1) "" else substring(lastIndexOf + 1)
}

/**
 * a.mp3 -> .mp3
 */
fun String.fileName2ExtensionWithPoint(): String {
    val lastIndexOf = lastIndexOf(".")
    return "." + (if (lastIndexOf == -1) "" else substring(lastIndexOf + 1))
}


/**
 * a.mp3 -> a
 */
fun String.fileName2Name(): String {
    val lastIndexOf = lastIndexOf(".")
    return if (lastIndexOf == -1) "" else substring(0, lastIndexOf)
}


/**
 * /storage/emulated/0/DCIM/Camera/zzzzz7.mp4 -> zzzzz7.mp4
 */
fun String.filePath2FileNameWithExtension(): String {
    val lastIndexOf = lastIndexOf("/")
    return if (lastIndexOf == -1) "" else substring(lastIndexOf + 1)
}


/**
 * /storage/emulated/0/DCIM/Camera/zzzzz7.mp4 -> zzzzz7
 */
fun String.filePath2FileNameWithoutExtension(): String {
    val lastIndexOf = lastIndexOf("/")
    val lastIndexOf2 = lastIndexOf(".")
    return if (lastIndexOf == -1) "" else substring(lastIndexOf + 1, lastIndexOf2)
}


fun String.maxLength(maxLength: Int): String {
    return if (length > maxLength) {
        substring(0, maxLength)
    } else {
        this
    }
}
