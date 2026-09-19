package com.asknehru.myclientsapi.ranicashew;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComboItemRepository extends JpaRepository<ComboItem, Long> {
    List<ComboItem> findAllByComboIdInOrderByComboIdAscSortOrderAscIdAsc(List<Long> comboIds);
    List<ComboItem> findAllByComboIdOrderBySortOrderAscIdAsc(Long comboId);
}
