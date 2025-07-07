package xyz.themis.serverAuthPlugin.listener.world

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import xyz.themis.serverAuthPlugin.Main

class AuthWorldBlockListener(val plugin: Main) : Listener {
    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val world = event.block.world.name
        if (world.equals(plugin.configManager.getConfig().getString("world.auth-world-name")!!, ignoreCase = true)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        val world = event.block.world.name
        if (world.equals(plugin.configManager.getConfig().getString("world.auth-world-name")!!, ignoreCase = true)) {
            event.isCancelled = true
        }
    }
}