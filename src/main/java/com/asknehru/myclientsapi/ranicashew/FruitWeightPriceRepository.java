package com.asknehru.myclientsapi.ranicashew;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FruitWeightPriceRepository extends JpaRepository<FruitWeightPrice, Long> {

    List<FruitWeightPrice> findAllByFruitIdInOrderByFruitIdAscIdAsc(Collection<Long> fruitIds);

    List<FruitWeightPrice> findAllByFruitIdOrderByIdAsc(Long fruitId);
}
