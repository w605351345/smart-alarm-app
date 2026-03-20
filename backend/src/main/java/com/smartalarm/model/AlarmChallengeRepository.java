package com.smartalarm.repository;

import com.smartalarm.model.AlarmChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 闹钟解密挑战 Repository
 */
@Repository
public interface AlarmChallengeRepository extends JpaRepository<AlarmChallenge, Long> {

    /**
     * 根据闹钟 ID 查找挑战配置
     */
    @Query("SELECT ac FROM AlarmChallenge ac WHERE ac.alarm.id = :alarmId")
    AlarmChallenge findByAlarmId(@Param("alarmId") Long alarmId);

    /**
     * 删除闹钟的挑战配置
     */
    void deleteByAlarmId(Long alarmId);
}
