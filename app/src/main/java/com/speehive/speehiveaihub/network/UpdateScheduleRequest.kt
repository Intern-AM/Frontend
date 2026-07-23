package com.speehive.speehiveaihub.network

import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class UpdateScheduleRequest(
    @SerializedName(value = "schdtimeLinkedIn", alternate = ["SchdtimeLinkedIn", "schdtimeLinkedin", "SchdTimeLinkedIn", "schdTimeLinkedIn", "SchdTimeLinkedin", "schdTimeLinkedin"])
    val schdtimeLinkedIn: String?,

    @SerializedName(value = "schdtimeInstagram", alternate = ["SchdtimeInstagram", "schdtimeinstagram", "SchdTimeInstagram", "schdTimeInstagram"])
    val schdtimeInstagram: String?,

    @SerializedName(value = "schdtimeTeams", alternate = ["SchdtimeTeams", "schdtimeteams", "SchdTimeTeams", "schdTimeTeams"])
    val schdtimeTeams: String?,

    @SerializedName(value = "schdtimeWhatsapp", alternate = ["schdtimeWhatsApp", "SchdtimeWhatsApp", "SchdtimeWhatsapp", "SchdTimeWhatsapp", "schdTimeWhatsapp", "SchdTimeWhatsApp", "schdTimeWhatsApp"])
    val schdtimeWhatsapp: String?,

    val isLinkedInModified: Boolean = false,
    val isInstagramModified: Boolean = false,
    val isTeamsModified: Boolean = false,
    val isWhatsappModified: Boolean = false
) {
    fun toJsonObject(): JsonObject {
        val obj = JsonObject()

        if (schdtimeLinkedIn != null) {
            obj.addProperty("schdtimeLinkedIn", schdtimeLinkedIn)
        } else if (isLinkedInModified) {
            obj.add("schdtimeLinkedIn", JsonNull.INSTANCE)
        }

        if (schdtimeInstagram != null) {
            obj.addProperty("schdtimeInstagram", schdtimeInstagram)
        } else if (isInstagramModified) {
            obj.add("schdtimeInstagram", JsonNull.INSTANCE)
        }

        if (schdtimeTeams != null) {
            obj.addProperty("schdtimeTeams", schdtimeTeams)
        } else if (isTeamsModified) {
            obj.add("schdtimeTeams", JsonNull.INSTANCE)
        }

        if (schdtimeWhatsapp != null) {
            obj.addProperty("schdtimeWhatsapp", schdtimeWhatsapp)
        } else if (isWhatsappModified) {
            obj.add("schdtimeWhatsapp", JsonNull.INSTANCE)
        }

        return obj
    }
}


