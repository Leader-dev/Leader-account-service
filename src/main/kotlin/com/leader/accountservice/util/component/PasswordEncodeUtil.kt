package com.leader.accountservice.util.component

import com.leader.accountservice.util.InternalErrorException
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.security.crypto.password.DelegatingPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

private const val ARGON2_ALGORITHM_ID = "argon2"

@Component
class PasswordEncodeUtil {

    private val passwordEncoder: PasswordEncoder = run {
        val encoders = mapOf(ARGON2_ALGORITHM_ID to Argon2PasswordEncoder())
        DelegatingPasswordEncoder(ARGON2_ALGORITHM_ID, encoders)
    }

    fun encode(rawPassword: String) = passwordEncoder.encode(rawPassword)
        ?: throw InternalErrorException("Password encoder yields a null value.")

    fun matches(rawPassword: String, password: String) = passwordEncoder.matches(rawPassword, password)
}