package com.ranjit.contactmanager.service;

import org.springframework.stereotype.Service;

import com.ranjit.contactmanager.entities.EmailDetails;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;


//Annotation
@Service
//Class
//Implementing EmailService interface
public class EmailService {

	@Autowired private JavaMailSender javaMailSender;

	@Value("${spring.mail.username}") private String sender;

	// Method 1
	// To send a simple email
	public boolean sendSimpleMail(EmailDetails details)
	{

		// Try block to check for exceptions
		try {

			// Creating a simple mail message
			SimpleMailMessage mailMessage
				= new SimpleMailMessage();

			// Setting up necessary details
			mailMessage.setFrom(sender);
			mailMessage.setTo(details.getRecipient());
			mailMessage.setText(details.getMsgBody());
			mailMessage.setSubject(details.getSubject());

			// Sending the mail
			javaMailSender.send(mailMessage);
			return true;
		}

		// Catch block to handle the exceptions
		catch (Exception e) {
			return false;
		}
	}


}

