package me.potato.demohystrixcommand;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.stereotype.Service;

@Service
public class SomeService {


	@HystrixCommand(fallbackMethod = "fallback")
	public String normal() throws Exception {
		throw new Exception("An exception occured");
	}

	private String fallback(){
		return "fallback";
	}
}
