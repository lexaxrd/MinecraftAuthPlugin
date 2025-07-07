package xyz.themis.serverAuthPlugin.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import xyz.themis.serverAuthPlugin.Main
import xyz.themis.serverAuthPlugin.helpers.coloredMessage

class VerifyCommand(val plugin: Main) : CommandExecutor {
    override fun onCommand(p: CommandSender, cmd: Command, lbl: String, args: Array<out String>): Boolean {
        if (cmd.name.equals("verify-email", false)) {
            if (p !is Player) {
                p.sendMessage(coloredMessage(plugin.configManager.getConfig().getString("messages.only-players")!!))
                return true
            }

            if (args.size != 1) {
                p.sendMessage(coloredMessage(plugin.configManager.getConfig().getString("messages.code-missing")!!))
                return true
            }
            val code = args[0]

            plugin.userManager.verifyEmail(p, code)
        }
        return true
    }
}