package com.example.demo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.example.demo.security.SecurityUtility.*;
import static org.springframework.http.HttpMethod.GET;

@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	private final UserDetailsService userDetailsService;
	private final ObjectMapper objectMapper;
	private final Http401UnauthorizedEntryPoint http401UnauthorizedEntryPoint;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(authenticationManagerBean(), objectMapper);
		customAuthenticationFilter.setFilterProcessesUrl(LOGIN_PATH);
		CustomAuthorizationFilter customAuthorizationFilter = new CustomAuthorizationFilter(userDetailsService);
		
		//@formatter:off
		http
				.cors().and()
				.csrf().disable()
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
				.authorizeRequests()
					.antMatchers(LOGIN_PATH).permitAll()
					.antMatchers("/token/refresh/**").permitAll()
					.antMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**").permitAll()
					.antMatchers(GET, CATEGORIES_PATH).permitAll()
					.antMatchers(GET, CATEGORIES_PATH + "/**").permitAll()
					//.antMatchers(PUT, CATEGORIES_PATH + "/**").hasAnyRole(MODERATOR, ADMIN)
					.anyRequest().authenticated().and()
				.addFilter(customAuthenticationFilter)
				.addFilterBefore(customAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
				.exceptionHandling().authenticationEntryPoint(http401UnauthorizedEntryPoint);
		//@formatter:off
		//http.addFilterAfter(new AccessDeniedExceptionFilter(), customAuthorizationFilter.getClass());
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
	}
	
	@Override
	public void configure(WebSecurity web) {
		web.ignoring().mvcMatchers("/swagger-ui.html/**", "/configuration/**", "/swagger-resources/**", "/v3/api-docs", "/webjars/**");
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
	
	/*@Bean
	public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
		return new Jackson2ObjectMapperBuilder().serializers(
				new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
				.serializationInclusion(JsonInclude.Include.NON_NULL);
	}
	*/
	/*@Bean
	@Primary
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
		mapper.addMixIn(Object.class, IgnoreHibernatePropertiesInJackson.class);
		return mapper;
	}
	
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	private abstract static class IgnoreHibernatePropertiesInJackson{ }*/
}
