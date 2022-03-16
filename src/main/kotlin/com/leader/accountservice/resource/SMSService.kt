package com.leader.accountservice.resource

interface SMSService {

    fun sendAuthCode(phone: String, authcode: String)
}