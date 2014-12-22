package com.hitsoft.email

object Email {
  implicit def stringToSeq(single: String): Seq[String] = Seq(single)

  implicit def liftToOption[T](t: T): Option[T] = Some(t)

  sealed abstract class MailType

  case object Plain extends MailType

  case object Rich extends MailType

  case object MultiPart extends MailType

  trait Config {
    val host: String
    val port: Int
    val user: Option[String]
    val password: Option[String]
  }

  trait Address {
    val email: String
    val name: String
  }

  case class Mail(
                    server: Config,
                   from: Address,
                   to: Seq[String],
                   cc: Seq[String] = Seq.empty,
                   bcc: Seq[String] = Seq.empty,
                   subject: String,
                   message: String,
                   richMessage: Option[String] = None,
                   attachment: Option[(java.io.File)] = None
                   )

  object send {
    def a(mail: Mail) {
      import org.apache.commons.mail._

      val format =
        if (mail.attachment.isDefined) MultiPart
        else if (mail.richMessage.isDefined) Rich
        else Plain

      val commonsMail: Email = format match {
        case Plain => new SimpleEmail().setMsg(mail.message)
        case Rich => new HtmlEmail().setHtmlMsg(mail.richMessage.get).setTextMsg(mail.message)
        case MultiPart => {
          val attachment = new EmailAttachment()
          attachment.setPath(mail.attachment.get.getAbsolutePath)
          attachment.setDisposition(EmailAttachment.ATTACHMENT)
          attachment.setName(mail.attachment.get.getName)
          new MultiPartEmail().attach(attachment).setMsg(mail.message)
        }
      }
      commonsMail.setCharset(EmailConstants.UTF_8)
      commonsMail.setSmtpPort(mail.server.port)
      commonsMail.setHostName(mail.server.host)

      if (mail.server.user.isDefined && mail.server.password.isDefined)
        commonsMail.setAuthentication(mail.server.user.get, mail.server.password.get)

      // Can't add these via fluent API because it produces exceptions
      mail.to foreach (commonsMail.addTo(_))
      mail.cc foreach (commonsMail.addCc(_))
      mail.bcc foreach (commonsMail.addBcc(_))

      commonsMail
        .setFrom(mail.from.email, mail.from.name)
        .setSubject(mail.subject)
        .send()
    }
  }
}
