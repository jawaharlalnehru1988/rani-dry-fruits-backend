package com.asknehru.myclientsapi.ranicashew;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FruitComboRepository extends JpaRepository<FruitCombo, Long> {
    List<FruitCombo> findAllByOrderByCreatedAtDesc();
    List<FruitCombo> findAllByIsActiveTrueOrderByCreatedAtDesc();
}
