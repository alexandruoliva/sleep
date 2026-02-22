package com.noom.interview.fullstack.sleep.repository;

import com.noom.interview.fullstack.sleep.models.MorningFeeling;
import com.noom.interview.fullstack.sleep.models.SleepLog;
import com.noom.interview.fullstack.sleep.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("unittest")
class SleepLogRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SleepLogRepository sleepLogRepository;

    @Test
    void save_insertsNewLogAndReturnsIt() {
        User user = userRepository.save(new User());
        SleepLog log = new SleepLog();
        log.setUserId(user.getId());
        log.setSleepDate(LocalDate.of(2025, 2, 22));
        log.setWentToBedAt(LocalTime.of(23, 0));
        log.setGotUpAt(LocalTime.of(7, 30));
        log.setTotalTimeInBedMinutes(510);
        log.setMorningFeeling(MorningFeeling.GOOD);

        SleepLog saved = sleepLogRepository.save(log);

        assertThat(saved.getId()).isPositive();
        assertThat(saved.getUserId()).isEqualTo(user.getId());
        assertThat(saved.getSleepDate()).isEqualTo(LocalDate.of(2025, 2, 22));
        assertThat(saved.getTotalTimeInBedMinutes()).isEqualTo(510);
        assertThat(saved.getMorningFeeling()).isEqualTo(MorningFeeling.GOOD);
    }

    @Test
    void findByUserIdAndSleepDate_returnsEmptyWhenNone() {
        User user = userRepository.save(new User());
        Optional<SleepLog> found = sleepLogRepository.findByUserIdAndSleepDate(user.getId(), LocalDate.of(2025, 2, 22));
        assertThat(found).isEmpty();
    }

    @Test
    void findByUserIdAndSleepDate_returnsLogAfterSave() {
        User user = userRepository.save(new User());
        SleepLog log = new SleepLog();
        log.setUserId(user.getId());
        log.setSleepDate(LocalDate.of(2025, 2, 22));
        log.setWentToBedAt(LocalTime.of(23, 0));
        log.setGotUpAt(LocalTime.of(7, 30));
        log.setTotalTimeInBedMinutes(480);
        log.setMorningFeeling(MorningFeeling.OK);
        sleepLogRepository.save(log);

        Optional<SleepLog> found = sleepLogRepository.findByUserIdAndSleepDate(user.getId(), LocalDate.of(2025, 2, 22));
        assertThat(found).isPresent();
        assertThat(found.get().getMorningFeeling()).isEqualTo(MorningFeeling.OK);
    }

    @Test
    void save_updatesWhenSameEntityReSaved() {
        User user = userRepository.save(new User());
        SleepLog log = new SleepLog();
        log.setUserId(user.getId());
        log.setSleepDate(LocalDate.of(2025, 2, 22));
        log.setWentToBedAt(LocalTime.of(23, 0));
        log.setGotUpAt(LocalTime.of(7, 0));
        log.setTotalTimeInBedMinutes(480);
        log.setMorningFeeling(MorningFeeling.BAD);
        SleepLog saved = sleepLogRepository.save(log);

        saved.setWentToBedAt(LocalTime.of(22, 30));
        saved.setGotUpAt(LocalTime.of(6, 30));
        saved.setMorningFeeling(MorningFeeling.GOOD);
        sleepLogRepository.save(saved);

        Optional<SleepLog> found = sleepLogRepository.findByUserIdAndSleepDate(user.getId(), LocalDate.of(2025, 2, 22));
        assertThat(found).isPresent();
        assertThat(found.get().getWentToBedAt()).isEqualTo(LocalTime.of(22, 30));
        assertThat(found.get().getMorningFeeling()).isEqualTo(MorningFeeling.GOOD);
    }

    @Test
    void findTopByUserIdOrderBySleepDateDesc_returnsMostRecentByDate() {
        User user = userRepository.save(new User());
        SleepLog log1 = new SleepLog();
        log1.setUserId(user.getId());
        log1.setSleepDate(LocalDate.of(2025, 2, 20));
        log1.setWentToBedAt(LocalTime.of(23, 0));
        log1.setGotUpAt(LocalTime.of(7, 0));
        log1.setTotalTimeInBedMinutes(480);
        log1.setMorningFeeling(MorningFeeling.OK);
        sleepLogRepository.save(log1);

        SleepLog log2 = new SleepLog();
        log2.setUserId(user.getId());
        log2.setSleepDate(LocalDate.of(2025, 2, 22));
        log2.setWentToBedAt(LocalTime.of(0, 30));
        log2.setGotUpAt(LocalTime.of(8, 0));
        log2.setTotalTimeInBedMinutes(450);
        log2.setMorningFeeling(MorningFeeling.GOOD);
        sleepLogRepository.save(log2);

        Optional<SleepLog> latest = sleepLogRepository.findTopByUserIdOrderBySleepDateDesc(user.getId());
        assertThat(latest).isPresent();
        assertThat(latest.get().getSleepDate()).isEqualTo(LocalDate.of(2025, 2, 22));
        assertThat(latest.get().getMorningFeeling()).isEqualTo(MorningFeeling.GOOD);
    }

    @Test
    void findTopByUserIdOrderBySleepDateDesc_returnsEmptyWhenNoLogs() {
        User user = userRepository.save(new User());
        assertThat(sleepLogRepository.findTopByUserIdOrderBySleepDateDesc(user.getId())).isEmpty();
    }
}
