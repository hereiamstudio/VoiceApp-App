package com.voiceapp.upload

import com.voiceapp.data.model.upload.EncryptedResponse
import com.voiceapp.util.Base64Encoder
import com.voiceapp.data.model.upload.Response
import com.voiceapp.util.Encrypter
import com.voiceapp.data.model.upload.ResponseEncrypter
import com.voiceapp.data.model.upload.ResponseSerialiser
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import javax.crypto.SecretKey

@RunWith(MockitoJUnitRunner::class)
class ResponseEncrypterTest {

    @Mock
    private lateinit var serialiser: ResponseSerialiser
    @Mock
    private lateinit var encrypter: Encrypter
    @Mock
    private lateinit var base6Encoder: Base64Encoder

    @Mock
    private lateinit var response: Response
    @Mock
    private lateinit var secret: SecretKey

    private lateinit var responseEncrypter: ResponseEncrypter

    @Before
    fun setUp() {
        responseEncrypter = ResponseEncrypter(serialiser, encrypter, base6Encoder)
    }

    @Test
    fun encryptResponseReturnsExpectedObject() {
        val iv = byteArrayOf(1, 2, 3)
        val encryptedResponse = byteArrayOf(4, 5, 6)
        val encodedSecret = byteArrayOf(7, 8, 9)
        whenever(serialiser.serialiseResponse(response))
            .thenReturn("serialisedResponse")
        whenever(encrypter.getIV())
            .thenReturn(iv)
        whenever(encrypter.generateAESKey())
            .thenReturn(secret)
        whenever(response.id)
            .thenReturn("id123")
        whenever(response.enumerator_id)
            .thenReturn("enumerator123")
        whenever(encrypter.encryptAES(iv, secret, "serialisedResponse"))
            .thenReturn(encryptedResponse)
        whenever(base6Encoder.encodeToString(encryptedResponse))
            .thenReturn("encryptedResponse")
        whenever(base6Encoder.encodeToString(iv))
            .thenReturn("ivBase64")
        whenever(encrypter.encryptDataRSA("ivBase64"))
            .thenReturn("ivEncrypted")
        whenever(secret.encoded)
            .thenReturn(encodedSecret)
        whenever(base6Encoder.encodeToString(encodedSecret))
            .thenReturn("secretBase64")
        whenever(encrypter.encryptDataRSA("secretBase64"))
            .thenReturn("encryptedSecret")
        val expected = EncryptedResponse(
            1,
            "id123",
            "enumerator123",
            "encryptedResponse",
            "ivEncrypted",
            "encryptedSecret")

        val result = responseEncrypter.encryptResponse(response)

        assertEquals(expected, result)
    }
}