package com.example.nlp

import com.example.data.TaskEntity
import java.util.Calendar

data class ParsedTaskResult(
    val title: String,
    val description: String = "",
    val tags: List<String> = emptyList(),
    val priority: String = "MEDIUM", // HIGH, MEDIUM, LOW, NONE
    val isUrgent: Boolean = false,
    val isImportant: Boolean = false,
    val dueDateTimestamp: Long? = null,
    val dueTimeString: String? = null,
    val recurrenceText: String? = null
)

object NlpParser {

    fun parseInput(input: String): ParsedTaskResult {
        var cleanText = input.trim()
        val tags = mutableListOf<String>()
        var priority = "MEDIUM"
        var isUrgent = false
        var isImportant = false
        var dueTimestamp: Long? = null
        var dueTimeString: String? = null
        var recurrenceText: String? = null

        // 1. Extract Tags: #tagname
        val tagRegex = Regex("""#(\w+)""")
        tagRegex.findAll(cleanText).forEach { match ->
            tags.add(match.groupValues[1].lowercase())
        }
        cleanText = tagRegex.replace(cleanText, "").trim()

        // 2. Extract Priority & Eisenhower Flags: !high, !urgent, !important, !p1, !p2, !p3
        if (cleanText.contains("!urgent", ignoreCase = true)) {
            isUrgent = true
            cleanText = cleanText.replace(Regex("""!urgent""", RegexOption.IGNORE_CASE), "").trim()
        }
        if (cleanText.contains("!important", ignoreCase = true)) {
            isImportant = true
            cleanText = cleanText.replace(Regex("""!important""", RegexOption.IGNORE_CASE), "").trim()
        }
        if (cleanText.contains("!high", ignoreCase = true) || cleanText.contains("!p1", ignoreCase = true)) {
            priority = "HIGH"
            isUrgent = true
            isImportant = true
            cleanText = cleanText.replace(Regex("""!(high|p1)""", RegexOption.IGNORE_CASE), "").trim()
        } else if (cleanText.contains("!medium", ignoreCase = true) || cleanText.contains("!p2", ignoreCase = true)) {
            priority = "MEDIUM"
            isImportant = true
            cleanText = cleanText.replace(Regex("""!(medium|p2)""", RegexOption.IGNORE_CASE), "").trim()
        } else if (cleanText.contains("!low", ignoreCase = true) || cleanText.contains("!p3", ignoreCase = true)) {
            priority = "LOW"
            cleanText = cleanText.replace(Regex("""!(low|p3)""", RegexOption.IGNORE_CASE), "").trim()
        }

        // 3. Extract Recurrence: "every Friday", "every day", "every month"
        val recurrenceRegex = Regex("""every\s+(day|monday|tuesday|wednesday|thursday|friday|saturday|sunday|week|month)""", RegexOption.IGNORE_CASE)
        val recurrenceMatch = recurrenceRegex.find(cleanText)
        if (recurrenceMatch != null) {
            recurrenceText = recurrenceMatch.value
            cleanText = recurrenceRegex.replace(cleanText, "").trim()
        }

        // 4. Extract Date / Time keywords
        val calendar = Calendar.getInstance()
        var dateFound = false

        // Time regex: at 5 PM, 5pm, at 17:00, 3:30 pm
        val timeRegex = Regex("""(?:at\s+)?(\d{1,2})(?::(\d{2}))?\s*(am|pm)?""", RegexOption.IGNORE_CASE)
        val timeMatch = timeRegex.find(cleanText)
        var hour = 9
        var minute = 0

        if (timeMatch != null && (timeMatch.groupValues[3].isNotEmpty() || cleanText.contains("at ", ignoreCase = true) || timeMatch.groupValues[2].isNotEmpty())) {
            val rawHour = timeMatch.groupValues[1].toIntOrNull() ?: 9
            val rawMinute = timeMatch.groupValues[2].toIntOrNull() ?: 0
            val amPm = timeMatch.groupValues[3].lowercase()

            hour = when {
                amPm == "pm" && rawHour < 12 -> rawHour + 12
                amPm == "am" && rawHour == 12 -> 0
                else -> rawHour
            }
            minute = rawMinute
            dueTimeString = String.format("%02d:%02d %s", if (rawHour == 0) 12 else if (rawHour > 12) rawHour - 12 else rawHour, minute, if (hour >= 12) "PM" else "AM")
            cleanText = timeRegex.replace(cleanText, "").trim()
        }

        // Date Keywords: "today", "tomorrow", "next Friday"
        if (cleanText.contains("today", ignoreCase = true)) {
            dateFound = true
            cleanText = cleanText.replace(Regex("""today""", RegexOption.IGNORE_CASE), "").trim()
        } else if (cleanText.contains("tomorrow", ignoreCase = true)) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            dateFound = true
            cleanText = cleanText.replace(Regex("""tomorrow""", RegexOption.IGNORE_CASE), "").trim()
        } else {
            // Check day of week e.g. "Friday", "Monday"
            val dayMap = mapOf(
                "monday" to Calendar.MONDAY,
                "tuesday" to Calendar.TUESDAY,
                "wednesday" to Calendar.WEDNESDAY,
                "thursday" to Calendar.THURSDAY,
                "friday" to Calendar.FRIDAY,
                "saturday" to Calendar.SATURDAY,
                "sunday" to Calendar.SUNDAY
            )
            for ((dayName, dayCode) in dayMap) {
                if (cleanText.contains(dayName, ignoreCase = true)) {
                    dateFound = true
                    val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
                    var daysUntil = dayCode - currentDay
                    if (daysUntil <= 0) daysUntil += 7
                    calendar.add(Calendar.DAY_OF_YEAR, daysUntil)
                    cleanText = cleanText.replace(Regex(dayName, RegexOption.IGNORE_CASE), "").trim()
                    break
                }
            }
        }

        if (dateFound || dueTimeString != null) {
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            dueTimestamp = calendar.timeInMillis
        }

        // Clean up remaining dangling prepositions like "at", "by", "on"
        cleanText = cleanText.replace(Regex("""\b(at|by|on)\b""", RegexOption.IGNORE_CASE), "").replace(Regex("""\s+"""), " ").trim()

        if (cleanText.isEmpty()) {
            cleanText = "Untitled Task"
        }

        return ParsedTaskResult(
            title = cleanText,
            description = if (recurrenceText != null) "Recurring: $recurrenceText" else "",
            tags = tags,
            priority = priority,
            isUrgent = isUrgent,
            isImportant = isImportant,
            dueDateTimestamp = dueTimestamp,
            dueTimeString = dueTimeString,
            recurrenceText = recurrenceText
        )
    }

    fun toTaskEntity(parsed: ParsedTaskResult): TaskEntity {
        return TaskEntity(
            title = parsed.title,
            description = parsed.description,
            priority = parsed.priority,
            isUrgent = parsed.isUrgent,
            isImportant = parsed.isImportant,
            dueDateTimestamp = parsed.dueDateTimestamp,
            dueTimeString = parsed.dueTimeString,
            tags = parsed.tags.joinToString(",")
        )
    }
}
