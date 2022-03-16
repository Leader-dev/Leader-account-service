package com.leader.accountservice.data.user

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

@Document(collection = "user_list")
class User {

    @Id
    lateinit var id: ObjectId
    lateinit var uid: String

    lateinit var phone: String
    var password: String? = null

    var nickname: String? = null
    var avatarUrl: String? = null

    // delete
    var deleteStartDate: Date? = null
}

interface UserRepository : MongoRepository<User, ObjectId> {

    fun existsByUid(uid: String): Boolean

    fun existsByPhone(phone: String): Boolean

    fun findByUid(uid: String): User?

    fun findByPhone(phone: String): User?
}