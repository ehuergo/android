/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.nextcloud.utils.autoRename

import com.nextcloud.utils.extensions.StringConstants
import com.nextcloud.utils.extensions.forbiddenFilenameCharacters
import com.nextcloud.utils.extensions.forbiddenFilenameExtension
import com.nextcloud.utils.extensions.shouldRemoveNonPrintableUnicodeCharacters
import com.owncloud.android.datamodel.OCFile
import com.owncloud.android.lib.resources.status.OCCapability
import java.util.regex.Pattern

object AutoRename {
    private const val REPLACEMENT = "_"

    fun rename(filename: String, capability: OCCapability, isFolderPath: Boolean = false): String {
        var result = filename

        capability.run {
            forbiddenFilenameCharactersJson?.let {
                var forbiddenFilenameCharacters = capability.forbiddenFilenameCharacters()
                if (isFolderPath) {
                    forbiddenFilenameCharacters = forbiddenFilenameCharacters.minus(OCFile.PATH_SEPARATOR)
                }

                forbiddenFilenameCharacters.forEach {
                    if (result.lowercase().contains(it)) {
                        result = result.replace(it, REPLACEMENT)
                    }
                }
            }

            forbiddenFilenameExtensionJson?.let {
                forbiddenFilenameExtension().any { forbiddenExtension ->
                    if (forbiddenExtension == StringConstants.SPACE) {
                        result = result.trimStart().trimEnd()
                    }

                    if (result.endsWith(forbiddenExtension, ignoreCase = true) ||
                        result.startsWith(forbiddenExtension, ignoreCase = true)
                    ) {
                        result = result.replace(forbiddenExtension, REPLACEMENT)
                    }

                    false
                }
            }
        }

        return if (capability.shouldRemoveNonPrintableUnicodeCharacters()) {
            val nonPrintableUnicodeVersion = removeNonPrintableUnicodeCharacters(result)
            convertToUTF8(nonPrintableUnicodeVersion)
        } else {
            result
        }
    }

    private fun convertToUTF8(filename: String): String {
        return String(filename.toByteArray(), Charsets.UTF_8)
    }

    private fun removeNonPrintableUnicodeCharacters(filename: String): String {
        val regex = "\\p{C}"
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(filename)
        return matcher.replaceAll("")
    }
}
