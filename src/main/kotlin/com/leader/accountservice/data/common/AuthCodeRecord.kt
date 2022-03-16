package com.leader.accountservice.data.common

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

@Document(collection = "auth_code")
class AuthCodeRecord {

    @Id
    lateinit var id: ObjectId
    lateinit var phone: String
    lateinit var authcode: String
    lateinit var timestamp: Date
}

interface AuthCodeRecordRepository : MongoRepository<AuthCodeRecord, ObjectId> {

    fun findByPhone(phone: String): AuthCodeRecord?

    fun deleteByPhone(phone: String)
}