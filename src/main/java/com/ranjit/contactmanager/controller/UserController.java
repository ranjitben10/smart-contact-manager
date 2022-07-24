package com.ranjit.contactmanager.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ranjit.contactmanager.dao.ContactRepository;
import com.ranjit.contactmanager.dao.UserRepository;
import com.ranjit.contactmanager.entities.Contact;
import com.ranjit.contactmanager.entities.User;
import com.ranjit.contactmanager.helper.Message;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@ModelAttribute
	public void addCommonData(Model m, Principal principal) {
		String userName = principal.getName();
		
		User user = userRepository.getUserByUserName(userName);
		
		m.addAttribute("user",user);
	}
	
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
	}
	
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		
		
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		
		return "normal/add_contact_form";
	}
	
	@PostMapping("/process-contact")
	public String processContact(@Valid @ModelAttribute("contact") Contact contact,BindingResult result,@RequestParam("profileImage") MultipartFile multipartFile,Principal principal,Model model, HttpSession session) {
		try {
			if(result.hasErrors()) {
				System.out.println("Error!"+ result.toString());
				model.addAttribute("contact",contact);
				return "/normal/add_contact_form";
			}
		
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		
		
		
		if(multipartFile.isEmpty()) {
			System.out.print("Empty File !");
			contact.setImage("contact.png");
		}else {
			contact.setImage(multipartFile.getOriginalFilename());
			File saveFile = new ClassPathResource("static/image").getFile();
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+multipartFile.getOriginalFilename());
			Files.copy(multipartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
		}
		
		contact.setUser(user);
		user.getContacts().add(contact);
		
		
		
		this.userRepository.save(user);
		model.addAttribute("contact",new Contact());
		System.out.print(contact);
		session.setAttribute("message", new Message("Successfully Added Contact!", "alert-success"));
		return "normal/add_contact_form";
		}catch(Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Contact Not Added!", "alert-danger"));
			model.addAttribute("contact",contact);
			return "login-fail";
		}
		
	}
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page ,Model model, Principal principal) {
		model.addAttribute("title"," Smart Contact Manager - User Contacts");
		
		String userName = principal.getName();
		
		User user = this.userRepository.getUserByUserName(userName);
		
		//List<Contact> contacts= user.getContacts(); 
		
		Pageable pageable = PageRequest.of(page, 6);
		
		Page<Contact> contacts  = this.contactRepository.findContactsByUser(user.getId(), pageable);
		
		model.addAttribute("contacts",contacts);
		model.addAttribute("currentPage",page);
		
		model.addAttribute("totalPages",contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	
	@GetMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId() == contact.getUser().getId()) {
			
			model.addAttribute("contact",contact);
			model.addAttribute("title",contact.getName());
		}
		
		
		return "/normal/contact_detail";
	}
	
	
	
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId, Model model, Principal principal, HttpSession httpSession) {
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		if(contact.getUser().getId()==user.getId()) {
			
			
			//contact.setUser(null);
			
			
			user.getContacts().remove(contact);
			this.userRepository.save(user);
			
			
			//this.contactRepository.delete(contact);
			
			httpSession.setAttribute("message", new Message("Contact Deleted Successfully...!","alert-success"));
			return "redirect:/user/show-contacts/0";
		}
		
		return "login-fail";
		
	}
	
	@PostMapping("/update-contact/{cid}")
	public String updateContact(@PathVariable("cid") Integer cId,Model model) {
		
		model.addAttribute("title","Update Contact");
		
		Contact contact = this.contactRepository.findById(cId).get();
		
		model.addAttribute("contact",contact);
		
		return "normal/update_contact";
	}
	
	
	@RequestMapping(value = "/process-contact-update", method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile multipartFile, Model model,HttpSession httpSession, Principal principal) {
		
		try {
			System.out.println(contact.getcId());
			Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();
			
			if(!multipartFile.isEmpty()) {
				
				
				File deleteOldFile = new ClassPathResource("static/image").getFile();
				File file1 = new File(deleteOldFile,oldContactDetail.getImage());
				file1.delete();
				
				
				File saveFile = new ClassPathResource("static/image").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+multipartFile.getOriginalFilename());
				Files.copy(multipartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(multipartFile.getOriginalFilename());
				
			}
			else {
				contact.setImage(oldContactDetail.getImage());
			}
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			httpSession.setAttribute("message", new Message("Your Contact Updated!","alert-success"));
		}catch(Exception e) {
			e.printStackTrace();
			System.out.print("error");
		}
		
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		
		model.addAttribute("title","Smart Contact Manager - Profile");
		
		return "normal/profile";
	}
	
	
	@GetMapping("/settings")
	public String openSettings(Model model) {
		model.addAttribute("title","Smart Contact Manager - Settings");

		return "normal/settings";
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPass") String oldPass, @RequestParam("newPass") String newPass, Principal principal, HttpSession httpSession) {
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		if(this.bCryptPasswordEncoder.matches(oldPass, user.getPassword())) {
			user.setPassword(this.bCryptPasswordEncoder.encode(newPass));
			this.userRepository.save(user);
			httpSession.setAttribute("message", new Message("Password Changed!","alert-success"));
		}
		else {
			httpSession.setAttribute("message", new Message("Old Password Not Matched!","alert-danger"));
			return "redirect:/user/settings";
		}
		
		
		return "redirect:/user/index";
	}
	
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String,Object> data) {
		int amount = Integer.parseInt(data.get("amount").toString());
		RazorpayClient razorpayClient = null;
		try {
			razorpayClient = new RazorpayClient("rzp_test", "mFTRj0T");
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("amount", amount*100);
			jsonObject.put("currency", "INR");
			jsonObject.put("receipt", "txn_123456");
			Order order = razorpayClient.Orders.create(jsonObject);
			System.out.println(order);
			return order.toString();
		} catch (RazorpayException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "Failed!";
		}
		
	}
	
}
