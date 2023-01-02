package be.lennertsoffers.supportportalapplication.constant;

import org.springframework.beans.factory.annotation.Value;

public class EmailConstant {
    public static final String SIMPLE_MAIL_TRANSFER_PROTOCOL = "smtps";
    @Value("${mail.username}")
    public static final String USERNAME = "lennertsoffers1@gmail.com";
    @Value("${mail.password}")
    public static final String PASSWORD = null;
    public static final String FROM_EMAIL = "lennertsoffers1@gmail.com";
    public static final String CC_EMAIL = "";
    public static final String EMAIL_SUBJECT = "Get Arrays, LLC - New Password";
    public static final String GMAIL_SMTP_SERVER = "smtp.gmail.com";
    public static final String SMTP_HOST = "mail.smtp.host";
    public static final String SMTP_AUTH = "mail.smtp.auth";
    public static final String SMTP_PORT = "mail.smtp.port";
    public static final int DEFAULT_PORT = 465;
    public static final String SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";
    public static final String SMTP_STARTTLS_REQUIRED = "mail.smtp.starttls.required";
}
