package xyz.themis.serverAuthPlugin.manager

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates.combine
import com.mongodb.client.model.Updates.set
import com.sun.org.apache.xpath.internal.operations.Bool
import org.bson.Document
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import xyz.themis.serverAuthPlugin.Main
import xyz.themis.serverAuthPlugin.helpers.coloredMessage
import xyz.themis.serverAuthPlugin.helpers.sendHtmlEmail
import java.lang.Exception
import java.security.MessageDigest
import java.util.Date

class UserManager(val plugin: Main, val usersCollection: MongoCollection<Document>) {
    fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private var codes = mutableMapOf<String, CodeEntry>()
    class CodeEntry(val code: String, val createdAt: Long = System.currentTimeMillis()) {
        val timeout = 10 * 60 * 1000
        fun isExpired(): Boolean {
            return System.currentTimeMillis() - createdAt > timeout
        }
    }
    fun cleanExpired() {
        codes.entries.removeIf { it.value.isExpired() }
    }
    fun generateVerificationCode(email: String): CodeEntry? {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val code = (1..6) .map { chars.random() } .joinToString("")
        codes[email] = CodeEntry(code)
        return codes[email]
    }

    fun sendVerificationCodeToUser(player: Player, email: String) {
        val user = usersCollection.find(eq("uuid", player.uniqueId.toString())).firstOrNull()
        if (user == null) {
            player.sendMessage(coloredMessage(plugin.configManager.getConfig().getString("messages.no-account")!!))
            return
        }
        
        val verificationCode = generateVerificationCode(email)?.code

        val htmlContent = plugin.configManager.parseTemplate(verificationCode!!, player.name, plugin.configManager.getConfig().getString("email.server-name")!!)

        val success = sendHtmlEmail(plugin, email, htmlContent)
        if (success) {
            usersCollection.updateOne(
                eq("email", email),
                combine(
                    set("verification_code", verificationCode),
                    set("verified", false)
                )
            )
        }
    }

    fun loginUser(player: Player, password: String): Boolean {
        val uuid = player.uniqueId
        val user = usersCollection.find(Document("uuid", uuid.toString())).firstOrNull()

        if (user == null) {
            player.sendMessage(coloredMessage(plugin.configManager.getConfig().getString("messages.no-account")!!))
            return false
        }

        val userPassword = user.getString("password")
        val hashedPassword = hashPassword(password)

        if (!hashedPassword.equals(userPassword, ignoreCase = false)) {
            player.sendMessage(coloredMessage(plugin.configManager.getConfig().getString("messages.password-wrong")!!))
            return false
        }

        if (plugin.configManager.getConfig().getBoolean("settings.disallow-unverified-accounts") ?: true) {
            val userVerifyStatus = user.getBoolean("verified")
            if (!userVerifyStatus) {
                val userEmail = user.getString("email")

                player.sendMessage(coloredMessage(plugin.configManager.getConfig().getString("messages.verification-required")!!))
                sendVerificationCodeToUser(player, userEmail)

                return false
            }
        }

        player.sendMessage(coloredMessage(plugin.configManager.getConfig().getString("messages.login-successful")!!))
        return true
    }

    fun registerUser(player: Player, args: Array<out String>) {
        val email = args[0]
        val password = args[1]
        val uuid = player.uniqueId

        plugin.logger.info("$email $password $uuid")

        try {
            val existingUserByUUID = plugin.databaseManager.usersCollection.find(eq("uuid", uuid.toString())).first()
            if (existingUserByUUID != null) {
                val existingUser = plugin.configManager.getConfig().getString("messages.already-registered")!!
                val coloredExistingMessage = ChatColor.translateAlternateColorCodes('&', existingUser)
                player.sendMessage(coloredExistingMessage)
                return
            }

            val existingUserByEmail = plugin.databaseManager.usersCollection.find(eq("email", email)).first()
            if (existingUserByEmail != null) {
                val existingUser = plugin.configManager.getConfig().getString("messages.email-already-registered")!!
                val coloredExistingMessage = ChatColor.translateAlternateColorCodes('&', existingUser)
                player.sendMessage(coloredExistingMessage)
                return
            }

            val hashedPassword = hashPassword(password)

            val userDoc = Document()
                .append("uuid", uuid.toString())
                .append("username", player.name)
                .append("email", email)
                .append("password", hashedPassword)
                .append("verified", false)
                .append("verification_code", "")
                .append("created_at", Date())


            usersCollection.insertOne(userDoc)
            val successMessage = plugin.configManager.getConfig().getString("messages.registration-successful")!!
            val coloredSuccessMessage = ChatColor.translateAlternateColorCodes('&', successMessage)
            player.sendMessage(coloredSuccessMessage)

            sendVerificationCodeToUser(player, email)
        }
        catch (e: Exception) {
            plugin.logger.warning("An error occurred while registering the user: ${e.message}")
        }
    }
    fun verifyEmail(player: Player, code: String) {
        val uuid = player.uniqueId

        val user = usersCollection.find(Document("uuid", uuid.toString())).firstOrNull()
        if (user == null) {
            val message =  plugin.configManager.getConfig().getString("messages.no-account")!!
            val coloredMessage = ChatColor.translateAlternateColorCodes('&', message)
            player.sendMessage(coloredMessage)
            return
        }

        if (user.getBoolean("verified") == true) {
            player.sendMessage(coloredMessage(plugin.configManager.getConfig().getString("messages.already-verified")!!))
            return
        }

        val userEmail = user.getString("email")

        val entry = codes[userEmail] ?: null
        if (entry == null) {
            val message =  plugin.configManager.getConfig().getString("messages.code-not-found")!!
            val coloredMessage = ChatColor.translateAlternateColorCodes('&', message)
            player.sendMessage(coloredMessage)
            sendVerificationCodeToUser(player, userEmail)

            return
        }
        if (entry.isExpired()) {
            val message =  plugin.configManager.getConfig().getString("messages.code-expired")!!
            val coloredMessage = ChatColor.translateAlternateColorCodes('&', message)
            player.sendMessage(coloredMessage)
            codes.remove(userEmail)
            sendVerificationCodeToUser(player, userEmail)
            return
        }
        val isValid = entry.code == code
        if (isValid) {
            codes.remove(userEmail)
            try {
                usersCollection.updateOne(eq("email", userEmail),
                    combine(
                        set("verified", true),
                        set("verification_code", "")
                    )
                )

                val message =  plugin.configManager.getConfig().getString("messages.verification-successful")!!
                val coloredMessage = ChatColor.translateAlternateColorCodes('&', message)
                player.sendMessage(coloredMessage)
            }
            catch (e: kotlin.Exception) {
                val message =  plugin.configManager.getConfig().getString("messages.verification-failed")!!.replace("%error%", e.message!!)
                val coloredMessage = ChatColor.translateAlternateColorCodes('&', message)
                player.sendMessage(coloredMessage)
            }

        }
        else {
            val message =  plugin.configManager.getConfig().getString("messages.code-valid")!!
            val coloredMessage = ChatColor.translateAlternateColorCodes('&', message)
            player.sendMessage(coloredMessage)
        }
    }
}