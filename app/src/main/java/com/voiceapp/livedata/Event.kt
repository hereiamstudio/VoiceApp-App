package com.voiceapp.livedata

/**
 * This is used as a wrapper around events which are only consumed by their first receiver, and will
 * yield `null` for all further uses.
 *
 * @param T The type of data this event contains.
 * @param content The event content.
 */
class Event<out T>(private val content: T) {

    private var hasBeenHandled = false

    /**
     * Get the content of this event. If this event has not been handled, a non-`null` value will
     * be returned, otherwise `null` will be returned.
     *
     * @return The content of this event, or `null` if it has already been handled.
     */
    fun getContentIfNotHandled(): T? = if (hasBeenHandled) {
        null
    } else {
        hasBeenHandled = true
        content
    }

    /**
     * Peek at the content of this [Event] without handling it.
     *
     * @return The content of this [Event].
     */
    fun peek() = content
}