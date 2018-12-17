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
