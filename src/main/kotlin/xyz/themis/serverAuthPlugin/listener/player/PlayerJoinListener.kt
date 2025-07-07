package xyz.themis.serverAuthPlugin.listener.player

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import xyz.themis.serverAuthPlugin.Main


class PlayerJoinListener(val plugin: Main) : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        if (plugin.configManager.getConfig().getBoolean("world.teleport-auth-world")) {
            val x = plugin.configManager.getConfig().getDouble("world.auth-world-teleport-location.x")
            val y = plugin.configManager.getConfig().getDouble("world.auth-world-teleport-location.y")
            val z = plugin.configManager.getConfig().getDouble("world.auth-world-teleport-location.z")

            val loc = Location(Bukkit.getWorld(plugin.configManager.getConfig().getString("world.auth-world-name") ?: "auth"), x, y, z)
            player.teleport(loc)

            if (player.world.name != plugin.configManager.getConfig().getString("world.auth-world-name") ?: "auth") return
        }

        if (plugin.configManager.getConfig().getBoolean("world.hide-other-players")) {
            for (other in Bukkit.getOnlinePlayers()) {
                if (other != player) {
                    player.hidePlayer(plugin, other)
                    other.hidePlayer(plugin, player)
                }
            }
        }

        val rawMessage = plugin.configManager.getConfig().getString("messages.required-auth")
        val coloredMessage: String? = ChatColor.translateAlternateColorCodes('&', rawMessage!!)
        player.sendMessage(coloredMessage!!)
    }
}