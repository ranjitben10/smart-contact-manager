package com.ranjit.contactmanager.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ranjit.contactmanager.dao.UserRepository;
import com.ranjit.contactmanager.entities.Contact;
import com.ranjit.contactmanager.entities.User;
import com.ranjit.contactmanager.helper.Message;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private UserRepository userRepository;
	
	@RequestMapping("/")
	public String home(Model model) {
		
		model.addAttribute("title","Home - Smart Contact Manager");
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model model) {
		
		model.addAttribute("title","About - Smart Contact Manager");
		return "about";
	}
	
	@RequestMapping("/signup")
	public String signUp(Model model) {
		
		model.addAttribute("title","Register - Smart Contact Manager");
		model.addAttribute("user",new User());
		return "signup";
	}
	
	@RequestMapping(value="/do_register", method=RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result, @RequestParam(value="agreement",defaultValue = "false") boolean agreement, Model model,HttpSession session) {
		
		try {
			if(!agreement) {
				System.out.println("You have not agreed Terms!");
				throw new Exception("You have not agreed Terms!");
			}
			
			if(result.hasErrors()) {
				System.out.println("Error!"+ result.toString());
				model.addAttribute("user",user);
				return "signup";
			}
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
			
			System.out.println(agreement);
			this.userRepository.save(user);
			
			
			session.setAttribute("message", new Message("Successfully Registered!", "alert-success"));
			model.addAttribute("user",new User());
			return "signup";
		}catch(Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something Went Wrong!!" + e.getMessage(), "alert-danger"));
			return "signup";
		}
		
	}
	
	@GetMapping("/signin")
	public String customLogin(Model model) {
		
		model.addAttribute("title","Signin - Smart Contact Manager");
		return "login";
	}
	
	@GetMapping("/login-fail")
	public String loginFail(Model model) {
		
		model.addAttribute("title","Error! - Smart Contact Manager");
		return "login-fail";
	}
}
