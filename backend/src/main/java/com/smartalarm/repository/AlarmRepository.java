package com.smartalarm.repository;

import com.smartalarm.model.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    List<Alarm> findByUserIdOrderByTimeAsc(Long userId);
    List<Alarm> findByUserIdAndEnabledTrueOrderByTimeAsc(Long userId);
    void deleteByUserId(Long userId);
}
