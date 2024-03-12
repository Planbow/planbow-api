package com.planbow.services;


import com.planbow.documents.planboard.Events;
import com.planbow.documents.planboard.Planboard;
import com.planbow.entities.user.UserEntity;
import com.planbow.utility.PlanbowUtility;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.planbow.utility.PlanbowUtility.extractNameFromEmail;
import static com.planbow.utility.PlanbowUtility.formatInstantToString;

@Service
@Log4j2
public class EmailService {

    private Configuration configuration;
    private JavaMailSender javaMailSender;

    @Autowired
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Autowired
    public void setJavaMailSender(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }


    @Async("asyncExecutor")
    public void planboardInvite(Planboard planboard, UserEntity owner, UserEntity userEntity, String role, Instant start) {
        log.info("Executing planboardInvite() method");
        configuration.setClassForTemplateLoading(this.getClass(), "/templates/");
        Map<String, Object> model = new HashMap<>();
        model.put("planboardName", planboard.getName());
        model.put("ownerName", owner.getName());
        model.put("role",role);
        model.put("userName", userEntity.getName());
        model.put("scheduleContent","");
       if(start!=null){
           String scheduleContent=
                   """
                                   %s has scheduled a meeting for this planboard
                                                       <br/><br/>
                                                       Title:  %s
                                                       <br/><br/>
                                                       Date :  %s
                           """.formatted(owner.getName(),planboard.getName(),formatInstantToString(start,"dd-MM-yyyy"));
           model.put("scheduleContent",scheduleContent);
       }

        model.put("verifyUrl", System.getenv("WEBSITE.URL")+"/invite?type=planboard&id="+planboard.getId());
        model.put("date", PlanbowUtility.formatDate(new Date()));
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        Template t = null;
        try {
            t = configuration.getTemplate("planboardInvites.ftl");
            String text = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);

            helper.setTo(userEntity.getEmail());
            helper.setFrom("no-reply@planbow.com");
            helper.setSubject("Planbow Invite - "+planboard.getName());
            helper.setText(text, true);
            javaMailSender.send(message);
        } catch (TemplateException | MessagingException | IOException e) {
            log.error("exception {}",e.getMessage());
        }
    }


    @Async("asyncExecutor")
    public void planboardInvite(Planboard planboard, UserEntity owner,String email,String role,Instant start) {
        log.info("Executing planboardInvite() method");
        configuration.setClassForTemplateLoading(this.getClass(), "/templates/");
        Map<String, Object> model = new HashMap<>();
        model.put("planboardName", planboard.getName());
        model.put("ownerName", owner.getName());
        model.put("role",role);

        String userName=extractNameFromEmail(email);
        model.put("userName",userName==null? " User": userName);

        model.put("scheduleContent","");
        if(start!=null){
            String scheduleContent=
                    """
                                    %s has scheduled a meeting for this planboard
                                                        <br/><br/>
                                                        Title:  %s
                                                        <br/><br/>
                                                        Date :  %s
                            """.formatted(owner.getName(),planboard.getName(),formatInstantToString(start,"dd-MM-yyyy"));
            model.put("scheduleContent",scheduleContent);
        }

        model.put("verifyUrl", System.getenv("WEBSITE.URL")+"/invite?type=planboard&id="+planboard.getId());
        model.put("date", PlanbowUtility.formatDate(new Date()));
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        Template t = null;
        try {
            t = configuration.getTemplate("planboardInvites.ftl");
            String text = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);

            helper.setTo(email);
            helper.setFrom("no-reply@planbow.com");
            helper.setSubject("Planbow Invite - "+planboard.getName());
            helper.setText(text, true);
            javaMailSender.send(message);
        } catch (TemplateException | MessagingException | IOException e) {
            log.error("exception {}",e.getMessage());
        }
    }


}
