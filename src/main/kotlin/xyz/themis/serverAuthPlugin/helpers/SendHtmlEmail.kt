package xyz.themis.serverAuthPlugin.helpers

import com.google.common.util.concurrent.ExecutionError
import com.sun.org.apache.xpath.internal.operations.Bool
import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import org.bukkit.entity.Player
import xyz.themis.serverAuthPlugin.Main
import java.util.Properties

fun sendHtmlEmail(plugin: Main, to: String, htmlContent: String): Boolean {
    val username = plugin.configManager.getConfig().getString("email.your-email")
    val appPassword = plugin.configManager.getConfig().getString("email.app-password")
    if (username == null || appPassword == null) {
        plugin.logger.warning(plugin.configManager.getConfig().getString("email.messages.email-app-password-missing"))
        return false;
    }

    try {
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication? {
                return PasswordAuthentication(username, appPassword)
            }
        })

        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(username))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
            subject = plugin.configManager.getConfig().getString("email.subject")

            setContent(htmlContent, "text/html; charset=utf-8")
        }
        Transport.send(message)
        plugin.logger.warning(plugin.configManager.getConfig().getString("email.messages.email-sent"))
        return true
    }
    catch (e: Exception) {
        plugin.logger.warning(plugin.configManager.getConfig().getString("email.messages.error-occurred")!!.replace("%error%", e.message!!))
        return false
    }

    return true

}