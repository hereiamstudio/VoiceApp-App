package com.voiceapp.data.model.upload

import com.google.gson.Gson
import javax.inject.Inject

class JsonResponseSerialiser @Inject constructor(
    private val gson: Gson) : ResponseSerialiser {

    override fun serialiseResponse(response: Response) = gson.toJson(response)
}