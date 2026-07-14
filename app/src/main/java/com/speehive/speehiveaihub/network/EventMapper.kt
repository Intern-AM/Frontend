package com.speehive.speehiveaihub.network

import com.speehive.speehiveaihub.models.Event

fun EventResponse.toEvent(): Event {

    return Event(
        id = id,
        title = title,
        description = description,
        startTime = startTime,
        endTime = endTime,
        location = location,
        eventType = eventType,
        status = status,
        approvalDeadline = approvalDeadline,
        designerImageUrl = RetrofitClient.getFormattedImageUrl(designerImageUrl)
    )
}