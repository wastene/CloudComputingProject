package de.aberisha.cndproject.webservice;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonObject;

@EnableDiscoveryClient
@RestController
@SpringBootApplication
public class WebServiceApplication {

	@Autowired
	private Environment env;
	
	@Autowired
	private DiscoveryClient client;
	
	@PostMapping(value="/proxy/login", consumes="application/json", produces="application/json")
	public ResponseEntity<?> proxyLogin(@RequestHeader HttpHeaders headers, @RequestBody String body){
		
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setOutputStreaming(false);
		
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new ErrorHandler());
		restTemplate.setRequestFactory(factory);
		
		String url = getAccountURI() + "/login";
		
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		HttpEntity<String> entity = new HttpEntity<String>(body, headers);
		
		ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
		return response;
	}
	
	@PostMapping(value="/proxy/register", consumes="application/json", produces="application/json")
	public ResponseEntity<String> proxyRegister(@RequestHeader HttpHeaders headers, @RequestBody String body){
		
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setOutputStreaming(false);
		
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new ErrorHandler());
		restTemplate.setRequestFactory(factory);

		String url = getAccountURI() + "/register";
		
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		HttpEntity<String> entity = new HttpEntity<String>(body, headers);
		
		ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
		return response;
	}
	
	/*
	@PostMapping(value="/login", consumes="application/json", produces="application/json")
	public ResponseEntity<?> login(@RequestBody final String body){
		String host = env.getProperty("server.account.host");
		String port = env.getProperty("server.account.port");
		if(host == null || port == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		RestTemplate restTemplate = new RestTemplate();
		String url = "http://"+host+":"+port+"/login";
		System.out.println("URL: "+url);
		
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		HttpEntity<String> entity = new HttpEntity<String>(body, headers);
		
		try {
			System.out.println(restTemplate.exchange(url, HttpMethod.POST, entity, String.class));
			ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
			System.out.println(response.getStatusCode()+"-"+response.getBody());
			return response;
		}catch(RestClientException e) {
			e.printStackTrace();
		}
		
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		
	}
	
	@PostMapping(value="/register", consumes="application/json", produces="application/json")
	public ResponseEntity<?> register(@RequestBody final String body){
		String host = env.getProperty("server.account.host");
		String port = env.getProperty("server.account.port");
		if(host == null || port == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		HttpEntity<String> entity = new HttpEntity<String>(body, headers);
		
		RestTemplate restTemplate = new RestTemplate();
		String url = "http://"+host+":"+port+"/register";
		
		return restTemplate.postForEntity(url, entity, String.class);
	}*/
	
	@GetMapping(value="/servers", produces="application/json")
	public String getServers() {
		
		String eurekaHost = env.getProperty("server.eureka.host");
		String eurekaPort = env.getProperty("server.eureka.port");
		if(eurekaHost == null) {
			eurekaHost = "";
		}
		if(eurekaPort == null) {
			eurekaPort = "";
		}
		
		JsonObject obj = new JsonObject();
		
		JsonObject eureka = new JsonObject();
		eureka.addProperty("host", eurekaHost);
		eureka.addProperty("port", eurekaPort);
		obj.add("eureka", eureka);
		
		return obj.toString();
	}
	
	// Return ServiceInstances of all Sensors
	@GetMapping(value="/sensors", produces="application/json")
	public ResponseEntity<?> getSensors(@RequestHeader HttpHeaders headers) {
		List<String> serviceIds = client.getServices();
		if(serviceIds == null) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		List<ServiceInstance> instances = new ArrayList<>();
		for(String serviceId : serviceIds) {
			List<ServiceInstance> temp = client.getInstances(serviceId);
			if(temp != null) {
				instances.addAll(temp);
			}
		}
		
		return ResponseEntity.ok().body(instances);
	}
	
	private String getAccountURI() {
		String host = env.getProperty("server.account.host");
		String port = env.getProperty("server.account.port");
		
		return "http://"+host+":"+port;
	}
	
	public static void main(String[] args) {
		SpringApplication.run(WebServiceApplication.class, args);
	}
}

// Creating own empty ErrorHandler, because Standard one produces error
// if response with 4xx comes
class ErrorHandler implements ResponseErrorHandler {
	@Override
	public boolean hasError(ClientHttpResponse response) throws IOException {
		return false;
	}

	@Override
	public void handleError(ClientHttpResponse response) throws IOException {
	}
}