package com.amrit.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;

public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
    private CustomAuthenticationProvider customAuthenticationProvider;

    @Autowired
    @Override
    protected void configure(AuthenticationManagerBuilder authManager) throws Exception {
        authManager.authenticationProvider(this.customAuthenticationProvider);
    }
	
	@Override
    protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
        .antMatchers("/resources/**", "/registration").permitAll()
        .anyRequest().authenticated()
        .and()
    .formLogin()
        .loginPage("/login")
        .permitAll()
        .and()
    .logout()
        .permitAll();
    }
	
	
	

}
