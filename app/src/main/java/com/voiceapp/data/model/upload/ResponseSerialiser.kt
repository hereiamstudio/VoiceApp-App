package com.voiceapp.data.model.upload

interface ResponseSerialiser {

    fun serialiseResponse(response: Response): String
}