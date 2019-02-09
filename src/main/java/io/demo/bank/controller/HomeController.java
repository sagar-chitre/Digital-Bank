package io.demo.bank.controller;

import io.demo.bank.model.UserProfile;
import io.demo.bank.model.security.Users;
import io.demo.bank.service.UserService;
import io.demo.bank.util.Constants;
import java.security.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class HomeController {
	
	private static final Logger LOG = LoggerFactory.getLogger(HomeController.class);
	
	// model attribute constants
	private static final String MODEL_ATT_USER 					= "user";
	private static final String MODEL_ATT_USER_PROFILE 			= "userProfile";
	private static final String MODEL_ATT_FIRST_NAME 			= "firstName";
	private static final String MODEL_ATT_ERROR_MSG				= "errorMsg";
	private static final String MODEL_ATT_AVATAR				= "avatar";
	private static final String MODEL_VAL_AVATAR_MALE			= "/images/admin.jpg";
	private static final String MODEL_VAL_AVATAR_FEMALE			= "/images/avatar/5.jpg";
  
	@Autowired
	private UserService userService;
	
	private void setUserDisplayDefaults (Users user, Model model) {
		
		// Add name for Welcome header
		model.addAttribute(MODEL_ATT_FIRST_NAME, user.getUserProfile().getFirstName());
		
		// Choose male or female avatar
		if (user.getUserProfile().getGender().equals(Constants.GENDER_MALE)) {
			model.addAttribute(MODEL_ATT_AVATAR, MODEL_VAL_AVATAR_MALE);
		}else {
			model.addAttribute(MODEL_ATT_AVATAR, MODEL_VAL_AVATAR_FEMALE);
		}
		
	}
	
  
	@GetMapping(Constants.URI_ROOT)
	public String root() {
		
		return Constants.DIR_REDIRECT + Constants.URI_HOME;
	}
  
	@GetMapping(Constants.URI_LOGIN)
	public String login(Model model) {
		
		model.addAttribute(MODEL_ATT_USER, new Users());
    
		return Constants.VIEW_LOGIN;
	}
  
	@GetMapping(Constants.URI_SIGNUP)
	public String signup(Model model) {
		
		model.addAttribute(MODEL_ATT_USER, new Users());
		model.addAttribute(MODEL_ATT_USER_PROFILE, new UserProfile());
    
		return Constants.VIEW_SIGNUP;
	}
  
	@PostMapping(Constants.URI_SIGNUP)
	public String signup(Model model,
						 @ModelAttribute(MODEL_ATT_USER) Users newUser, 
						 @ModelAttribute(MODEL_ATT_USER_PROFILE) UserProfile newProfile) {
		
		boolean bError = false;
    
		// Set the email address to also be the username
		newUser.setUsername(newProfile.getEmailAddress());
    
	    LOG.debug("Signup POST begin: ");
	    LOG.debug("User: " + newUser);
	    LOG.debug("Profile: " + newProfile);
    
	    // Add user objects to the model
	    model.addAttribute(MODEL_ATT_USER, newUser);
	    model.addAttribute(MODEL_ATT_USER_PROFILE, newProfile);
	    
	    // If email already exists then return error
	    if (userService.checkEmailAdressExists(newProfile.getEmailAddress())) {
	    	
	    	// Return error
			model.addAttribute(MODEL_ATT_ERROR_MSG, "An account is already registered with the "
													+ "email address provided. Login with the existing"
													+ "account or provide another email address for registration.");
	    	bError = true;
	    }
    
	    // If SSN already exists then return an error
	    if (userService.checkSsnExists(newProfile.getSsn())) {
	    	
	    	// Return error
			model.addAttribute(MODEL_ATT_ERROR_MSG, "An account is already registered with the "
													+ "Social Security Number provided. Login with"
													+ " the existing account or provide another SSN for registration.");
	    	bError = true;
	    }
	    
	    LOG.debug("Signup POST End: ");
    
	    // if we have an error go back to sign up page
	    if (bError) {
	    	return Constants.VIEW_SIGNUP;
	    }
	    
	    return Constants.VIEW_REGISTER;
	}
  
	@GetMapping(Constants.URI_REGISTER)
	public String register(Model model) {
    
		// Since this a a registration process, add user object and send them to signup
		model.addAttribute(MODEL_ATT_USER, new Users());
		model.addAttribute(MODEL_ATT_USER_PROFILE, new UserProfile());
    
		return Constants.VIEW_SIGNUP;
	}
  
	@PostMapping(Constants.URI_REGISTER)
	public String register(Model model,
						   @ModelAttribute(MODEL_ATT_USER) Users newUser, 
						   @ModelAttribute(MODEL_ATT_USER_PROFILE) UserProfile newProfile) {
		
		newUser.setUserProfile(newProfile);
    
		LOG.debug("Registering new User: " + newUser);
    
		newUser = userService.createUser(newUser);
		model.addAttribute(MODEL_ATT_USER, newUser);
    
		LOG.debug("User Registered: " + newUser);
    
		return Constants.VIEW_LOGIN;
	}
  
	@GetMapping(Constants.URI_HOME)
	public String home(Principal principal, Model model) {
    
		Users user = userService.findByUsername(principal.getName());
		this.setUserDisplayDefaults(user, model);
    
		return Constants.VIEW_HOME;
	}
	
}