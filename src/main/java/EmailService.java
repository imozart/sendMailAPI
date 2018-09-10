import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;

public class EmailService {

    public static final String TRANSPORT_PROTOCOL = "mail.transport.protocol";
    public static final String SMTP_HOST = "mail.smtps.host";
    public static final String SMTP_AUTH_USER = "mail.smtps.user";
    public static final String SMTP_AUTH_PWD = "mail.smtps.password";
    public static final String SMTP_PORT = "mail.smtps.port";
    public static final String SMTP_AUTH = "mail.smtps.auth";
    public static final String SMTP_SSL_ENABLE = "mail.smtps.ssl.enable";
    public static final String DEBUG = "mail.debug";

    private void sendEmail(Email email) {
        Properties props = buildPropertiesEmail();
        Session session = Session.getDefaultInstance(props, null);
        session.setDebug(Boolean.valueOf(props.getProperty(DEBUG)));

        try {
            Message msg = this.buildEmailMessage(session, email);
            Transport transport = session.getTransport("smtps");
            transport.connect(props.getProperty(SMTP_HOST), props.getProperty(SMTP_AUTH_USER), props.getProperty(SMTP_AUTH_PWD));
            transport.sendMessage(msg, msg.getAllRecipients());
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Asynchronous
    public Future<?> send(Email email) {
        sendEmail(email);
        return new AsyncResult<Future>(null);
    }

    private Properties buildPropertiesEmail() {
        Properties properties = System.getProperties();
        properties.put(TRANSPORT_PROTOCOL, "smtps");
        properties.put(SMTP_HOST, "smtp.gmail.com");
        properties.put(SMTP_AUTH_USER, "email@gmail.com");
        properties.put(SMTP_AUTH_PWD, "*********");
        properties.put(SMTP_PORT, "465");
        properties.put(SMTP_AUTH, "true");
        properties.put(SMTP_SSL_ENABLE, "true");
        properties.put(DEBUG, "false");
        return properties;
    }

    private Message buildEmailMessage(Session session, Email email) throws MessagingException {
        Message msg = new MimeMessage(session);
        msg.setSubject(email.getSubject());
        this.addRecievers(msg, email);
        Multipart multipart = new MimeMultipart();
        this.addMessageBodyPart(multipart, email);
        this.addAttachments(multipart, email);
        msg.setContent(multipart);
        return msg;
    }

    private void addRecievers(Message msg, Email email) throws MessagingException {
        InternetAddress from = new InternetAddress(email.getFrom());
        msg.setFrom(from);

        InternetAddress[] to = this.getInternetAddresses(email.getTo());
        msg.setRecipients(Message.RecipientType.TO, to);

        InternetAddress[] cc = this.getInternetAddresses(email.getCc());
        msg.setRecipients(Message.RecipientType.CC, cc);

        InternetAddress[] bcc = this.getInternetAddresses(email.getBcc());
        msg.setRecipients(Message.RecipientType.BCC, bcc);

    }

    private void addMessageBodyPart(Multipart multipart, Email email) throws MessagingException {
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(email.getText(), email.getMimeType());
        multipart.addBodyPart(messageBodyPart);
    }

    private void addAttachments(Multipart multipart, Email email) throws MessagingException {
        List<Attachment> attachments = email.getAttachments();
        if (attachments != null && attachments.size() > 0) {
            for (Attachment attachment : attachments) {
                BodyPart attachmentBodyPart = new MimeBodyPart();
                String filename = attachment.getFilename();
                DataSource source = new ByteArrayDataSource(attachment.getData(),
                        attachment.getMimeType());
                attachmentBodyPart.setDataHandler(new DataHandler(source));
                attachmentBodyPart.setFileName(filename);
                multipart.addBodyPart(attachmentBodyPart);
            }
        }
    }

    private InternetAddress[] getInternetAddresses(List<String> addresses)
            throws AddressException {
        if (addresses == null || addresses.size() == 0) {
            return null;
        }
        InternetAddress[] iAddresses = new InternetAddress[addresses.size()];
        for (int i = 0; i < addresses.size(); i++) {
            iAddresses[i] = new InternetAddress(addresses.get(i));
        }
        return iAddresses;
    }
}