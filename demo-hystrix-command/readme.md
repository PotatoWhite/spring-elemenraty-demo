# Demo-hystrix-command
- 간단한 Circuit Breaker의 동작을 살펴볼 수 있는 예제


## Project 생성
1. spring boot initializer를 통해 프로젝트를 생성한다.
2. openjdk 11 을 사용한다.


## Dependency
1. Dependencies
 - hystrix client(no Web)

   
## Resource
1. 없음

## Package 구성
1. 없음

## Application Runner 생성
- simple-rest-server 로 부터 수신될 simple Payload
    ~~~java
    package me.potato.demohystrixcommand;

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.CommandLineRunner;
    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.SpringBootApplication;
    import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
    import org.springframework.stereotype.Component;

    @Component
    @EnableCircuitBreaker
    @SpringBootApplication
    public class Application implements CommandLineRunner {

        @Autowired
        SomeService  someService;

        public static void main(String[] args) {
            SpringApplication.run(Application.class, args);
        }


        @Override
        public void run(String... args) throws Exception {
            System.out.println(someService.normal());
        }


    }
   ~~~

## SomeService 생성 - HystrixCommand가 동작할 객체
- normal Method의 Exception이 발생하는 경우 fallback이 호출 된다.
- normal과 fallback method는 type과 input Param이 동일해야한다.
- fallback은 추가적으로 normal에서 발생한 exception을 Throwable type으로 전달 받을 수 있다.

    ~~~java
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
    ~~~
