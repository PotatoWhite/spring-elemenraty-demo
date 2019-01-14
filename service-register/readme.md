# Service-register
 - Eureka를 이용한 Name Server를 구축함
 - Demo는 Standalone 이지만 상용서비스시 point of Failure의 risk가 커 Cluster로 운영행한다.


## Project 생성
1. spring boot initializer를 통해 프로젝트를 생성한다.
2. openjdk 11 을 사용한다.


## Dependency
1. Dependencies
 - Eureka Server

2. 기타 - 왜인지 모르겠지만 Spring boot 2.0 이상에서는 별도의 xml 관련 Lib이 필요함
    ~~~xml
    <dependency>
        <groupId>com.sun.xml.ws</groupId>
        <artifactId>jaxws-rt</artifactId>
        <version>2.3.1</version>
    </dependency>
    ~~~
   
## Resource
1. application.properties
    ~~~conf
    server.port=8761
    spring.application.name=service-register
    eureka.client.fetch-registry=false
    ~~~

## Package 구성
1. 없음


## Application 에 Annotation을 이용한 설정
1. @EnableEurekaServer 를 추가 함

    ~~~java
    package me.potato.serviceregister;

    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.SpringBootApplication;
    import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

    @EnableEurekaServer
    @SpringBootApplication
    public class Application {

        public static void main(String[] args) {
            SpringApplication.run(Application.class, args);
        }
    }
    ~~~

---
---

# 참고 : Eureka Client 설정
1. Dependency
  - Eureka client

    ~~~xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    ~~~
    
## Resource
1. application.properties
    ~~~conf
    spring.application.name=ribbon-eureka-client
    eureka.client.service-url.default-zone=http://localhost:8761/eureka
    ~~~

## Application 생성시 Annotation 추가
1. @EnableEurekaClient 를 추가 함

    ~~~java
    @SpringBootApplication
    @EnableEurekaClient
    public class Application {

        public static void main(String[] args) {
            SpringApplication.run(Application.class, args);
        }
    }
    ~~~
