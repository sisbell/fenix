/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.ext

import android.content.Context
import androidx.core.net.toUri
import kotlinx.coroutines.runBlocking
import mozilla.components.lib.publicsuffixlist.PublicSuffixList
import mozilla.components.support.ktx.android.net.hostWithoutCommonPrefixes
import java.net.MalformedURLException
import java.net.URL

/**
 * Replaces the keys with the values with the map provided.
 */
fun String.replace(pairs: Map<String, String>): String {
    var result = this
    pairs.forEach { (l, r) -> result = result.replace(l, r) }
    return result
}

/**
 * Tries to parse and get host part if this [String] is valid URL.
 * Otherwise returns the string.
 */
fun String.tryGetHostFromUrl(): String = try {
    URL(this).host
} catch (e: MalformedURLException) {
    this
}

/**
 * Trim a host's prefix and suffix
 */
fun String.urlToTrimmedHost(context: Context): String =
    this.urlToTrimmedHost(context.components.publicSuffixList)

/**
 * Trim a host's prefix and suffix
 */
fun String.urlToTrimmedHost(publicSuffixList: PublicSuffixList): String {
    return try {
        val host = toUri().hostWithoutCommonPrefixes ?: return this
        runBlocking {
            publicSuffixList.stripPublicSuffix(host).await()
        }
    } catch (e: MalformedURLException) {
        this
    }
}

/**
 * Trims a URL string of its scheme and common prefixes.
 *
 * This is intended to act much like [PublicSuffixList.getPublicSuffixPlusOne()] but unlike
 * that method, leaves the path, anchor, etc intact.
 *
 */
fun String.simplifiedUrl(): String {
    val afterScheme = this.substringAfter("://")
    for (prefix in listOf("www.", "m.", "mobile.")) {
        if (afterScheme.startsWith(prefix)) {
            return afterScheme.substring(prefix.length)
        }
    }
    return afterScheme
}
