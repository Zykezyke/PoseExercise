import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailSender {
    companion object {
        private const val HOST = "smtp.gmail.com"
        private const val PORT = "465"
        private const val EMAIL = "formfixteam@gmail.com"  // Replace with your email
        private const val PASSWORD = "lshs zzvd wpgx agnr"   // Replace with your app password

        fun sendOTPEmail(recipientEmail: String, otp: String, name:String, callback: (Boolean) -> Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                val success = sendEmail(recipientEmail, otp, name)
                withContext(Dispatchers.Main) {
                    callback(success)
                }
            }
        }

        fun sendForgotEmail(recipientEmail: String, otp: String, name:String, callback: (Boolean) -> Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                val success = forgotEmail(recipientEmail, otp, name)
                withContext(Dispatchers.Main) {
                    callback(success)
                }
            }
        }

        private fun sendEmail(recipientEmail: String, otp: String, name:String): Boolean {
            val props = Properties().apply {
                put("mail.smtp.ssl.enable", "true")
                put("mail.smtp.port", PORT)
                put("mail.smtp.auth", "true")
                put("mail.smtp.host", HOST)
                put("mail.smtp.socketFactory.port", PORT)
                put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
            }

            return try {
                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication() = PasswordAuthentication(EMAIL, PASSWORD)
                })

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(EMAIL))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
                    subject = "FormFix Account Verification"
                    setContent("""
                        <!DOCTYPE html>
                            <html lang="en">
                            <head>
                              <meta charset="UTF-8" />
                              <title></title>
                              <style>
                                body {
                                  margin: 0;
                                  padding: 0;
                                  font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
                                  color: #333;
                                  background-color: #fff;
                                }
                            
                                .container {
                                  margin: 0 auto;
                                  width: 100%;
                                  max-width: 600px;
                                  padding: 0 0px;
                                  border-radius: 5px;
                                  line-height: 1.8;
                                }
                            
                                .header {
                                  border-bottom: 1px solid #dfdfdf;
                                  text-align: center;
                                }
                            
                                .header a {
                                  font-size: 1.4em;
                                  color: black;
                                  text-decoration: none;
                                  font-weight: 600;
                                }
                            
                                .content {
                                  min-width: 700px;
                                  overflow: auto;
                                  line-height: 2;
                                }
                            
                                .otp {
                                  background: linear-gradient(to right, #28405C 0, #3C628F 50%, #5187C1 100%);
                                  margin: 0 auto;
                                  width: max-content;
                                  padding: 0 10px;
                                  color: #fff;
                                  border-radius: 4px;
                                  letter-spacing: 3px;
                                }
                            
                                .footer {
                                  color: #aaa;
                                  font-size: 0.8em;
                                  line-height: 1;
                                  font-weight: 300;
                                }
                            
                                .email-info {
                                  color: #666666;
                                  font-weight: 400;
                                  font-size: 13px;
                                  line-height: 18px;
                                  padding-bottom: 6px;
                                }
                            
                                .email-info a {
                                  text-decoration: none;
                                  color: #5187C1;
                                }
                              </style>
                            </head>
                            
                            <body>
                              <div class="container">
                                <div class="header">
                                  <a>FormFix</a>
                                </div>
                                <br />
                                <strong>Dear $name,</strong>
                                <p>
                                  We have received a sign up request for your FormFix account. For
                                  security purposes, please verify your identity by providing the
                                  following One-Time Password (OTP).
                                  <br />
                                  <b>Your One-Time Password (OTP) verification code is:</b>
                                </p>
                                <h2 class="otp">$otp</h2>
                                <p style="font-size: 0.9em">
                                  <strong>One-Time Password (OTP) is valid for 5 minutes.</strong>
                                  <br />
                                  <br />
                                  If you did not initiate this sign up request,you can safely ignore this email
                                  . Please ensure the confidentiality of your OTP and avoid sharing it with
                                  anyone.<br />
                                  <strong>Do not forward or give this code to anyone.</strong>
                                  <br />
                                  <br />
                                  <strong>Thank you for using FormFix.</strong>
                                  <br />
                                  <br />
                                  Best regards,
                                  <br />
                                  <strong>FormFix Team</strong>
                                </p>
                            
                                <hr style="border: none; border-top: 0.5px solid #131111" />
                                <div class="footer">
                                  <p>This email can't receive replies.</p>
                                  <p>
                                    For more information about FormFix, visit
                                    <strong>[WEBSITE LINK]</strong>
                                  </p>
                                </div>
                              </div>
                              <div style="text-align: center">
                                <div class="email-info">
                                  <span>
                                    This email was sent to
                                    <a href="mailto:{Email Adress}">$recipientEmail</a>
                                  </span>
                                </div>
                            
                                <div class="email-info">
                                  &copy; 2024 FormFix. All rights
                                  reserved.
                                </div>
                              </div>
                            </body>
                            </html>
                    """.trimIndent(), "text/html; charset=utf-8")
                }

                Transport.send(message)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }


        private fun forgotEmail(recipientEmail: String, otp: String, name:String): Boolean {
            val props = Properties().apply {
                put("mail.smtp.ssl.enable", "true")
                put("mail.smtp.port", PORT)
                put("mail.smtp.auth", "true")
                put("mail.smtp.host", HOST)
                put("mail.smtp.socketFactory.port", PORT)
                put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
            }

            return try {
                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication() = PasswordAuthentication(EMAIL, PASSWORD)
                })

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(EMAIL))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
                    subject = "Reset Your FormFix Password"
                    setContent("""
                        <!DOCTYPE html>
                            <html lang="en">
                            <head>
                              <meta charset="UTF-8" />
                              <title></title>
                              <style>
                                body {
                                  margin: 0;
                                  padding: 0;
                                  font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
                                  color: #333;
                                  background-color: #fff;
                                }
                            
                                .container {
                                  margin: 0 auto;
                                  width: 100%;
                                  max-width: 600px;
                                  padding: 0 0px;
                                  border-radius: 5px;
                                  line-height: 1.8;
                                }
                            
                                .header {
                                  border-bottom: 1px solid #dfdfdf;
                                  text-align: center;
                                }
                            
                                .header a {
                                  font-size: 1.4em;
                                  color: black;
                                  text-decoration: none;
                                  font-weight: 600;
                                }
                            
                                .content {
                                  min-width: 700px;
                                  overflow: auto;
                                  line-height: 2;
                                }
                            
                                .otp {
                                  background: linear-gradient(to right, #28405C 0, #3C628F 50%, #5187C1 100%);
                                  margin: 0 auto;
                                  width: max-content;
                                  padding: 0 10px;
                                  color: #fff;
                                  border-radius: 4px;
                                  letter-spacing: 3px;
                                }
                            
                                .footer {
                                  color: #aaa;
                                  font-size: 0.8em;
                                  line-height: 1;
                                  font-weight: 300;
                                }
                            
                                .email-info {
                                  color: #666666;
                                  font-weight: 400;
                                  font-size: 13px;
                                  line-height: 18px;
                                  padding-bottom: 6px;
                                }
                            
                                .email-info a {
                                  text-decoration: none;
                                  color: #5187C1;
                                }
                              </style>
                            </head>
                            
                            <body>
                              <div class="container">
                                <div class="header">
                                  <a>FormFix</a>
                                </div>
                                <br />
                                <strong>Dear $name,</strong>
                                <p>
                                  We have received a request to reset the password for your FormFix account. For
                                  security purposes, please verify your identity by providing the
                                  following One-Time Password (OTP).
                                  <br />
                                  <b>Your One-Time Password (OTP) verification code is:</b>
                                </p>
                                <h2 class="otp">$otp</h2>
                                <p style="font-size: 0.9em">
                                  <strong>One-Time Password (OTP) is valid for 5 minutes.</strong>
                                  <br />
                                  <br />
                                  If you did not request to reset your password, you can safely ignore this email
                                  . Please ensure the confidentiality of your OTP and avoid sharing it with
                                  anyone.<br />
                                  <strong>Do not forward or give this code to anyone.</strong>
                                  <br />
                                  <br />
                                  <strong>Thank you for using FormFix.</strong>
                                  <br />
                                  <br />
                                  Best regards,
                                  <br />
                                  <strong>FormFix Team</strong>
                                </p>
                            
                                <hr style="border: none; border-top: 0.5px solid #131111" />
                                <div class="footer">
                                  <p>This email can't receive replies.</p>
                                  <p>
                                    For more information about FormFix, visit
                                    <strong>[WEBSITE LINK]</strong>
                                  </p>
                                </div>
                              </div>
                              <div style="text-align: center">
                                <div class="email-info">
                                  <span>
                                    This email was sent to
                                    <a href="mailto:{Email Adress}">$recipientEmail</a>
                                  </span>
                                </div>
                            
                                <div class="email-info">
                                  &copy; 2024 FormFix. All rights
                                  reserved.
                                </div>
                              </div>
                            </body>
                            </html>
                    """.trimIndent(), "text/html; charset=utf-8")
                }

                Transport.send(message)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        fun sendReactivationEmail(recipientEmail: String, otp: String, name: String, callback: (Boolean) -> Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                val success = sendReactivationEmailImpl(recipientEmail, otp, name)
                withContext(Dispatchers.Main) {
                    callback(success)
                }
            }
        }

        private fun sendReactivationEmailImpl(recipientEmail: String, otp: String, name: String): Boolean {
            val props = Properties().apply {
                put("mail.smtp.ssl.enable", "true")
                put("mail.smtp.port", PORT)
                put("mail.smtp.auth", "true")
                put("mail.smtp.host", HOST)
                put("mail.smtp.socketFactory.port", PORT)
                put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
            }

            return try {
                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication() = PasswordAuthentication(EMAIL, PASSWORD)
                })

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(EMAIL))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
                    subject = "Reactivate Your FormFix Account"
                    setContent("""
                        <!DOCTYPE html>
                            <html lang="en">
                            <head>
                              <meta charset="UTF-8" />
                              <title></title>
                              <style>
                                body {
                                  margin: 0;
                                  padding: 0;
                                  font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
                                  color: #333;
                                  background-color: #fff;
                                }
                            
                                .container {
                                  margin: 0 auto;
                                  width: 100%;
                                  max-width: 600px;
                                  padding: 0 0px;
                                  border-radius: 5px;
                                  line-height: 1.8;
                                }
                            
                                .header {
                                  border-bottom: 1px solid #dfdfdf;
                                  text-align: center;
                                }
                            
                                .header a {
                                  font-size: 1.4em;
                                  color: black;
                                  text-decoration: none;
                                  font-weight: 600;
                                }
                            
                                .content {
                                  min-width: 700px;
                                  overflow: auto;
                                  line-height: 2;
                                }
                            
                                .otp {
                                  background: linear-gradient(to right, #28405C 0, #3C628F 50%, #5187C1 100%);
                                  margin: 0 auto;
                                  width: max-content;
                                  padding: 0 10px;
                                  color: #fff;
                                  border-radius: 4px;
                                  letter-spacing: 3px;
                                }
                            
                                .footer {
                                  color: #aaa;
                                  font-size: 0.8em;
                                  line-height: 1;
                                  font-weight: 300;
                                }
                            
                                .email-info {
                                  color: #666666;
                                  font-weight: 400;
                                  font-size: 13px;
                                  line-height: 18px;
                                  padding-bottom: 6px;
                                }
                            
                                .email-info a {
                                  text-decoration: none;
                                  color: #5187C1;
                                }
                              </style>
                            </head>
                            
                            <body>
                              <div class="container">
                                <div class="header">
                                  <a>FormFix</a>
                                </div>
                                <br />
                                <strong>Dear $name,</strong>
                                <p>
                                  We have received a request to reactivate your FormFix account. 
                                  To confirm your identity and proceed with the reactivation, 
                                  please use the following One-Time Password (OTP).
                                  <br />
                                  <b>Your One-Time Password (OTP) verification code is:</b>
                                </p>
                                <h2 class="otp">$otp</h2>
                                <p style="font-size: 0.9em">
                                  <strong>One-Time Password (OTP) is valid for 5 minutes.</strong>
                                  <br />
                                  <br />
                                  If you did not request to reactivate your account, 
                                  please disregard this email. For your security, 
                                  ensure the confidentiality of your OTP and do not share it with anyone.
                                  <br />
                                  <strong>Do not forward or give this code to anyone.</strong>
                                  <br />
                                  <br />
                                  <strong>Thank you for choosing FormFix.</strong>
                                  <br />
                                  <br />
                                  Best regards,
                                  <br />
                                  <strong>FormFix Team</strong>
                                </p>
                            
                                <hr style="border: none; border-top: 0.5px solid #131111" />
                                <div class="footer">
                                  <p>This email can't receive replies.</p>
                                  <p>
                                    For more information about FormFix, visit
                                    <strong>[WEBSITE LINK]</strong>
                                  </p>
                                </div>
                              </div>
                              <div style="text-align: center">
                                <div class="email-info">
                                  <span>
                                    This email was sent to
                                    <a href="mailto:{Email Adress}">$recipientEmail</a>
                                  </span>
                                </div>
                            
                                <div class="email-info">
                                  &copy; 2024 FormFix. All rights
                                  reserved.
                                </div>
                              </div>
                            </body>
                            </html>
                    """.trimIndent(), "text/html; charset=utf-8")
                }

                Transport.send(message)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    }


}