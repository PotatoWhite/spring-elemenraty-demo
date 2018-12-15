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

	public Crop saveCrop(Crop crop){
		return cropRepo.saveAndFlush(crop);
	}

	public Optional<Crop> updateCrops(@NotNull Long id, Crop crop){
		boolean byId = cropRepo.existsById(id);

		if(byId){
			crop.setId(id);
			return Optional.ofNullable(saveCrop(crop));
		}
		else {
			return Optional.empty();
		}

	}

}
