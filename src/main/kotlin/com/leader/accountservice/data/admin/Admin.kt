package com.leader.accountservice.data.admin

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository

@Document(collection = "admin_list")
class Admin {

    @Id
    lateinit var id: ObjectId
    lateinit var username: String
    var password: String? = null
}

interface AdminRepository : MongoRepository<Admin, ObjectId> {

    fun existsByUsername(username: String): Boolean

    fun findByUsername(username: String): Admin?
}