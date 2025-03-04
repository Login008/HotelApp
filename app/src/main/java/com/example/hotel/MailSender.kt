package com.example.hotel

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object MailSender {

    suspend fun sendEmail(recipient: String, subject: String, body: String) {
        withContext(Dispatchers.IO) {
            try {
                val username = "stalkerrealsteel@gmail.com" // Укажите ваш email
                val password = "nzimcarhjhipqagi" // Сгенерируйте пароль приложения в Google

                val props = Properties().apply {
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.starttls.enable", "true")
                    put("mail.smtp.host", "smtp.gmail.com")
                    put("mail.smtp.port", "587")
                }

                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(username, password)
                    }
                })

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(username))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient))
                    setSubject(subject)
                    setText(body)
                }

                Transport.send(message)
                Log.d("MailSender", "Email sent successfully to $recipient")

            } catch (e: MessagingException) {
                Log.e("MailSender", "Error sending email: ${e.message}")
            }
        }
    }
}
