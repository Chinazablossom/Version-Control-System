package svcs

import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

val dir = File("vcs")
val configFile = File("vcs/config.txt")
val add = File("vcs/index.txt")
val log = File("vcs/log.txt.")
val commit = File("vcs/commits")

fun main(args: Array<String>) {
    if (!dir.exists()) dir.mkdir()
    if (!configFile.exists()) configFile.createNewFile()
    if (!add.exists()) add.createNewFile()
    if (!log.exists()) log.createNewFile()
    if (!commit.exists()) commit.mkdir()
    val input = args.getOrNull(1) ?: ""
    when (args.getOrNull(0) ?: "") {
        "config" -> config(input)
        "add" -> add(args)
        "log" -> log()
        "commit" -> commit(args)
        "checkout" -> checkout(input)
        else -> helpPage(args.getOrNull(0) ?: "")

    }

}


fun helpPage(command: String) {
    fun c() = mapOf(
        "config" to "Get and set a username.",
        "add" to "Add a file to the index.",
        "log" to "Show commit logs.",
        "commit" to "Save changes.",
        "checkout" to "Restore a file."
    )
    if (command.isEmpty() || command == "--help") {
        println("These are SVCS commands:")
        for ((key, value) in c()) {
            println("$key${" ".repeat(11 - command.length)}$value")
        }
    } else {
        println(c().getOrDefault(command, "'$command' is not a SVCS command."))
    }
}

fun config(userName: String) {
    val name = if (userName.isNotEmpty()) {
        configFile.writeText(userName)
        userName
    } else {
        if (configFile.isFile) configFile.readText() else ""
    }
    println(if (name.isEmpty()) "Please, tell me who you are." else "The username is $name.")
}

fun add(args: Array<String>) {
    if (args.size == 1 && add.length() == 0L) {
        println("Add a file to the index.")
    } else if (args.size == 1) {
        println("Tracked files:")
        print(add.readText())
    }
    if (args.size == 2) {
        if (File(args[1]).isFile) {
            if (add.isFile && add.readLines().contains(args[1])) {
                println("${args[1]} already exists.")
            } else {
                add.appendText("${args[1]}\n")
                println("The file '${args[1]}' is tracked.")
            }
        } else println("Can't find '${args[1]}'.")
    }

}

fun log() {
    if (log.length() == 0L)
        println("No commits yet.")
    println(log.readText())

}

fun commit(args: Array<String>) {
    fun getHash(input: String): String {
        val sha1 = MessageDigest.getInstance("SHA-1")
        return BigInteger(1, sha1.digest(input.toByteArray())).toString(16)
    }

    var indexedFiles = ""
    if (args.size > 1 && add.length() > 0) {
        add.readLines()
            .filter { File(it).exists() }
            .forEach { indexedFiles += File(it).readText() }
        val hash = getHash(indexedFiles)
        val newDir = "${commit.path}/$hash"
        if (File(newDir).exists()) {
            println("Nothing to commit.")
            return
        } else File(newDir).mkdir()
        if (File(newDir).exists()) {
            for (line in add.readLines()) {
                val fileIn = File("${File("./").path}/$line")
                val fileOut = File("$newDir/$line")
                if (fileOut.exists()) {
                    fileOut.delete()
                }
                if (fileIn.exists()) {
                    fileIn.copyTo(fileOut)
                }
            }
            var newlogString = """
                    commit $hash
                    Author: ${configFile.readText()}
                    ${args[1]}
           
                """.trimIndent()
            newlogString += log.readText()
            log.writeText(newlogString)
            println("Changes are committed.")
        }
    } else if (args.size > 1 && add.length() == 0L) {
        println("Nothing to commit.")
    } else {
        println("Message was not passed.")
    }

}

fun checkout(commitId: String) {
    if (commitId.isEmpty()) {
        println("Commit id was not passed.")
    } else {
        val dir = File("$commit/$commitId")
        if (!dir.exists()) println("Commit does not exist.")
        else {
            dir.copyRecursively(File("./"), true)
            println("Switched to commit $commitId.")
        }
    }
}