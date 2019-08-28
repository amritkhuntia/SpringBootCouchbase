package com.amrit.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.amrit.beans.User;
import com.amrit.service.UserService;

public class CustomAuthenticationProvider  implements AuthenticationProvider {
	private static final Logger logger = Logger.getLogger(CustomAuthenticationProvider.class);

	public CustomAuthenticationProvider() {
	    logger.info("---CustomAuthenticationProvider created---");
	}
	
	@Autowired
	UserService userService;
	
	 User user =null;

	
	@Override
	public Authentication authenticate(Authentication auth) throws AuthenticationException {
		if(auth!=null){
	          user = userService.findUserByUsername(auth.getName());

			if(user!=null && user.getUsername().equals(auth.getName()) &&
					auth.getCredentials().equals(user.getPassword())){
				 List<GrantedAuthority> grantedAuths = new ArrayList<>();
			        grantedAuths.add(new SimpleGrantedAuthority("ROLE_USER"));
			        
			        return new UsernamePasswordAuthenticationToken(auth.getName(), auth.getCredentials(), grantedAuths);
			}
			else  if(user!=null && user.getUsername().equals("admin") &&
					auth.getCredentials().equals("admin")){
				List<GrantedAuthority> grantedAuths = new ArrayList<>();
		        grantedAuths.add(new SimpleGrantedAuthority("ROLE_USER"));
		        grantedAuths.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
		        
		        return new UsernamePasswordAuthenticationToken(auth.getName(), auth.getCredentials(), grantedAuths);
			}
				return null;
			
		}
		return null;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return true;
	}

}
