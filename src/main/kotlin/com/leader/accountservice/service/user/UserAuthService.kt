package com.leader.accountservice.service.user

import com.leader.accountservice.mq.UserInfoMessageQueue
import com.leader.accountservice.data.user.User
import com.leader.accountservice.data.user.UserRepository
import com.leader.accountservice.util.InternalErrorException
import com.leader.accountservice.util.component.PasswordEncodeUtil
import com.leader.accountservice.util.component.RandomUtil
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class UserAuthService @Autowired constructor(
    private val userRepository: UserRepository,
    private val passwordEncodeUtil: PasswordEncodeUtil,
    private val randomUtil: RandomUtil,
    private val userInfoMessageQueue: UserInfoMessageQueue
) {

    @Value("\${leader.uid-length}")
    private val uidLength = 0

    @Value("\${leader.uid-length-capacity}")
    private val uidLengthCapacity: Long = 0

    private fun generateNewUid(): String {
        if (userRepository.count() > uidLengthCapacity) {
            throw InternalErrorException("Uid length capacity exceeded")
        }
        return randomUtil.nextNumberID(uidLength) { !uidExists(it) }
    }

    fun uidExists(uid: String): Boolean = userRepository.existsByUid(uid)

    fun assertUidExists(uid: String) {
        if (!uidExists(uid)) {
            throw InternalErrorException("Uid not exist")
        }
    }

    fun phoneExists(phone: String): Boolean {
        return userRepository.existsByPhone(phone)
    }

    fun assertPhoneExists(phone: String) {
        if (!phoneExists(phone)) {
            throw InternalErrorException("Phone not exist")
        }
    }

    fun getUserByPhone(phone: String) = userRepository.findByPhone(phone)
        ?: throw InternalErrorException("Phone not exist.")

    fun createUser(phone: String): User {
        return createUser(phone, null, null)
    }

    fun createUser(phone: String, password: String?, nickname: String?): User {
        val user = User()
        user.phone = phone
        if (password != null) {
            user.password = passwordEncodeUtil.encode(password)
        }
        user.nickname = nickname
        synchronized(userRepository) {
            user.uid = generateNewUid()
            val newUser = userRepository.insert(user)
            if (nickname != null) {
                userInfoMessageQueue.sendUserNicknameUpdated(newUser.id, nickname)
            }
            return newUser
        }
    }

    fun hasPassword(phone: String): Boolean {
        val user = getUserByPhone(phone)
        return user.password != null
    }

    fun validateUser(phone: String, password: String): User? {
        val user = getUserByPhone(phone)
        val correctPassword = user.password ?: return null
        return if (passwordEncodeUtil.matches(password, correctPassword)) user else null
    }

    fun updateUserPasswordByPhone(phone: String, newPassword: String) {
        val user = getUserByPhone(phone)
        user.password = passwordEncodeUtil.encode(newPassword)
        userRepository.save(user)
    }

    fun updateUserNicknameByPhone(phone: String, nickname: String) {
        val user = getUserByPhone(phone)
        user.nickname = nickname
        userRepository.save(user)
        userInfoMessageQueue.sendUserNicknameUpdated(user.id, nickname)
    }

    fun updateUserNicknameById(userId: ObjectId, nickname: String) {
        val user = userRepository.findById(userId).orElseThrow {
            InternalErrorException("User not exist.")
        }
        user.nickname = nickname
        userRepository.save(user)
        userInfoMessageQueue.sendUserNicknameUpdated(user.id, nickname)
    }

    fun getUserIdByPhone(phone: String): ObjectId {
        val user = getUserByPhone(phone)
        return user.id
    }

    fun getUserPhoneById(id: ObjectId): String? {
        val user = userRepository.findById(id).orElse(null) ?: return null
        return user.phone
    }
}