package com.ericsson.cifwk.tdm.application.email;

import com.google.common.annotations.VisibleForTesting;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.ericsson.cifwk.tdm.infrastructure.EmailConfiguration.DEFAULT_ENCODING;
import static org.springframework.ui.freemarker.FreeMarkerTemplateUtils.processTemplateIntoString;

@Service
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private Configuration freemarkerConfig;

    @Async
    public void sendAsync(MailMessageBean message) {
        sendSync(message);
    }

    @VisibleForTesting
    void sendSync(MailMessageBean message) {
        String subject = generateContent(message.getSubjTemplate(), message.getSubjectParams());
        String body = generateContent(message.getBodyTemplate(), message.getBodyParams());

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(message.getTo().stream().toArray(String[]::new));
        mailMessage.setCc(message.getCc());
        mailMessage.setSubject(subject);
        mailMessage.setText(body);
        mailSender.send(mailMessage);
    }

    private String generateContent(MsgTemplate templateName, Map<String, Object> context) {
        try {
            Template template = freemarkerConfig.getTemplate(templateName.getName(), DEFAULT_ENCODING);
            return processTemplateIntoString(template, context);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e);  // NOSONAR
        }
    }
}
