package me.potato.farm.cropmanager.crop;

import com.sun.net.httpserver.HttpsConfigurator;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;


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
		if(!crop.isPresent())
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
		if(!crop.isPresent())
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
