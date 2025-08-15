// .github/scripts/assign_reviewer.main.kts

// Required Dependencies:
// 1. github-api-1.321.jar: https://repo1.maven.org/maven2/org/kohsuke/github-api/1.321/github-api-1.321.jar
// 2. jackson-databind-2.17.0.jar: https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.17.0/jackson-databind-2.17.0.jar
// 3. jackson-core-2.17.0.jar: https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.17.0/jackson-core-2.17.0.jar
// 4. jackson-annotations-2.17.0.jar: https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.17.0/jackson-annotations-2.17.0.jar
// 5. commons-lang3-3.12.0.jar: https://repo1.maven.org/maven2/org/apache/commons/commons-lang3/3.12.0/commons-lang3-3.12.0.jar
// 6. commons-io-2.11.0.jar: https://repo1.maven.org/maven2/commons-io/commons-io/2.11.0/commons-io-2.11.0.jar
//
// Place these .jar files in a new directory: .github/scripts/libs/

import org.kohsuke.github.GitHub
import org.kohsuke.github.GHUser
import org.kohsuke.github.GHRepository
import java.io.File
import java.util.concurrent.TimeUnit

fun findValidReviewerForFile(filePath: String, prAuthor: String, github: GitHub, repo: GHRepository): GHUser? {
    try {
        // Use %an to get the author's name directly
        val process = ProcessBuilder("git", "log", "--pretty=format:%an", filePath)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        process.waitFor(10, TimeUnit.SECONDS)
        val output = process.inputStream.bufferedReader().readText().lines()

        // Iterate through the history to find the first valid collaborator
        for (authorName in output) {
            println("${output}")

            if (authorName.isBlank() || authorName.lowercase() == prAuthor.lowercase()) {
                continue // Skip blank lines or the PR author
            }

            try {
                println("  - Checking potential reviewer: $authorName")
                val user = github.getUser(authorName)
                if (repo.isCollaborator(user)) {
                    println("    -> Found valid collaborator: ${user.login}")
                    return user // Found a valid reviewer, return them
                } else {
                    println("    -> Not a collaborator.")
                }
            } catch (e: org.kohsuke.github.GHFileNotFoundException) {
                println("    -> Could not find GitHub user '$authorName'.")
                continue // User not found, try the next one
            } catch (e: Exception) {
                println("    -> Error checking collaborator status for $authorName: ${e.message}")
                continue // Some other error, try the next one
            }
        }
    } catch (e: Exception) {
        println("Error checking git log for $filePath: ${e.message}")
    }
    
    // If the loop finishes, no valid reviewer was found in the file's history
    println("  - No valid past collaborator found for this file.")
    return null
}

fun main() {
    try {
        // --- Get environment variables ---
        val token = System.getenv("GITHUB_TOKEN")
        val prNumber = System.getenv("PR_NUMBER").toInt()
        val repoName = System.getenv("GITHUB_REPOSITORY")

        // --- Initialize GitHub client ---
        val github = GitHub.connectUsingOAuth(token)
        val repo = github.getRepository(repoName)
        val pr = repo.getPullRequest(prNumber)
        val prAuthor = pr.user.login

        println("Processing PR #$prNumber by $prAuthor in repo $repoName")

        // --- Get changed files and find reviewers ---
        val changedFiles = pr.listFiles()
        val reviewersToAdd = mutableSetOf<GHUser>()

        for (file in changedFiles) {
            println("Checking file: ${file.filename}")
            val validReviewer = findValidReviewerForFile(file.filename, prAuthor, github, repo)
            if (validReviewer != null) {
                reviewersToAdd.add(validReviewer)
            }
        }

        // --- Assign reviewers ---
        if (reviewersToAdd.isNotEmpty()) {
            val reviewerLogins = reviewersToAdd.map { it.login }
            println("Requesting review from: $reviewerLogins")
            pr.requestReviewers(reviewersToAdd.toList())
        } else {
            println("No suitable reviewers found.")
        }

    } catch (e: Exception) {
        println("An error occurred in the main process: ${e.message}")
        e.printStackTrace()
        kotlin.system.exitProcess(1)
    }
}

main()
