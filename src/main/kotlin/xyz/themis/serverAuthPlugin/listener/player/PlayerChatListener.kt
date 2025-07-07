package xyz.themis.serverAuthPlugin.listener.player

import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import xyz.themis.serverAuthPlugin.Main

class PlayerChatListener(val plugin: Main) : Listener {
    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        val player = event.player

        if (player.world.name.equals(plugin.configManager.getConfig().getString("world.auth-world-name")!!, true)) {
            event.isCancelled = true

            val message = plugin.configManager.getConfig().getString("messages.cant-send-message")
            val coloredMessage = ChatColor.translateAlternateColorCodes('&', message!!)
            player.sendMessage(coloredMessage!!);

        }
    }
}