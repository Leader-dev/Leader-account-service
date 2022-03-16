package com.leader.accountservice.controller.admin

import com.leader.accountservice.service.admin.AdminAuthService
import com.leader.accountservice.service.common.ContextService
import com.leader.accountservice.service.common.PasswordService
import com.leader.accountservice.util.authError
import com.leader.accountservice.util.isRequiredArgument
import com.leader.accountservice.util.success
import com.leader.accountservice.util.error
import org.bson.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin")
class AdminAuthController @Autowired constructor (
    private val adminAuthService: AdminAuthService,
    private val passwordService: PasswordService,
    private val contextService: ContextService
) {

    class QueryObject {
        var username: String? = null
        var password: String? = null
    }

    @PostMapping("/key")
    fun getPublicKey(): Document {
        // generate public key
        val publicKey = passwordService.generateKey()

        // put public key in response
        return success("publicKey", publicKey)
    }

    @PostMapping("/adminid")
    fun getAdminId(): Document? {
        val adminId = contextService.adminId
        return success("adminId", adminId)
    }

    @PostMapping("/login")
    fun login(@RequestBody queryObject: QueryObject): Document {
        val username = queryObject.username.isRequiredArgument("phone")
        if (!adminAuthService.usernameExists(username)) {
            return error("user_not_exist")
        }
        val password = queryObject.password.isRequiredArgument("password")
        val decryptedPassword = passwordService.decrypt(password)
        val admin = adminAuthService.validateAdmin(username, decryptedPassword)
            ?: return error("password_incorrect")
        contextService.adminId = admin.id
        return success()
    }

    @PostMapping("/logout")
    fun logout(): Document? {
        contextService.adminId = null
        return success()
    }

    @PostMapping("/change-password")
    fun changePassword(@RequestBody queryObject: QueryObject): Document? {
        val newPassword = queryObject.password.isRequiredArgument("password")
        val adminId = contextService.adminId
            ?: return authError()
        adminAuthService.changePassword(adminId, newPassword)
        return success()
    }
}