package com.leader.accountservice.controller.user

import com.leader.accountservice.service.common.AuthCodeService
import com.leader.accountservice.service.common.ContextService
import com.leader.accountservice.service.common.PasswordService
import com.leader.accountservice.service.user.UserAuthService
import com.leader.accountservice.util.*
import org.bson.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user")
class UserAuthController @Autowired constructor(
    private val userAuthService: UserAuthService,
    private val authCodeService: AuthCodeService,
    private val passwordService: PasswordService,
    private val contextService: ContextService
) {

    class QueryObject {
        var nickname: String? = null
        var phone: String? = null
        var authcode: String? = null
        var password: String? = null
    }

    @PostMapping("/authcode")
    fun sendAuthCode(@RequestBody queryObject: QueryObject): Document? {
        val phone = queryObject.phone.isRequiredArgument("phone")
        val sendSuccess = authCodeService.sendAuthCode(phone)
        return if (sendSuccess) success() else error("request_too_frequent")
    }

    @PostMapping("/check-authcode")
    fun checkAuthCode(@RequestBody queryObject: QueryObject): Document {
        val phone = queryObject.phone.isRequiredArgument("phone")
        val authcode = queryObject.authcode.isRequiredArgument("authcode")
        if (!authCodeService.validateAuthCode(phone, authcode)) {
            return error("authcode_incorrect")
        }
        return success()
    }

    @PostMapping("/key")
    fun getPublicKey(): Document {
        // generate public key
        val publicKey = passwordService.generateKey()

        // put public key in response
        return success("publicKey", publicKey)
    }

    @PostMapping("/decrypt")
    fun decryptTest(@RequestBody queryObject: QueryObject): Document {
        val password = queryObject.password.isRequiredArgument("password")
        return success("password", passwordService.decrypt(password))
    }

    @PostMapping("/userid")
    fun getUserId(): Document {
        return success("userId", contextService.userId)
    }

    @PostMapping("/register")
    fun register(@RequestBody queryObject: QueryObject): Document {
        val nickname = queryObject.nickname.isRequiredArgument("nickname")
        val phone = queryObject.phone.isRequiredArgument("phone")
        val password = queryObject.password.isRequiredArgument("password")
        val authcode = queryObject.authcode.isRequiredArgument("authcode")

        // check authcode
        if (!authCodeService.validateAuthCode(phone, authcode)) {
            return error("authcode_incorrect")
        }

        // register user
        val decryptedPassword = passwordService.decrypt(password)
        val user = userAuthService.createUser(phone, decryptedPassword, nickname)

        // invalidate current authcode
        authCodeService.invalidateAuthCode(phone)

        // update token
        contextService.userId = user.id

        return success()
    }

    @PostMapping("/login")
    fun login(@RequestBody queryObject: QueryObject): Document {
        val phone = queryObject.phone.isRequiredArgument("phone")
        if (!userAuthService.phoneExists(phone)) {
            return error("user_not_exist")
        }
        val password = queryObject.password
        val authcode = queryObject.authcode
        val user = if (password != null) {
            val decryptedPassword = passwordService.decrypt(password)
            userAuthService.validateUser(phone, decryptedPassword)
                ?: return error("password_incorrect")
        } else if (authcode != null) {
            if (!authCodeService.validateAuthCode(phone, authcode)) {
                return error("authcode_incorrect")
            }
            userAuthService.getUserByPhone(phone)
        } else {
            throw InternalErrorException("Require argument password or authcode")
        }
        contextService.userId = user.id
        return success()
    }

    @PostMapping("/logout")
    fun logout(): Document? {
        contextService.userId = null
        return success()
    }

    @PostMapping("/quick-login")
    fun quickLogin(@RequestBody queryObject: QueryObject): Document? {
        val phone = queryObject.phone.isRequiredArgument("phone")
        val authcode = queryObject.authcode.isRequiredArgument("authcode")
        // check authcode
        if (!authCodeService.validateAuthCode(phone, authcode)) {
            return error("authcode_incorrect")
        }
        val user = if (userAuthService.phoneExists(phone)) {
            userAuthService.getUserByPhone(phone)
        } else {
            userAuthService.createUser(phone)
        }

        // invalidate current authcode
        authCodeService.invalidateAuthCode(phone)

        // update token
        contextService.userId = user.id

        return success()
    }

    @PostMapping("/change-nickname")
    fun changeNickname(@RequestBody queryObject: QueryObject): Document? {
        val nickname = queryObject.nickname.isRequiredArgument("nickname")
        val userId = contextService.userId ?: return error("not_logged_in")
        userAuthService.updateUserNicknameById(userId, nickname)
        return success()
    }

    @PostMapping("/change-password")
    fun changePassword(@RequestBody queryObject: QueryObject): Document? {
        val phone = queryObject.phone.isRequiredArgument("phone")
        val authcode = queryObject.authcode.isRequiredArgument("authcode")
        val password = queryObject.password.isRequiredArgument("password")
        if (!authCodeService.validateAuthCode(phone, authcode)) {
            return error("authcode_incorrect")
        }

        // decrypt password
        val decryptedPassword = passwordService.decrypt(password)

        // update user
        userAuthService.updateUserPasswordByPhone(phone, decryptedPassword)
        return success()
    }
}