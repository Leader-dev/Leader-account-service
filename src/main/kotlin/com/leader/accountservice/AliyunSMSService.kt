package com.leader.accountservice

import com.aliyun.dysmsapi20170525.Client
import com.aliyun.dysmsapi20170525.models.SendSmsRequest
import com.aliyun.teaopenapi.models.Config
import com.leader.accountservice.resource.SMSService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AliyunSMSService : SMSService {
    @Value("\${aliyun.sms.endpoint}")
    private val endpoint: String? = null

    @Value("\${aliyun.sms.access-key-id}")
    private val accessKeyId: String? = null

    @Value("\${aliyun.sms.access-key-secret}")
    private val accessKeySecret: String? = null

    @Value("\${aliyun.sms.sign-name}")
    private val signName: String? = null

    @Value("\${aliyun.sms.template-code}")
    private val templateCode: String? = null

    @Value("\${aliyun.sms.authcode-param-name}")
    private val authcodeParamName: String? = null

    fun createClient(): Client {
        val config: Config = Config()
            .setAccessKeyId(accessKeyId)
            .setAccessKeySecret(accessKeySecret)
        config.endpoint = endpoint
        return Client(config)
    }

    override fun sendAuthCode(phone: String, authcode: String) {
        val client = createClient()
        val sendSmsRequest: SendSmsRequest = SendSmsRequest()
            .setPhoneNumbers(phone)
            .setSignName(signName)
            .setTemplateCode(templateCode)
            .setTemplateParam("{\"$authcodeParamName\":\"$authcode\"}")
        val code: String = client.sendSms(sendSmsRequest).getBody().code
        if ("OK" != code) {
            throw RuntimeException("Send authcode failed. Receiving code: $code")
        }
    }
}