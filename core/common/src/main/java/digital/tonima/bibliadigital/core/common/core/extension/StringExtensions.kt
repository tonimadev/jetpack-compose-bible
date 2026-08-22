package digital.tonima.bibliadigital.core.common.core.extension

import java.text.Normalizer

fun String.removeAccents(): String {
    var string = this
    val out = CharArray(this.length)
    string = Normalizer.normalize(string, Normalizer.Form.NFD)
    var j = 0
    run {
        var i = 0
        val n = string.length
        while (i < n) {
            val c = string[i]
            if (c <= '\u007F') out[j++] = c
            ++i
        }
    }
    return String(out)
}
