package me.potato.yieldmanager;

import lombok.extern.slf4j.Slf4j;
import me.potato.yieldmanager.service.proxy.CropDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RibbonClient(name = "crop-manager")
public class AppRunner implements ApplicationRunner {

    private final RestTemplate restTemplate;

    @Value("${startup.message}")
    private String startupMessage;

    public AppRunner(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info(startupMessage);

        CropDto newCrop = CropDto.builder()
                .name("testPotato")
                .className("test")
                .build();

        try {
            ResponseEntity<CropDto> cropDtoResponseEntity = restTemplate.postForEntity("http://crop-manager/api/crops", newCrop, CropDto.class);

        } catch (RestClientException ex) {
            log.info(ex.getClass().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
