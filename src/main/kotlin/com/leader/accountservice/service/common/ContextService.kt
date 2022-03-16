package com.leader.accountservice.service.common

import com.leader.accountservice.ThreadJWTData
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ContextService @Autowired constructor(
    val threadJWTData: ThreadJWTData
) {

    companion object {
        private const val USER_ID_KEY = "userId"
        private const val ADMIN_ID_KEY = "adminId"
    }

    var userId: ObjectId?
        get() {
            val stringObjectId = threadJWTData[USER_ID_KEY] as? String ?: return null
            return ObjectId(stringObjectId)
        }
        set(value) {
            if (value == null) {
                threadJWTData.remove(USER_ID_KEY)
            } else {
                threadJWTData[USER_ID_KEY] = value.toString()
            }
        }

    var adminId: ObjectId?
        get() {
            val stringObjectId = threadJWTData[ADMIN_ID_KEY] as? String ?: return null
            return ObjectId(stringObjectId)
        }
        set(value) {
            if (value == null) {
                threadJWTData.remove(ADMIN_ID_KEY)
            } else {
                threadJWTData[ADMIN_ID_KEY] = value.toString()
            }
        }
}