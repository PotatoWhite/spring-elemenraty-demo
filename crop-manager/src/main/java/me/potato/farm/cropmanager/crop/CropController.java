package me.potato.farm.cropmanager.crop;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpOutputMessage;
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
