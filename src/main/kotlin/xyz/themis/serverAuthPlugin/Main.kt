package xyz.themis.serverAuthPlugin

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.plugin.java.JavaPlugin
import xyz.themis.serverAuthPlugin.commands.LoginCommand
import xyz.themis.serverAuthPlugin.commands.RegisterCommand
import xyz.themis.serverAuthPlugin.commands.VerifyCommand
import xyz.themis.serverAuthPlugin.listener.player.PlayerChatListener
import xyz.themis.serverAuthPlugin.listener.player.PlayerJoinListener
import xyz.themis.serverAuthPlugin.listener.world.AuthWorldBlockListener
import xyz.themis.serverAuthPlugin.manager.ConfigManager
import xyz.themis.serverAuthPlugin.manager.DatabaseManager
import xyz.themis.serverAuthPlugin.manager.UserManager

class Main : JavaPlugin() {
    lateinit var configManager: ConfigManager
    lateinit var databaseManager: DatabaseManager
    lateinit var userManager: UserManager
    override fun onEnable() {
        configManager = ConfigManager(this)
        databaseManager = DatabaseManager(this)
        userManager = UserManager(this, databaseManager.usersCollection)

        getCommand("register")?.setExecutor(RegisterCommand(this))
        getCommand("verify-email")?.setExecutor(VerifyCommand(this))
        getCommand("login")?.setExecutor(LoginCommand(this))
        server.pluginManager.registerEvents(PlayerChatListener(this), this)
        server.pluginManager.registerEvents(PlayerJoinListener(this), this)

        server.scheduler.runTaskTimerAsynchronously(this, Runnable {
            userManager.cleanExpired()
        }, 0L, 1200L)

        logger.info("Server Auth Plugin enabled")
    }

    override fun onDisable() {
        logger.info("Server Auth Plugin disabled")
    }
}
