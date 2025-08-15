// .github/scripts/assign_reviewer.main.kts

// Required Dependencies:
// 1. github-api-1.321.jar: https://repo1.maven.org/maven2/org/kohsuke/github-api/1.321/github-api-1.321.jar
// 2. jackson-databind-2.17.0.jar: https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.17.0/jackson-databind-2.17.0.jar
// 3. jackson-core-2.17.0.jar: https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.17.0/jackson-core-2.17.0.jar
// 4. jackson-annotations-2.17.0.jar: https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.17.0/jackson-annotations-2.17.0.jar
//
// Place these .jar files in a new directory: .github/scripts/libs/

import org.kohsuke.github.GitHub
import java.io.File
import java.util.concurrent.TimeUnit

fun getLastModifier(filePath: String, prAuthor: String): String? {
    try {
        val process = ProcessBuilder("git", "log", "--pretty=format:%ae", filePath)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        process.waitFor(10, TimeUnit.SECONDS)
        val output = process.inputStream.bufferedReader().readText().lines()

        for (email in output) {
            if (email.isBlank()) continue
            val username = getGithubUsernameFromEmail(email)
            if (username.lowercase() != prAuthor.lowercase()) {
                return username
            }
        }
    } catch (e: Exception) {
        println("Error checking git log for $filePath: ${e.message}")
    }
    return null
}

fun getGithubUsernameFromEmail(email: String): String {
    // This is a simplification. A more robust solution might query the GitHub API.
    return email.split('@')[0]
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
        val reviewersToAdd = mutableSetOf<String>()

        for (file in changedFiles) {
            println("Checking file: ${file.filename}")
            val lastModifier = getLastModifier(file.filename, prAuthor)
            if (lastModifier != null) {
                println("Found last modifier for ${file.filename}: $lastModifier")
                try {
                    // Check if the user is a collaborator before adding
                    if (repo.isCollaborator(lastModifier)) {
                        reviewersToAdd.add(lastModifier)
                    } else {
                        println("User $lastModifier is not a collaborator. Skipping.")
                    }
                } catch (e: Exception) {
                     println("Could not verify if $lastModifier is a collaborator. Adding anyway. Error: ${e.message}")
                     reviewersToAdd.add(lastModifier)
                }
            }
        }

        // --- Assign reviewers ---
        if (reviewersToAdd.isNotEmpty()) {
            println("Requesting review from: $reviewersToAdd")
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
