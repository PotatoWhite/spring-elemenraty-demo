package me.potato.farm.cropmanager.crop;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CropRepository extends JpaRepository<Crop, Long> {
	Page<Crop> findAll(Pageable pageable);

}
