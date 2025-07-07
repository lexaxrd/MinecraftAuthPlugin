package xyz.themis.serverAuthPlugin.manager

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import xyz.themis.serverAuthPlugin.Main
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.logging.Logger

class ConfigManager(val plugin: Main) {
    val logger: Logger = plugin.logger
    private var config: FileConfiguration
    private val templateFile: File = File(plugin.dataFolder, "email_template.html")

    private val configFile: File
    init {
        if (!plugin.dataFolder.exists()) {
            logger.info("DiscordSync folder not found, creating folder...")
            plugin.dataFolder.mkdirs()
        }

        configFile = File(plugin.dataFolder, "config.yml")
        if (!configFile.exists()) {
            logger.info("config.yml not found, creating file...")
            plugin.saveDefaultConfig()
        }

        config = plugin.config

        if (!templateFile.exists()) {
            plugin.logger.info("email_template.html not found, creating default template...")
            plugin.saveResource("email_template.html", false)
        }
    }
    fun getConfig(): FileConfiguration = config
    fun getTemplate(): String {
        return templateFile.readText(StandardCharsets.UTF_8)
    }
    fun parseTemplate(code: String, username: String, serverName: String): String {
        return getTemplate()
            .replace("{{code}}", code)
            .replace("{{username}}", username)
            .replace("{{server_name}}", serverName)
    }
}