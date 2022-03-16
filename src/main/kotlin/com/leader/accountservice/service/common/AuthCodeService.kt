package com.leader.accountservice.service.common

import com.leader.accountservice.data.common.AuthCodeRecord
import com.leader.accountservice.data.common.AuthCodeRecordRepository
import com.leader.accountservice.resource.SMSService
import com.leader.accountservice.util.component.DateUtil
import com.leader.accountservice.util.component.RandomUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AuthCodeService @Autowired constructor(
    val dateUtil: DateUtil,
    val randomUtil: RandomUtil,
    val authCodeRecordRepository: AuthCodeRecordRepository,
    val smsService: SMSService
) {
    companion object {
        private const val AUTHCODE_LENGTH = 6
        private const val AUTHCODE_REQUEST_INTERVAL: Long = 60000
        private const val AUTHCODE_EXPIRE: Long = 300000
    }

    private fun timePassedSinceLastAuthCode(phone: String): Long {
        val authCodeRecord = authCodeRecordRepository.findByPhone(phone) ?: return -1
        return dateUtil.getCurrentTime() - authCodeRecord.timestamp.time
    }

    private fun insertAuthCodeRecord(phone: String, authcode: String) {
        val authCodeRecord = AuthCodeRecord()
        authCodeRecord.phone = phone
        authCodeRecord.authcode = authcode
        authCodeRecord.timestamp = dateUtil.getCurrentDate()
        authCodeRecordRepository.deleteByPhone(phone) // make sure previous ones are deleted
        authCodeRecordRepository.insert(authCodeRecord)
    }

    fun sendAuthCode(phone: String): Boolean {
        val timePassed = timePassedSinceLastAuthCode(phone)
        if (timePassed != -1L && timePassed < AUTHCODE_REQUEST_INTERVAL) {
            return false
        }

        // randomly generate authcode
        val authcode: String = randomUtil.nextAuthCode(AUTHCODE_LENGTH)
        smsService.sendAuthCode(phone, authcode)

        // insert record to database
        insertAuthCodeRecord(phone, authcode)
        return true
    }

    fun validateAuthCode(phone: String, authcode: String): Boolean {
        val authCodeRecord: AuthCodeRecord = authCodeRecordRepository.findByPhone(phone) ?: return false
        val timePassed: Long = dateUtil.getCurrentTime() - authCodeRecord.timestamp.time
        return timePassed <= AUTHCODE_EXPIRE && authCodeRecord.authcode == authcode
    }

    fun invalidateAuthCode(phone: String) {
        authCodeRecordRepository.deleteByPhone(phone)
    }
}