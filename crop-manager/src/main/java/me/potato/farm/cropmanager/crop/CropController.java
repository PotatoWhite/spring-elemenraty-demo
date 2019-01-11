package me.potato.farm.cropmanager.crop;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

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

        return service.getCrop(id)
                .map(crop -> ResponseEntity.ok().body(mapper.map(crop, CropDto.class)))
                .orElse(ResponseEntity.status(NO_CONTENT).build());
    }


    @HystrixCommand(
            fallbackMethod = "fallback"
    )
    @PostMapping("/api/crops")
    public ResponseEntity createCrop(@RequestBody CropDto cropDto) {

        log.info(cropDto.toString());
        return validate(cropDto)
                .map(errorString -> ResponseEntity.status(BAD_REQUEST).body((Object)errorString))
                .orElseGet(() -> {
                    Crop saved = service.saveCrop(mapper.map(cropDto, Crop.class));
                    return ResponseEntity.status(CREATED).body(mapper.map(saved, CropDto.class));
                });


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

    public ResponseEntity<String> fallback(CropDto cropDto, Throwable throwable) {
        log.info(throwable.getMessage());

        return ResponseEntity
                .status(SERVICE_UNAVAILABLE)
                .body(throwable.getMessage());

    }


    @PatchMapping("/api/crops/{id}")
    public ResponseEntity updateCrop(@PathVariable Long id, @RequestBody CropDto cropDto) {
        return service.updateCrops(id, mapper.map(cropDto, Crop.class))
                .map(crop -> ResponseEntity.status(OK).body(mapper.map(crop, CropDto.class)))
                .orElse(ResponseEntity.noContent().build());

    }


}
