// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.spotless)
}

apply(from = "spotless.gradle")

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
}

detekt {
    toolVersion = libs.versions.detekt.get()
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
}

val sortDependencies by tasks.registering {
    group = "Verification"
    description = "Checks and sorts dependencies and plugins in build.gradle.kts files with spacing between groups."

    val buildFilesFromConfig = project.allprojects.map { it.file("build.gradle.kts") }.filter { it.exists() }
    inputs.files(buildFilesFromConfig)

    doLast {
        inputs.files.forEach { file ->
            val lines = file.readLines()
            val newLines = mutableListOf<String>()
            var i = 0
            while (i < lines.size) {
                val line = lines[i]
                if (line.trim().startsWith("plugins {") || line.trim().startsWith("dependencies {")) {
                    val isDependencies = line.trim().startsWith("dependencies {")
                    newLines.add(line)
                    val blockLines = mutableListOf<String>()
                    i++
                    var openBraces = 1
                    while (i < lines.size && openBraces > 0) {
                        val currentLine = lines[i]
                        openBraces += currentLine.count { it == '{' }
                        openBraces -= currentLine.count { it == '}' }
                        if (openBraces > 0) {
                            blockLines.add(currentLine)
                            i++
                        }
                    }

                    if (isDependencies) {
                        val groups = mutableMapOf<String, MutableList<String>>()
                        val other = mutableListOf<String>()

                        var currentComments = mutableListOf<String>()

                        blockLines.forEach { bl ->
                            val trimmed = bl.trim()
                            if (trimmed.isEmpty()) return@forEach

                            if (trimmed.startsWith("//")) {
                                currentComments.add(bl)
                            } else {
                                val match = Regex("^([a-zA-Z]+)\\(.*\\)$").find(trimmed)
                                val groupName = match?.groupValues?.get(1) ?: "other"

                                val entry = (currentComments + bl).joinToString("\n")
                                if (groupName != "other") {
                                    groups.getOrPut(groupName) { mutableListOf() }.add(entry)
                                } else {
                                    other.add(entry)
                                }
                                currentComments = mutableListOf()
                            }
                        }

                        val sortedGroupNames = groups.keys.sorted()
                        sortedGroupNames.forEachIndexed { index, name ->
                            val sortedEntries = groups[name]!!.sortedBy { it.trim().lowercase() }
                            newLines.addAll(sortedEntries)
                            if (index < sortedGroupNames.size - 1 || other.isNotEmpty()) {
                                if (newLines.last().isNotBlank()) {
                                    newLines.add("")
                                }
                            }
                        }
                        if (other.isNotEmpty()) {
                            newLines.addAll(other.sortedBy { it.trim().lowercase() })
                        }
                    } else {
                        // For plugins, just sort alphabetically but keep comments
                        val entries = mutableListOf<String>()
                        var currentComments = mutableListOf<String>()
                        blockLines.forEach { bl ->
                            val trimmed = bl.trim()
                            if (trimmed.isEmpty()) return@forEach
                            if (trimmed.startsWith("//")) {
                                currentComments.add(bl)
                            } else {
                                entries.add((currentComments + bl).joinToString("\n"))
                                currentComments = mutableListOf()
                            }
                        }
                        newLines.addAll(entries.sortedBy { it.trim().lowercase() })
                    }

                    if (i < lines.size) newLines.add(lines[i])
                } else {
                    newLines.add(line)
                }
                i++
            }
            file.writeText(newLines.joinToString("\n") + "\n")
        }
    }
}
