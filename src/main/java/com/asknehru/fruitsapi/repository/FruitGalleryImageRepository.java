package com.asknehru.fruitsapi.repository;

import com.asknehru.fruitsapi.domain.FruitGalleryImage;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FruitGalleryImageRepository extends JpaRepository<FruitGalleryImage, Long> {

    List<FruitGalleryImage> findAllByFruitIdInOrderByFruitIdAscIdAsc(Collection<Long> fruitIds);

    List<FruitGalleryImage> findAllByFruitIdOrderByIdAsc(Long fruitId);
}
