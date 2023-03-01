package com.voiceapp.data.model.upload

/*
{
    encryption_version: 1,
    data: [ciphered stringified JSON],
    iv: [public key encrypted iv used to cipher data],
    secret: [public key encrypted secret used to cipher data]
}

data is encrypted using AES (iv and secret)
iv and secret are encrypted using RSA with the public key (only server has the private key and can decrypt them)

 */

data class EncryptedResponse(
    val encryption_version: Int = 1,
    val id: String?,
    val enumerator_id: String?,
    val data: String,
    val iv: String?,
    val secret: String?)