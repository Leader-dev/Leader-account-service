package com.leader.accountservice.mq

import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.core.Queue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class UserInfoMessageQueue @Autowired constructor(
    private val amqpTemplate: AmqpTemplate
) {

    companion object {
        private const val USER_NICKNAME_UPDATED = "user-nickname-updated"
    }

    @Bean
    fun userNameUpdatedQueue(): Queue {
        return Queue(USER_NICKNAME_UPDATED)
    }

    fun sendUserNicknameUpdated(userId: ObjectId, nickname: String) {
        amqpTemplate.convertAndSend(USER_NICKNAME_UPDATED, Document("userId", userId).append("nickname", nickname))
    }
}