package de.aberisha.cndproject.sensor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RestController
@EnableDiscoveryClient
@SpringBootApplication
public class SensorApplication {
	
	private List<Sensor> sensors = new ArrayList<Sensor>();
	
	public static void main(String[] args) {
		SpringApplication.run(SensorApplication.class, args);
	}
	
	
	@GetMapping("/sensors")
	public List<Sensor> getSensors() {
		return this.sensors;
	}
	
	@PostMapping(value="/sensors", consumes="application/json")
	public void addSensor(@RequestBody String body) {
		JsonParser parser = new JsonParser();
		JsonElement elem = parser.parse(body);
		
		if(elem == null) {
			return;
		}
		
		if(elem.isJsonObject()) {
			JsonObject obj = elem.getAsJsonObject();
			
			String name = gsonGetString(obj.get("name"));
			String unit = gsonGetString(obj.get("unit"));
			Sensor sensor = new Sensor(name, unit);
			new SensorThread(sensor).start();
			this.sensors.add(sensor);
		}
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
