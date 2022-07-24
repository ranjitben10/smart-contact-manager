package com.ranjit.contactmanager.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ranjit.contactmanager.dao.UserRepository;
import com.ranjit.contactmanager.entities.EmailDetails;
import com.ranjit.contactmanager.entities.User;
import com.ranjit.contactmanager.helper.Message;
import com.ranjit.contactmanager.service.EmailService;

@Controller
public class ForgotController {
	private Random random = new Random(1000);
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private EmailService emailService;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@RequestMapping("/forgot")
	public String OpenEmailForm() {
		return "forgot_email_form";
	}
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email, HttpSession httpSession) {
		int otp = random.nextInt(999999);
		
		EmailDetails emailDetails = new EmailDetails();
		emailDetails.setRecipient(email);
		emailDetails.setSubject("OTP Verification For Contact Manager !!!");
		emailDetails.setMsgBody("OTP :   " + otp  + "   Your OTP !");
		
		boolean flag = this.emailService.sendSimpleMail(emailDetails);
		
		if(flag) {
			httpSession.setAttribute("otp", otp);
			httpSession.setAttribute("email", email);
			return "verify_otp";
			
		}else {
			httpSession.setAttribute("message", new Message("Check Your Mail!","alert-danger"));
			return "forgot_email_form";
		}
		
	}
	
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp, HttpSession httpSession){
		int storedOtp =(int) httpSession.getAttribute("otp");
		String storedEmail =(String) httpSession.getAttribute("email");
		if(storedOtp==otp) {
			User user = this.userRepository.getUserByUserName(storedEmail);
			if(user==null) {
				httpSession.setAttribute("message", new Message("User Not Exists!","alert-danger"));
				return "forgot_email_form";
				
			}else {
				
			}
			return "password_change_form";
		}else {
			httpSession.setAttribute("message", new Message("Wrong OTP!","alert-danger"));
			return "verify_otp";
		}

		
	}
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newPass") String NewPass, HttpSession httpSession) {
		
		String email =(String) httpSession.getAttribute("email");
		User user = this.userRepository.getUserByUserName(email);
		user.setPassword(this.bCryptPasswordEncoder.encode(NewPass));
		this.userRepository.save(user);
		httpSession.setAttribute("Message", new Message("Password Updated !","alert-success"));
		return "redirect:/signin?change=password changed";
	}
}
