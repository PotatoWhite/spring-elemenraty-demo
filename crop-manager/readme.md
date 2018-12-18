# Crop-Manager
- Item:Crop을 CRUD 하는 관리성 API Server 이다.

1. 프로젝트 생성
2. Test001 - ap

## Project 생성
1. spring boot initializer를 통해 프로젝트를 생성한다.
2. openjdk 11 을 사용한다.


## Dependency
1. Dependencies
 - jpa, hateoas, web, config, mysql, h2, lombok, hystrix client
   
## Resource
1. application.properties - non active profile
    ~~~properties
    spring.application.name=crop-manager
    startup.message=Startup (non active profile)
    spring.jpa.show-sql=true

    hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds=3000
    ~~~

2. application-dev.properties - dev
    ~~~properties
    startup.message=Startup (Dev Env)

    spring.jpa.hibernate.ddl-auto=create-drop

    spring.datasource.url=jdbc:mysql://localhost/spring_demo
    spring.datasource.username=potato
    spring.datasource.password=potato123
    spring.datasource.driver-class-name=com.mysql.jdbc.Driver
    ~~~


## Package 구성
1. 패키지 구조를 만든다.
 - crop
 - exceptionhandler
 
## Application 생성
- ModelMapper를 Bean으로 등록한다.
- ModelMapper를 통해 DTO와 VO간의 변환을 수행한다.
- DTO는 Data Transfer Object로 실제 DB에서 받아온 Entity와 전송시 사용할 규격의 불일치를 대응할 수 있다.

    ~~~java
    package me.potato.farm.cropmanager;

    import lombok.extern.slf4j.Slf4j;
    import org.modelmapper.ModelMapper;
    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.SpringBootApplication;
    import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
    import org.springframework.context.annotation.Bean;

    @Slf4j
    @EnableCircuitBreaker
    @SpringBootApplication
    public class Application {

        public static void main(String[] args) {
            SpringApplication.run(Application.class, args);

        }

        @Bean
        public ModelMapper modelMapper(){
            return new ModelMapper();
        }

    }
    ~~~

## Crop Entity를 생성 
- "created"는 최초 등록시만 생성된다.
- "updated"는 매번 Update될 때 갱신된다.

    ~~~java
    package me.potato.farm.cropmanager.crop;

    import lombok.*;
    import org.hibernate.annotations.CreationTimestamp;
    import org.hibernate.annotations.UpdateTimestamp;

    import javax.persistence.*;
    import java.time.LocalDateTime;

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @ToString
    @Entity
    @EqualsAndHashCode(of = "id")
    public class Crop {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;


        private String name;
        private String className;



        @CreationTimestamp
        @Column(updatable = false, nullable = false)
        private LocalDateTime created;

        @UpdateTimestamp
        private LocalDateTime updated;


    }
    ~~~

## Crop DTO를 생성
- Network을 통해 외부로 전송된 Item의 규격이다. 
- created나 updated는 제외 되어있다.(필요하면 넣으면 된다.)

    ~~~java
    package me.potato.farm.cropmanager.crop;

    import com.fasterxml.jackson.databind.PropertyNamingStrategy;
    import com.fasterxml.jackson.databind.annotation.JsonNaming;
    import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.NoArgsConstructor;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public class CropDto {
        private Long id;
        private String name;
        private String className;
    }
    ~~~


## CropRepository 생성
1. Crop Respository

    ~~~java 
    package me.potato.farm.cropmanager.crop;

    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.data.jpa.repository.JpaRepository;

    public interface CropRepository extends JpaRepository<Crop, Long> {
        Page<Crop> findAll(Pageable pageable);

    }
    ~~~

## Service 생성
1. 실제 프로젝트에서 Controller에서 직접 Repository를 호출 하는 경우는 드물다.
2. Spring에서 service layer를 통해 구현하는 것을 권장한다.

    ~~~java
    package me.potato.farm.cropmanager.crop;

    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.stereotype.Service;

    import javax.validation.constraints.NotNull;
    import java.util.Optional;


    @Service
    public class CropService {

        private  final CropRepository cropRepo;

        public CropService(CropRepository cropRepo) {
            this.cropRepo = cropRepo;
        }

        public Optional<Crop> getCrop(Long id){
            return cropRepo.findById(id);
        }

        public Page<Crop> getAllCrops(Pageable pageinfo){
            return cropRepo.findAll(pageinfo);
        }

        public Crop saveCrop(Crop crop) {
            return cropRepo.saveAndFlush(crop);
        }

        public Optional<Crop> updateCrops(@NotNull Long id, Crop crop) {
            boolean byId = cropRepo.existsById(id);
            if(byId) return Optional.empty();
            crop.setId(id);
            return Optional.of(saveCrop(crop));
        }

    }

    ~~~
 

## Controller 생성 - Version 01
1. 기본적인 형태의 Controller 를 생성한다.

    ~~~java
    package me.potato.farm.cropmanager.crop;

    import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
    import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
    import lombok.extern.slf4j.Slf4j;
    import org.hibernate.exception.JDBCConnectionException;
    import org.modelmapper.ModelMapper;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    import javax.persistence.PersistenceException;
    import java.util.Optional;

    @Slf4j
    @RestController
    public class CropController {

        private final CropService service;
        private final ModelMapper mapper;

        public CropController(CropService service, ModelMapper mapper) {
            this.service = service;
            this.mapper = mapper;
        }


        @GetMapping("/api/crops")
        public Page<Crop> getPagedCrops(Pageable pageable) {
            return service.getAllCrops(pageable);
        }

        @GetMapping("/api/crops/{id}")
        public ResponseEntity getCrop(@PathVariable("id") Long id) {

            Optional<Crop> crop = service.getCrop(id);
            if (!crop.isPresent())
                return ResponseEntity.noContent().build();
            else {
                return ResponseEntity.ok(mapper.map(crop.get(), CropDto.class));
            }
        }


        @PostMapping("/api/crops")
        public ResponseEntity createCrop(@RequestBody CropDto cropDto) {

            Crop saved = service.saveCrop(
                    mapper.map(cropDto, Crop.class)
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                            mapper.map(saved, CropDto.class)
                    );

        }

        @PatchMapping("/api/crops/{id}")
        public ResponseEntity updateCrop(@PathVariable Long id, @RequestBody CropDto cropDto) {
            Optional<Crop> crop = service.updateCrops(id, mapper.map(cropDto, Crop.class));
            if (!crop.isPresent())
                return ResponseEntity.noContent().build();
            else {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(
                                mapper.map(crop.get(), CropDto.class)
                        );
            }

        }


    }

    ~~~
 
## Test 수행 - 001
1. Run > Edit Configuration > Spring Boot > Application > configuration > Active profiles : dev
2. Mysql 실행
3. Application 실행
4. Mysql 종료
5. Crop 생성
    - Request
    ~~~json
    POST localhost:8080/api/crops
    Content-Type: application/json

    {
    "name":"potato",
    "class_name":"root"
    }
    ~~~

    - Response
    ~~~json
    {
    "timestamp": "2018-12-17T09:11:37.760+0000",
    "status": 500,
    "error": "Internal Server Error",
    "message": "Could not open JPA EntityManager for transaction; nested exception is org.hibernate.exception.JDBCConnectionException: Unable to acquire JDBC Connection",
    "path": "/api/crops"
    }
    ~~~ 
      

- Exception 발생 내역을 그대로 노출 시킨다면, 대부분의 Client는 해당 정보가 쓸모 없다. 
따라서 좀 더 명확한 에러 전달이 필요하다.  

## JpaExceptionHandler의 생성
- JpaExceptionHandling을 위해서 별도의 ControllerAdvice를 추가한다.

    ~~~java
    package me.potato.farm.cropmanager.exceptionhandler;

    import org.hibernate.exception.JDBCConnectionException;
    import org.springframework.http.HttpStatus;
    import org.springframework.web.bind.annotation.ControllerAdvice;
    import org.springframework.web.bind.annotation.ExceptionHandler;
    import org.springframework.web.bind.annotation.ResponseBody;
    import org.springframework.web.bind.annotation.ResponseStatus;

    @ControllerAdvice
    public class JpaExceptionHandler {

        @ExceptionHandler(JDBCConnectionException.class)
        @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
        @ResponseBody
        public String handler(JDBCConnectionException ex){
            return ex.getMessage();
        }

    }
    ~~~


## Test 수행 - 002
1. Run > Edit Configuration > Spring Boot > Application > configuration > Active profiles : dev
2. Mysql 실행
3. Application 실행
4. Mysql 종료
5. Crop 생성
    - Request
    ~~~json
    POST localhost:8080/api/crops
    Content-Type: application/json

    {
    "name":"potato",
    "class_name":"root"
    }
    ~~~

    - Response
    ~~~json
    HTTP/1.1 503 
    Content-Type: text/plain;charset=UTF-8
    Content-Length: 33
    Date: Mon, 17 Dec 2018 09:20:47 GMT
    Connection: close

    Unable to acquire JDBC Connection
    ~~~ 
      
- Mysql 이 연결이 되지 않기 때문에 약 30초 후 Connection 관련 에러가 발생한다.
- Advice를 이용한다면 특적 Exception의 Handling을 Global 하게 할 수 있다. (Global Exception Handler == Advice)


## Controller 생성 - Version 02
1. ControllerAdvice를 이용하면 Exception Handling은 가능하지만, 많은 시간이 지나 후 Exception이 발생한다.
2. 따라서 Fast Fail을 위해서 Hystix Command를 추가 한다.

    ~~~java
    package me.potato.farm.cropmanager.crop;

    import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
    import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
    import lombok.extern.slf4j.Slf4j;
    import org.hibernate.exception.JDBCConnectionException;
    import org.modelmapper.ModelMapper;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    import javax.persistence.PersistenceException;
    import java.util.Optional;

    @Slf4j
    @RestController
    public class CropController {

        private final CropService service;
        private final ModelMapper mapper;

        public CropController(CropService service, ModelMapper mapper) {
            this.service = service;
            this.mapper = mapper;
        }


        @GetMapping("/api/crops")
        public Page<Crop> getPagedCrops(Pageable pageable) {
            return service.getAllCrops(pageable);
        }

        @GetMapping("/api/crops/{id}")
        public ResponseEntity getCrop(@PathVariable("id") Long id) {

            Optional<Crop> crop = service.getCrop(id);
            if (!crop.isPresent())
                return ResponseEntity.noContent().build();
            else {
                return ResponseEntity.ok(mapper.map(crop.get(), CropDto.class));
            }
        }


        @HystrixCommand(
                commandProperties = {@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "5000")}
                , fallbackMethod = "fallback"
        )
        @PostMapping("/api/crops")
        public ResponseEntity createCrop(@RequestBody CropDto cropDto) {

            Crop saved = service.saveCrop(
                    mapper.map(cropDto, Crop.class)
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                            mapper.map(saved, CropDto.class)
                    );

        }

        public ResponseEntity fallback(CropDto cropDto, Throwable throwable) {

            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(throwable.getMessage());

        }


        @PatchMapping("/api/crops/{id}")
        public ResponseEntity updateCrop(@PathVariable Long id, @RequestBody CropDto cropDto) {
            Optional<Crop> crop = service.updateCrops(id, mapper.map(cropDto, Crop.class));
            if (!crop.isPresent())
                return ResponseEntity.noContent().build();
            else {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(
                                mapper.map(crop.get(), CropDto.class)
                        );
            }

        }


    }
    ~~~


## Test 수행 - 003
1. Run > Edit Configuration > Spring Boot > Application > configuration > Active profiles : dev
2. Mysql 실행
3. Application 실행
4. Mysql 종료
5. Crop 생성
    - Request
    ~~~json
    POST localhost:8080/api/crops
    Content-Type: application/json

    {
    "name":"potato",
    "class_name":"root"
    }
    ~~~

    - Response
    ~~~json
    HTTP/1.1 503 
    Content-Type: text/plain;charset=UTF-8
    Content-Length: 33
    Date: Mon, 17 Dec 2018 09:20:47 GMT
    Connection: close

    Unable to acquire JDBC Connection
    ~~~ 
      
- Mysql 이 연결이 되지 않기 때문에 약 5초 후 Connection 관련 에러가 발생한다.
- HystrixCommand는 Exception을 Hooking 하기 때문에 Advice가 실행되지 않는다.



## Controller 생성 - Version 03
1. Client 요청에 대한 입력정보 오류를 친절하게 안내 하기 위해서는 Springframework의 Validation 기능을 사용할 수 있다.
2. 입력 정보를 담을 CropDto에 Validation 설정을 한다.
3. Controller 수신시 Validation을 수행한다.

- CropDto에 @NotEmpty를 통해 제약을 추가 할 수 있다.
- Integer등의 Numeric Type의 경우 @NotNull을 사용할 수 있다.
    ~~~java
    package me.potato.farm.cropmanager.crop;

    import com.fasterxml.jackson.databind.PropertyNamingStrategy;
    import com.fasterxml.jackson.databind.annotation.JsonNaming;
    import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.NoArgsConstructor;

    import javax.validation.constraints.NotEmpty;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public class CropDto {
        private Long id;

        @NotEmpty
        private String name;

        @NotEmpty
        private String className;
    }
    ~~~

- Springframework의 Validator내장 Bean을 주입 받는다.
- validate Method를 추가한다.
- createCrop의 예시를 보면 Optional Type의 Validation 기능을 볼 수 있다.
    ~~~java
    package me.potato.farm.cropmanager.crop;

    import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
    import lombok.extern.slf4j.Slf4j;
    import org.modelmapper.ModelMapper;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.validation.BeanPropertyBindingResult;
    import org.springframework.validation.Errors;
    import org.springframework.validation.Validator;
    import org.springframework.web.bind.annotation.*;

    import java.util.Optional;
    import java.util.stream.Collectors;

    @Slf4j
    @RestController
    public class CropController {

        private final CropService service;
        private final ModelMapper mapper;
        private final Validator validator;

        public CropController(CropService service, ModelMapper mapper, Validator validator) {
            this.service = service;
            this.mapper = mapper;
            this.validator = validator;
        }


        @GetMapping("/api/crops")
        public Page<Crop> getPagedCrops(Pageable pageable) {
            return service.getAllCrops(pageable);
        }

        @GetMapping("/api/crops/{id}")
        public ResponseEntity getCrop(@PathVariable("id") Long id) {

            Optional<Crop> crop = service.getCrop(id);
            if (!crop.isPresent())
                return ResponseEntity.noContent().build();
            else {
                return ResponseEntity.ok(mapper.map(crop.get(), CropDto.class));
            }
        }


        @HystrixCommand(
                fallbackMethod = "fallback"
        )
        @PostMapping("/api/crops")
        public ResponseEntity createCrop(@RequestBody CropDto cropDto) throws InterruptedException {

            Optional<String> validateError = validate(cropDto);
            
            if (validateError.isPresent())
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(validateError.get());


            Crop saved = service.saveCrop(
                    mapper.map(cropDto, Crop.class)
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                            mapper.map(saved, CropDto.class)
                    );

        }

        private Optional<String> validate(Object object) {
            Errors errors = new BeanPropertyBindingResult(object, object.getClass().getName());
            validator.validate(object, errors);

            if (errors.hasErrors()) {
                String errMessage = errors.getFieldErrors().stream().map(error -> error.getField() + " : " + error.getDefaultMessage()).collect(Collectors.joining(" / "));
                return Optional.ofNullable(errMessage);
            }

            return Optional.empty();
        }

        public ResponseEntity fallback(CropDto cropDto, Throwable throwable) {
            log.info(throwable.getMessage());

            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(throwable.getMessage());

        }


        @PatchMapping("/api/crops/{id}")
        public ResponseEntity updateCrop(@PathVariable Long id, @RequestBody CropDto cropDto) {
            Optional<Crop> crop = service.updateCrops(id, mapper.map(cropDto, Crop.class));
            if (!crop.isPresent())
                return ResponseEntity.noContent().build();
            else {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(
                                mapper.map(crop.get(), CropDto.class)
                        );
            }

        }


    }

    ~~~
