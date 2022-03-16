package com.leader.accountservice.util.component

import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.util.*
import javax.crypto.Cipher

private const val RSA_KEY_PAIR_GENERATOR_ALGORITHM_NAME = "RSA"

private const val RSA_CIPHER_TRANSFORMATION_NAME = "RSA/ECB/PKCS1Padding"

@Component
class RSAUtil {

    fun generateRSAKeyPair(keysize: Int): KeyPair {
        val generator = KeyPairGenerator.getInstance(RSA_KEY_PAIR_GENERATOR_ALGORITHM_NAME)
        generator.initialize(keysize)
        return generator.generateKeyPair()
    }

    fun encryptRSA(plainText: String, key: PublicKey): String {
        val cipher = Cipher.getInstance(RSA_CIPHER_TRANSFORMATION_NAME)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val bytesIn = plainText.toByteArray(StandardCharsets.UTF_8)
        val bytesEncrypted = cipher.doFinal(bytesIn)
        return Base64.getEncoder().encodeToString(bytesEncrypted)
    }

    fun decryptRSA(cipherText: String, key: PrivateKey): String {
        val cipher = Cipher.getInstance(RSA_CIPHER_TRANSFORMATION_NAME)
        cipher.init(Cipher.DECRYPT_MODE, key)
        val bytesIn = cipherText.toByteArray(StandardCharsets.UTF_8)
        val bytesDecoded = Base64.getDecoder().decode(bytesIn)
        return String(cipher.doFinal(bytesDecoded))
    }
}