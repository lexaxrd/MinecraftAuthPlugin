package xyz.themis.serverAuthPlugin.manager

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import org.bson.Document
import xyz.themis.serverAuthPlugin.Main

class DatabaseManager(val plugin: Main) {
    private lateinit var client: MongoClient
    lateinit var usersCollection: MongoCollection<Document>

    init {
        val dbUrl = plugin.configManager.getConfig().getString("mongo.url")
        if (dbUrl == null) {
            throw IllegalStateException("❌ Database URL is required!")
        }
        val dbName = plugin.configManager.getConfig().getString("mongo.db-name")
        if (dbName == null) {
            throw IllegalStateException("❌ Database name is required!")
        }
        val collectionName = plugin.configManager.getConfig().getString("mongo.collection-name")
        if (collectionName == null) {
            throw IllegalStateException("❌ Collection name is required!")
        }


        client = MongoClients.create(dbUrl)
        val database = client.getDatabase(dbName)
        usersCollection = database.getCollection(collectionName)
        println("✅ MongoDB connected to database '$dbName'")
    }
}