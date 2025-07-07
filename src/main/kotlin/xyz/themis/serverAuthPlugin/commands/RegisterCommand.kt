package xyz.themis.serverAuthPlugin.commands

import com.mongodb.client.model.Filters.eq
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import xyz.themis.serverAuthPlugin.Main
import xyz.themis.serverAuthPlugin.helpers.coloredMessage
import java.security.MessageDigest

class RegisterCommand(private val plugin: Main) : CommandExecutor {
    override fun onCommand(
        p: CommandSender,
        cmd: Command,
        lbl: String,
        args: Array<String>
    ): Boolean {
        if (cmd.name.equals("register", true)) {
            if (p !is Player) {

                p.sendMessage(coloredMessage(plugin.configManager.getConfig().getString("messages.only-players")!!))
                return true
            }

            if (args?.size != 2) {
                p.sendMessage(coloredMessage(plugin.configManager.getConfig().getString("messages.args-missing")!!))
                return true
            }

            plugin.userManager.registerUser(p, args)
            return true
        }
        return false
    }
}