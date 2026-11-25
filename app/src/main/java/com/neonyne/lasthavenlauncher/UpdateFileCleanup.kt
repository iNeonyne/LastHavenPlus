package com.neonyne.lasthavenlauncher

import java.io.File

/** Removes crash-abandoned download/patch files; verified APKs are preserved. */
internal object UpdateFileCleanup {
    fun removeAbandonedPartialFiles(directory: File) {
        directory.listFiles()?.forEach { file ->
            if (file.isFile && file.name.endsWith(".part")) {
                file.delete()
            }
        }
    }
}
