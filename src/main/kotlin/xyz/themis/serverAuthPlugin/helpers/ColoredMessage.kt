package xyz.themis.serverAuthPlugin.helpers

import org.bukkit.ChatColor

fun coloredMessage(message: String): String {
    return ChatColor.translateAlternateColorCodes('&', message)
}