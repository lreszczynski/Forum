package com.example.demo.security;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import java.security.Key;

@Slf4j
public class SecurityUtility {
	//@Value("${app.token.secret}")
	public static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
	public static final String LOGIN_PATH = "/login";
	public static final String CATEGORIES_PATH = "/categories/";
	public static final String ROLES_PATH = "/roles/";
	public static final String USERS_PATH = "/users/";
	public static final String THREADS_PATH = "/threads/";
	public static final String POSTS_PATH = "/posts/";
	public static final String TOKEN_REFRESH_PATH = "/token/refresh/";
	
}
