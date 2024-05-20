package me.archiru.bingusbot

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.Member
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class BingusBot {
    suspend fun startBot(
        token: String,
        serverID: String,
        channelId: String,
        puroPuroSelected: Boolean,
        userId: String,
        magpieSelected: Boolean,
        ninjaSelected: Boolean,
        dragonSelected: Boolean,
        crystalSelected: Boolean,
        luckySelected: Boolean,
        secondsToWait: Int) {
        val kord = Kord(token)

        println("Starting bot...")

        val scheduler = Executors.newScheduledThreadPool(1)
        scheduler.scheduleAtFixedRate(
            SendMessageTask(kord, Snowflake(channelId), Snowflake(serverID), userId, puroPuroSelected, magpieSelected, ninjaSelected, dragonSelected, crystalSelected, luckySelected),
            0, secondsToWait.toLong(), TimeUnit.SECONDS
        )

        kord.login()
    }
}

class SendMessageTask(
    private val client: Kord,
    private val channelId: Snowflake,
    private val guildId: Snowflake,
    private val userId: String,
    private val showPuroPuro: Boolean,
    private val showMagpie: Boolean,
    private val showNinja: Boolean,
    private val showDragon: Boolean,
    private val showCrystal: Boolean,
    private val showLucky: Boolean,
    private var lastItems: List<ImplingData> = emptyList()
) : Runnable {

    private val implingNames = mapOf(
        1642 to "Magpie impling",
        1643 to "Ninja impling",
        8741 to "Crystal impling",
        1644 to "Dragon impling",
        7233 to "Lucky impling"
    )

    override fun run() {
        GlobalScope.launch {
            val url = "https://puos0bfgxc2lno5-implingdb.adb.us-phoenix-1.oraclecloudapps.com/ords/impling/implingdev/dev"
            val jsonData = fetchData(url)

            if (jsonData != null) {
                val gson = Gson()
                val parser = JsonParser()
                val jsonObject = parser.parse(jsonData).asJsonObject
                val itemsArray = jsonObject.getAsJsonArray("items")
                val itemListType = object : TypeToken<List<ImplingData>>() {}.type
                val items: List<ImplingData> = gson.fromJson(itemsArray, itemListType)
                val guild = client.getGuild(guildId)
                val member: Member = guild.getMember(Snowflake(userId))

                val newestItems = items.filter { isNewestItem(it) }

                val messageContent = buildString {
                    newestItems.forEachIndexed { index, data ->
                        val implingName = implingNames[data.npcid] ?: "Unknown impling"
                        if (shouldShow(implingName)) {
                            if (showPuroPuro) {
                                append(implingInfoString(implingName, data, index, member))
                            } else {
                                if (data.xcoord !in 2561..2621 && data.ycoord !in 4290..4349) {
                                    append(implingInfoString(implingName, data, index, member))
                                }
                            }
                        }
                    }
                }

                sendMessageInternal(messageContent)

                lastItems = items
            }
        }
    }

    private fun shouldShow(implingName: String): Boolean {
        return when (implingName) {
            "Magpie impling" -> showMagpie
            "Ninja impling" -> showNinja
            "Dragon impling" -> showDragon
            "Crystal impling" -> showCrystal
            "Lucky impling" -> showLucky
            else -> false
        }
    }

    private fun implingInfoString(implingName: String, data: ImplingData, index: Int, member: Member): String {
        return "${index + 1}. $implingName: world: ${data.world}, Area: https://explv.github.io/?centreX=${data.xcoord}&centreY=${data.ycoord}&centreZ=${data.plane}&zoom=10, discoveredtime: ${data.formattedDiscoveredTime()} ${member.mention}\nXcoord: ${data.xcoord}\nYcoord: ${data.ycoord}\n"
    }

    private fun isNewestItem(item: ImplingData): Boolean {
        return !lastItems.contains(item)
    }

    private fun fetchData(url: String): String? {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        val response = client.newCall(request).execute()
        return response.body?.string()
    }

    data class ImplingData(
        @SerializedName("npcid") val npcid: Int,

        @SerializedName("world") val world: Int,

        @SerializedName("xcoord") val xcoord: Int,

        @SerializedName("ycoord") val ycoord: Int,

        @SerializedName("plane") val plane: Int,

        @SerializedName("discoveredtime") val discoveredtime: Long

    ) {
        fun formattedDiscoveredTime(): String {
            val instant = Instant.ofEpochSecond(discoveredtime)
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())
            return formatter.format(instant)
        }
    }

    private suspend fun sendMessageInternal(content: String) {
        if (content.isNotEmpty()) {
            try {
                val maxMessageLength = 2000
                if (content.length <= maxMessageLength) {
                    client.rest.channel.createMessage(channelId) {
                        this.content = content
                    }
                } else {
                    // don't you know chunk it up, you got to chunk it up
                    val chunks = content.chunked(maxMessageLength)
                    for (chunk in chunks) {
                        client.rest.channel.createMessage(channelId) {
                            this.content = chunk
                        }
                    }
                }
            } catch (e: Exception) {
                println("Failed to send message: ${e.message}")
            }
        }
    }
}

