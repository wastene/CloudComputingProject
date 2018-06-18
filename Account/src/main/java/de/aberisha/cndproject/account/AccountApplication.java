package de.aberisha.cndproject.account;


import java.util.Arrays;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RestController
@SpringBootApplication
public class AccountApplication {

	@Autowired
	private UserRepository repository;
	
	@PostMapping(value="/login", consumes="application/json", produces="application/json")
	public ResponseEntity<?> login(@RequestBody final String body, @RequestHeader HttpHeaders requestHeader) {
		
		final JsonParser parser = new JsonParser();
		final JsonElement el = parser.parse(body);
		if(el.isJsonObject() == false) {
			// Bad Request need Json
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		final JsonObject obj = el.getAsJsonObject();
		final String username = gsonGetString(obj.get("user"));
		final String passwordHash = gsonGetString(obj.get("pw"));
		
		if(username == "" || passwordHash == "") {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		Optional<User> optionalUser = repository.findById(username);
		if(optionalUser.isPresent() == false) {
			return errorResponse("User not exists", HttpStatus.UNAUTHORIZED);
		}
		User user = optionalUser.get();
		if(user.getPasswordHash().equals(passwordHash) == false) {
			// Password incorrect
			return errorResponse("Password not correct", HttpStatus.UNAUTHORIZED);
		}
		
		// Password correct
		/*
		List<String> cookies = requestHeader.get("Cookie");
		if(cookies != null) {
			for(String cookie : cookies) {
				if(cookies.contains("JSESSIONID")) {
					
				}
			}
		}
		*/
		return ResponseEntity.status(HttpStatus.OK).build();
	}
	
	@PostMapping(value="/register", consumes="application/json", produces="application/json")
	public ResponseEntity<?> register(@RequestBody final String body){
		
		final JsonParser parser = new JsonParser();
		final JsonElement el = parser.parse(body);
		if(el.isJsonObject() == false) {
			// Bad Request need Json
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		final JsonObject obj = el.getAsJsonObject();
		final String username = gsonGetString(obj.get("user"));
		final String passwordHash = gsonGetString(obj.get("pw"));
		
		if(username == "" || passwordHash == "") {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		if(repository.findById(username).isPresent()) {
			// User already exists
			return errorResponse("User already exists.", HttpStatus.UNAUTHORIZED);
		}
		
		// User not exists - create User
		repository.save(new User(username, passwordHash));
		
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	private String gsonGetString(JsonElement elem) {
		if(elem == null) {
			return "";
		}
		if(elem.isJsonPrimitive() == false) {
			return "";
		}
		return elem.getAsString();
	}
	
	private ResponseEntity<String> errorResponse(final String errorMessage, final HttpStatus status) {

		JsonObject json = new JsonObject();
		json.addProperty("error", errorMessage);
		
		return new ResponseEntity<String>(json.toString(), status);
	}
	
	public static void main(String[] args) {
		SpringApplication.run(AccountApplication.class, args);
	}
    
}

@EnableWebSecurity
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.cors().and().csrf().disable();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080", "*"));
		configuration.setAllowedMethods(Arrays.asList("GET","POST"));
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}


