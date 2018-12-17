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
