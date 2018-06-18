package de.aberisha.cndproject.account;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class User {
	
	@Id
	private String username;
	
	private String passwordHash;
	
	private String cookie;
	
	public User() {
	}
	
	public User(final String username, final String passwordHash) {
		this.username = username;
		this.passwordHash = passwordHash;
		this.cookie = null;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public String getPasswordHash() {
		return this.passwordHash;
	}
	
	public String getCookie() {
		return this.cookie;
	}
	
	public void setCookie(String cookie) {
		this.cookie = cookie;
	}
	
}
