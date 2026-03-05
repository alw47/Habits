package com.liwo.habits.data.repo

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.room.withTransaction
import com.liwo.habits.data.db.AppDatabase
import com.liwo.habits.data.model.Habit
import com.liwo.habits.data.model.HabitLog
import com.liwo.habits.data.model.HabitStatus
import com.liwo.habits.data.model.Redemption
import com.liwo.habits.data.model.Reward
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase
) {

    suspend fun exportToDownloads(): String {
        val json = buildJson()
        val fileName = "habits_backup_${LocalDate.now()}.json"

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/json")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: error("Could not create file in Downloads")
            resolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                ?: error("Could not open output stream")
            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            "Downloads/$fileName"
        } else {
            @Suppress("DEPRECATION")
            val dir = android.os.Environment
                .getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            dir.mkdirs()
            java.io.File(dir, fileName).writeText(json)
            "Downloads/$fileName"
        }
    }

    suspend fun clearAll() {
        db.withTransaction {
            db.habitLogDao().deleteAll()
            db.redemptionDao().deleteAll()
            db.habitDao().deleteAll()
            db.rewardDao().deleteAll()
        }
    }

    suspend fun importFromUri(uri: Uri) {
        val json = context.contentResolver.openInputStream(uri)?.use {
            it.readBytes().toString(Charsets.UTF_8)
        } ?: error("Could not read file")

        val root = JSONObject(json)
        val habits = parseHabits(root.getJSONArray("habits"))
        val logs = parseLogs(root.getJSONArray("habitLogs"))
        val rewards = parseRewards(root.getJSONArray("rewards"))
        val redemptions = parseRedemptions(root.getJSONArray("redemptions"))

        db.withTransaction {
            db.habitLogDao().deleteAll()
            db.redemptionDao().deleteAll()
            db.habitDao().deleteAll()
            db.rewardDao().deleteAll()
            db.habitDao().insertAll(habits)
            db.rewardDao().insertAll(rewards)
            db.habitLogDao().insertAll(logs)
            db.redemptionDao().insertAll(redemptions)
        }
    }

    private suspend fun buildJson(): String {
        val habits = db.habitDao().getAllHabits()
        val logs = db.habitLogDao().getAllLogs()
        val rewards = db.rewardDao().getAllRewards()
        val redemptions = db.redemptionDao().getAllRedemptions()

        return JSONObject().apply {
            put("version", 1)
            put("exportedAt", LocalDate.now().toString())
            put("habits", JSONArray().also { arr ->
                habits.forEach { h ->
                    arr.put(JSONObject().apply {
                        put("id", h.id)
                        put("name", h.name)
                        put("pointsDone", h.pointsDone)
                        put("pointsMissed", h.pointsMissed)
                        put("isActive", h.isActive)
                        put("sortOrder", h.sortOrder)
                        put("daysMask", h.daysMask)
                    })
                }
            })
            put("habitLogs", JSONArray().also { arr ->
                logs.forEach { l ->
                    arr.put(JSONObject().apply {
                        put("habitId", l.habitId)
                        put("date", l.date)
                        put("status", l.status.value)
                    })
                }
            })
            put("rewards", JSONArray().also { arr ->
                rewards.forEach { r ->
                    arr.put(JSONObject().apply {
                        put("id", r.id)
                        put("name", r.name)
                        put("cost", r.cost)
                        put("description", r.description ?: "")
                        put("isActive", r.isActive)
                        put("isRecurring", r.isRecurring)
                        put("sortOrder", r.sortOrder)
                    })
                }
            })
            put("redemptions", JSONArray().also { arr ->
                redemptions.forEach { rd ->
                    arr.put(JSONObject().apply {
                        put("id", rd.id)
                        put("rewardId", rd.rewardId)
                        put("rewardName", rd.rewardName)
                        put("cost", rd.cost)
                        put("createdAtMillis", rd.createdAtMillis)
                    })
                }
            })
        }.toString(2)
    }

    private fun parseHabits(arr: JSONArray): List<Habit> = (0 until arr.length()).map {
        val o = arr.getJSONObject(it)
        Habit(
            id = o.getLong("id"),
            name = o.getString("name"),
            pointsDone = o.getInt("pointsDone"),
            pointsMissed = o.getInt("pointsMissed"),
            isActive = o.getBoolean("isActive"),
            sortOrder = o.getInt("sortOrder"),
            daysMask = o.getInt("daysMask")
        )
    }

    private fun parseLogs(arr: JSONArray): List<HabitLog> = (0 until arr.length()).map {
        val o = arr.getJSONObject(it)
        HabitLog(
            habitId = o.getLong("habitId"),
            date = o.getString("date"),
            status = HabitStatus.fromInt(o.getInt("status"))
        )
    }

    private fun parseRewards(arr: JSONArray): List<Reward> = (0 until arr.length()).map {
        val o = arr.getJSONObject(it)
        Reward(
            id = o.getLong("id"),
            name = o.getString("name"),
            cost = o.getInt("cost"),
            description = o.optString("description").takeIf { s -> s.isNotEmpty() },
            isActive = o.getBoolean("isActive"),
            isRecurring = o.getBoolean("isRecurring"),
            sortOrder = o.getInt("sortOrder")
        )
    }

    private fun parseRedemptions(arr: JSONArray): List<Redemption> = (0 until arr.length()).map {
        val o = arr.getJSONObject(it)
        Redemption(
            id = o.getLong("id"),
            rewardId = o.getLong("rewardId"),
            rewardName = o.getString("rewardName"),
            cost = o.getInt("cost"),
            createdAtMillis = o.getLong("createdAtMillis")
        )
    }
}