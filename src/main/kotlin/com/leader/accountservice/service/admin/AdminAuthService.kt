package com.leader.accountservice.service.admin

import com.leader.accountservice.data.admin.Admin
import com.leader.accountservice.data.admin.AdminRepository
import com.leader.accountservice.util.component.PasswordEncodeUtil
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AdminAuthService @Autowired constructor(
    private val adminRepository: AdminRepository,
    private val passwordEncodeUtil: PasswordEncodeUtil
) {

    fun usernameExists(username: String): Boolean {
        return adminRepository.existsByUsername(username)
    }

    fun validateAdmin(username: String, password: String): Admin? {
        val admin = adminRepository.findByUsername(username) ?: return null
        val correctPassword = admin.password ?: return admin  // if one doesn't have a password, it's valid
        return if (passwordEncodeUtil.matches(password, correctPassword)) admin else null
    }

    fun changePassword(adminId: ObjectId, password: String) {
        adminRepository.findById(adminId).ifPresent { admin ->
            admin.password = passwordEncodeUtil.encode(password)
            adminRepository.save(admin)
        }
    }
}