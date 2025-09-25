package com.lerchenflo.hallenmanager.util

import kotlin.math.min

// ---------- normalizeForSearch (KMP-safe, no java.text.Normalizer) ----------

fun String.normalizeForSearch(): String {
    // lowercase first (KMP-safe)
    val lower = this.lowercase()

    // remove diacritics using a mapping (covers common Latin-script diacritics)
    val sb = StringBuilder(lower.length)
    for (ch in lower) {
        sb.append(DIACRITICS_MAP[ch] ?: ch)
    }
    val mapped = sb.toString()

    // keep only letters, digits and whitespace; replace others with space
    // Regex with Unicode properties should work in KMP Kotlin/Native and JS too
    val cleaned = mapped.replace(Regex("[^\\p{L}\\p{Nd}\\s]"), " ")

    // collapse whitespace and trim
    return cleaned.replace(Regex("\\s+"), " ").trim()
}

// Basic mapping of accented/diacritic characters to ASCII equivalents.
// We lowercase the input first, so only lowercase keys are needed.
private val DIACRITICS_MAP: Map<Char, Char> = mapOf(
    // a
    'à' to 'a', 'á' to 'a', 'â' to 'a', 'ã' to 'a', 'ä' to 'a', 'å' to 'a',
    'ā' to 'a', 'ă' to 'a', 'ą' to 'a', 'ǎ' to 'a', 'ȧ' to 'a', 'ạ' to 'a',
    'ả' to 'a', 'ấ' to 'a', 'ầ' to 'a', 'ẩ' to 'a', 'ẫ' to 'a', 'ậ' to 'a',

    // ae/ae-like
    'æ' to 'a', 'ǽ' to 'a',

    // c
    'ç' to 'c', 'ć' to 'c', 'ĉ' to 'c', 'č' to 'c', 'ċ' to 'c',

    // d
    'ď' to 'd', 'đ' to 'd',

    // e
    'è' to 'e', 'é' to 'e', 'ê' to 'e', 'ë' to 'e', 'ē' to 'e', 'ĕ' to 'e',
    'ė' to 'e', 'ę' to 'e', 'ě' to 'e', 'ȅ' to 'e', 'ȇ' to 'e', 'ẹ' to 'e',
    'ẻ' to 'e', 'ẽ' to 'e', 'ế' to 'e', 'ề' to 'e', 'ể' to 'e', 'ễ' to 'e',
    'ệ' to 'e',

    // g
    'ğ' to 'g', 'ĝ' to 'g', 'ġ' to 'g', 'ģ' to 'g',

    // h
    'ĥ' to 'h', 'ħ' to 'h',

    // i
    'ì' to 'i', 'í' to 'i', 'î' to 'i', 'ï' to 'i', 'ī' to 'i', 'ĭ' to 'i',
    'į' to 'i', 'ı' to 'i', 'ỉ' to 'i', 'ị' to 'i',

    // j
    'ĵ' to 'j',

    // k
    'ķ' to 'k', 'ĸ' to 'k',

    // l
    'ł' to 'l', 'ľ' to 'l', 'ĺ' to 'l', 'ļ' to 'l', 'ḷ' to 'l',

    // n
    'ñ' to 'n', 'ń' to 'n', 'ň' to 'n', 'ņ' to 'n', 'ṅ' to 'n',

    // o
    'ò' to 'o', 'ó' to 'o', 'ô' to 'o', 'õ' to 'o', 'ö' to 'o', 'ø' to 'o',
    'ō' to 'o', 'ŏ' to 'o', 'ő' to 'o', 'ǒ' to 'o', 'ǫ' to 'o', 'œ' to 'o',

    // r
    'ŕ' to 'r', 'ř' to 'r', 'ŗ' to 'r',

    // s
    'ś' to 's', 'š' to 's', 'ş' to 's', 'ŝ' to 's', 'ș' to 's',

    // t
    'ť' to 't', 'ţ' to 't', 'ŧ' to 't', 'ț' to 't',

    // u
    'ù' to 'u', 'ú' to 'u', 'û' to 'u', 'ü' to 'u', 'ū' to 'u', 'ŭ' to 'u',
    'ů' to 'u', 'ű' to 'u', 'ų' to 'u', 'ǔ' to 'u', 'ụ' to 'u', 'ủ' to 'u',

    // w
    'ŵ' to 'w',

    // y
    'ý' to 'y', 'ÿ' to 'y', 'ŷ' to 'y', 'ȳ' to 'y',

    // z
    'ź' to 'z', 'ž' to 'z', 'ż' to 'z', 'ẑ' to 'z',

    // other Latin additions
    'ß' to 's', 'þ' to 't', 'ƒ' to 'f'
)

// ---------- fuzzy subsequence ----------
/**
 * checks if `query` is a subsequence of `text` (characters in order, but not necessarily contiguous).
 * Example: "hw" is subsequence of "hallen world".
 */
fun isFuzzySubsequence(query: String, text: String): Boolean {
    if (query.isEmpty()) return true
    var qi = 0
    var ti = 0
    while (qi < query.length && ti < text.length) {
        if (query[qi] == text[ti]) qi++
        ti++
    }
    return qi == query.length
}

// ---------- Levenshtein-with-cutoff ----------
/**
 * Levenshtein with early cutoff (only tracks distance up to 'maxDist').
 * Returns true if distance(s, t) <= maxDist.
 */
fun levenshteinWithinThreshold(s: String, t: String, maxDist: Int): Boolean {
    // quick length checks
    if (kotlin.math.abs(s.length - t.length) > maxDist) return false
    // ensure a is shorter
    val (a, b) = if (s.length <= t.length) s to t else t to s
    val n = a.length
    val m = b.length

    var prev = IntArray(m + 1) { it } // distances for row i-1
    var curr = IntArray(m + 1)

    for (i in 1..n) {
        curr[0] = i
        // compute only within window [max(1, i-maxDist) .. min(m, i+maxDist)]
        val from = maxOf(1, i - maxDist)
        val to = min(m, i + maxDist)
        if (from > 1) curr[from - 1] = Int.MAX_VALUE / 2 // sentinel to make transitions safe

        for (j in from..to) {
            val cost = if (a[i - 1] == b[j - 1]) 0 else 1
            val deletion = prev[j] + 1
            val insertion = curr[j - 1] + 1
            val substitution = prev[j - 1] + cost
            curr[j] = min(min(deletion, insertion), substitution)
        }
        // outside of window, set large values
        for (j in 0 until from) curr[j] = Int.MAX_VALUE / 2
        for (j in (to + 1)..m) curr[j] = Int.MAX_VALUE / 2

        // swap rows
        val tmp = prev
        prev = curr
        curr = tmp
    }

    return prev[m] <= maxDist
}
