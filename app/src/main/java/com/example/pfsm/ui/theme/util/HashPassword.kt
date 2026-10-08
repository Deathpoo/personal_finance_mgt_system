package com.example.pfsm.ui.theme.util

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

private const val ITERATIONS = 120_000
private const val KEY_LENGTH_BITS = 256
private const val SALT_BYTES = 16

private fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }
private fun String.hexToBytes() = chunked(2).map { it.toInt(16).toByte() }.toByteArray()

private fun pbkdf2(password: String, salt: ByteArray): ByteArray {
    val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
    return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
}

fun hashPassword(password: String): String {
    val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
    return "${salt.toHex()}:${pbkdf2(password, salt).toHex()}"
}


fun verifyPassword(password: String, stored: String): Boolean {
    val parts = stored.split(":")
    if (parts.size != 2) return false
    val salt = parts[0].hexToBytes()
    val expected = parts[1].hexToBytes()
    return MessageDigest.isEqual(expected, pbkdf2(password, salt))
}