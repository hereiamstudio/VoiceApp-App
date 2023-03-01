package com.voiceapp.util

import android.util.Base64
import com.voiceapp.BuildConfig
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject

class Encrypter @Inject constructor(
    private val base64Encoder: Base64Encoder
) {

    @Throws(NoSuchAlgorithmException::class)
    fun generateAESKey(): SecretKey {
        val secureRandom = SecureRandom()
        // Do *not* seed secureRandom! Automatically seeded from system entropy.
        val keyGenerator: KeyGenerator = KeyGenerator.getInstance("AES")
        // Key size = 256 bits
        keyGenerator.init(256, secureRandom)

        return keyGenerator.generateKey()
    }

    fun getIV(): ByteArray {
        val secureRandom = SecureRandom()
        val iv = ByteArray(16)
        secureRandom.nextBytes(iv)

        return iv
    }

    @Throws(java.lang.Exception::class)
    fun encryptAES(iv: ByteArray, secretKey: SecretKey, plainText: String): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val ivspec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec)

        return cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
    }

    fun encryptDataRSA(text: String?) = text?.let {
        encryptDataRSA(it.toByteArray())
    } ?: encryptDataRSA("null".toByteArray())

    fun encryptDataRSA(text: ByteArray?): String? {
        var encoded: String? = ""
        val encrypted: ByteArray?
        try {
            val pubKey = getPubKey()
            encrypted = encryptRSA(text, pubKey)
            encoded = base64Encoder.encodeToString(encrypted)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return encoded
    }

    private fun encryptRSA(text: ByteArray?, pubKey: PublicKey): ByteArray? {
        var cipherText: ByteArray? = null
        try {
            val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            cipher.init(Cipher.ENCRYPT_MODE, pubKey)
            cipherText = cipher.doFinal(text)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return cipherText
    }

    private fun getPubKey(): PublicKey {
        val keyBytes = Base64.decode(BuildConfig.PUBLIC_KEY, Base64.DEFAULT)
        val spec = X509EncodedKeySpec(keyBytes)
        val kf = KeyFactory.getInstance("RSA")

        return kf.generatePublic(spec)
    }
}