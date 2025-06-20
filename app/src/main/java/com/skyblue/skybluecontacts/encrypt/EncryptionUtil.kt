package com.skyblue.skybluecontacts.encrypt

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object EncryptionUtil {
    private const val SECRET_KEY = "1234567890123456" // 16-char AES key (store securely)
    private const val TRANSFORMATION = "AES/CBC/PKCS5PADDING"

    fun encrypt(data: String): String {
        val keySpec = SecretKeySpec(SECRET_KEY.toByteArray(), "AES")
        val iv = IvParameterSpec(SECRET_KEY.toByteArray()) // For demo only, use random IV in prod
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv)
        val encrypted = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(encrypted, Base64.DEFAULT)
    }

    fun decrypt(data: String): String {
        val keySpec = SecretKeySpec(SECRET_KEY.toByteArray(), "AES")
        val iv = IvParameterSpec(SECRET_KEY.toByteArray())
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, iv)
        val decrypted = cipher.doFinal(Base64.decode(data, Base64.DEFAULT))
        return String(decrypted)
    }
}
