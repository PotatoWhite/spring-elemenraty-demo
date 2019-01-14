# Service-Configurer
- 설정 파일을 Git Repository를 이용하여 저장관리/제공 하는 설정 서비스 제공 Application

## Project 생성
1. spring boot initializer를 통해 프로젝트를 생성한다.
2. openjdk 11 을 사용한다.


## Dependency
1. Dependencies
 - config server, eureka client
   
## Resource
1. application.properties - non active profile
    ~~~properties
    server.port=8888
    spring.application.name=service-configurer
    spring.cloud.config.server.git.uri=https://github.com/PotatoWhite/spring-elementary-demo-config.git
    
    eureka.client.service-url.default-zone=http://localhost:8761/eurekaa~~~
    ~~~
    
## Package 구성
1. 없음

## Git Repository 생성
- Github에 "spring-elementary-demo-config" 를 생성
- Local 환경에 git 동기화를 맞춘다.
    ~~~sh
    > cd spring-elementary-demo-config
    > git init
    > git remote add origin https://github.com/PotatoWhite/spring-elementary-demo-config.git
    > echo "config repository" >> readme.md
    > git add .
    > git commit -am"init"
    > git push -u origin master
    ~~~


## Application 생성
- Git Repository를 하나 생성해야 한다.
- @EnableConfigServer 를 추가 한다.

    ~~~java
    package me.potato.demo.serviceconfigurer;
    
    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.SpringBootApplication;
    import org.springframework.cloud.config.server.EnableConfigServer;
    
    @SpringBootApplication
    @EnableConfigServer
    public class Application {
    
        public static void main(String[] args) {
            SpringApplication.run(Application.class, args);
        }
    
    }
    ~~~
