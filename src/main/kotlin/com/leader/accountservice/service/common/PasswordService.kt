package com.leader.accountservice.service.common

import com.leader.accountservice.ThreadJWTData
import com.leader.accountservice.util.InternalErrorException
import com.leader.accountservice.util.component.DateUtil
import com.leader.accountservice.util.component.RSAUtil
import com.leader.accountservice.util.component.RandomUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.security.PrivateKey
import java.util.*

@Service
class PasswordService @Autowired constructor(
    private val dateUtil: DateUtil,
    private val randomUtil: RandomUtil,
    private val rsaUtil: RSAUtil,
    private val threadJWTData: ThreadJWTData
) {

    companion object {
        const val PRIVATE_KEY_ID = "private_key_id"
        private const val RSA_KEYSIZE = 1024
        private const val RSA_KEY_EXPIRE: Long = 60000
    }

    private val keyMap = HashMap<UUID, Pair<PrivateKey, Date>>()

    private fun generateNewUUID(): UUID {
        return randomUtil.nextUUID { !keyMap.containsKey(it) }
    }

    private fun putKeyIntoKeyMap(key: PrivateKey): UUID {
        val id = generateNewUUID()
        keyMap[id] = key to dateUtil.getCurrentDate()
        return id
    }

    private fun getKeyFromKeyMap(id: UUID): PrivateKey? {
        val keyToCreateDate = keyMap[id] ?: return null
        return if (keyToCreateDate.second.time + RSA_KEY_EXPIRE < dateUtil.getCurrentTime()) null
            else keyToCreateDate.first
    }

    private fun savePrivateKey(key: PrivateKey) {
        // save private to session, recording timestamp
        val id = putKeyIntoKeyMap(key)
        threadJWTData[PRIVATE_KEY_ID] = id.toString()
    }

    // get and validate private key
    private fun getPrivateKey(): PrivateKey? {
        // get and validate private key
        val keyIdString = threadJWTData[PRIVATE_KEY_ID] ?: return null
        val keyId: UUID = UUID.fromString(keyIdString as String) ?: return null

        // remove private key from session
        threadJWTData.remove(PRIVATE_KEY_ID)

        // check if key expired
        return getKeyFromKeyMap(keyId)
    }

    fun generateKey(): ByteArray {
        val keyPair = rsaUtil.generateRSAKeyPair(RSA_KEYSIZE)
        savePrivateKey(keyPair.private)
        return keyPair.public.encoded
    }

    fun decrypt(password: String): String {
        val key = getPrivateKey() ?: throw InternalErrorException("Private key not found.")
        return rsaUtil.decryptRSA(password, key)
    }
}