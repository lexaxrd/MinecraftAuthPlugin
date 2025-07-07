package xyz.themis.serverAuthPlugin.commands

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import xyz.themis.serverAuthPlugin.Main
import xyz.themis.serverAuthPlugin.helpers.coloredMessage

class LoginCommand(val plugin: Main) : CommandExecutor {
    override fun onCommand(p: CommandSender, cmd: Command, lbl: String, args: Array<out String>): Boolean {
        if (cmd.name.equals("login", false)) {
            if (p !is Player) {
                p.sendMessage(coloredMessage(plugin.configManager.getConfig().getString("messages.only-players")!!))
                return true
            }

            if (args.size != 1) {
                p.sendMessage(coloredMessage(plugin.configManager.getConfig().getString("messages.password-missing")!!))
                return true
            }

            val password = args[0]
            val success = plugin.userManager.loginUser(p, password)
            if (success) {
                val countdownSeconds = 3

                for (i in countdownSeconds downTo 1) {
                    Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                        p.sendMessage("$i...")
                    }, ((countdownSeconds - i) * 20L))
                }

                Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                    val x = plugin.configManager.getConfig().getDouble("world.lobby-world-teleport-location.x")
                    val y = plugin.configManager.getConfig().getDouble("world.lobby-world-teleport-location.y")
                    val z = plugin.configManager.getConfig().getDouble("world.lobby-world-teleport-location.z")
                    
                    val loc = Location(Bukkit.getWorld(plugin.configManager.getConfig().getString("world.lobby-world-name") ?: "world"), x, y, z)
                    p.teleport(loc)
                }, countdownSeconds * 20L)

            }
            return true

        }
        return false
    }
}