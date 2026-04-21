package com.asknehru.fruitsapi.repository;

import com.asknehru.fruitsapi.domain.FruitGallery;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FruitGalleryRepository extends JpaRepository<FruitGallery, Long> {

    @Query("SELECT DISTINCT f FROM FruitGallery f LEFT JOIN FETCH f.images i ORDER BY f.id ASC, i.id ASC")
    List<FruitGallery> findAllWithImagesOrderById();

    @Query("SELECT f FROM FruitGallery f LEFT JOIN FETCH f.images i WHERE f.id = :id ORDER BY i.id ASC")
    Optional<FruitGallery> findByIdWithImages(Long id);
}
