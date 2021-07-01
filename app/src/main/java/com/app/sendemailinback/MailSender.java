package com.app.sendemailinback;

import java.security.Security;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailSender extends javax.mail.Authenticator {
    private String user;
    private String password;
    private Session session;

    static {
        Security.addProvider(new JSSEProvider());
    }

    public MailSender(String user, String password) {
        this.user = user; //Your SMTP username. In case of GMail SMTP this is going to be your GMail email address.
        this.password = password; //Your SMTP password. In case of GMail SMTP this is going to be your GMail password.

        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        //Hostname of the SMTP mail server which you want to connect for sending emails.
        props.setProperty("mail.host", "smtp.gmail.com");
        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", false);
        props.setProperty("mail.smtp.quitwait", "false");
        props.put("mail.smtp.ssl.enable", true);

        session = Session.getDefaultInstance(props, this);
    }

    protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
        return new javax.mail.PasswordAuthentication(user, password);
    }

    public synchronized void sendMail(String subject, String body,
                                      String sender, String recipients) throws Exception {
        try{
            MimeMessage message = new MimeMessage(session);
            DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/plain"));
            message.setFrom(new InternetAddress(sender,Utils.FROM_DISPLAY_NAME));//Sender Mail Address and Personal Name
            message.setSubject(subject);
            message.setDataHandler(handler);

            if (recipients.indexOf(',') > 0)
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
            else
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients,Utils.TO_DISPLAY_NAME));//Recipient Mail Address and Personal Name

            Transport.send(message);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
