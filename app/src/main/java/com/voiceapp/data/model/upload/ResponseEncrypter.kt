package com.voiceapp.data.model.upload

import com.voiceapp.util.Base64Encoder
import com.voiceapp.util.Encrypter
import javax.inject.Inject

class ResponseEncrypter @Inject constructor(
    private val serialiser: ResponseSerialiser,
    private val encrypter: Encrypter,
    private val base64Encoder: Base64Encoder
) {

    fun encryptResponse(response: Response): EncryptedResponse {
        val responseData = serialiser.serialiseResponse(response)
        val iv = encrypter.getIV()
        val secret = encrypter.generateAESKey()

        return EncryptedResponse(
            id = response.id,
            enumerator_id = response.enumerator_id,
            data = base64Encoder.encodeToString(encrypter.encryptAES(iv, secret, responseData)),
            iv = encrypter.encryptDataRSA(base64Encoder.encodeToString(iv)),
            secret = encrypter.encryptDataRSA(base64Encoder.encodeToString(secret.encoded)))
    }
}