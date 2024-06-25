package site.iris.issuefy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import site.iris.issuefy.entity.UserNotification;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

	int countByUserIdAndIsReadFalse(Long userId);

	List<UserNotification> findTop5ByUserIdAndIsReadFalseOrderByNotificationPushTimeDesc(Long userId);

}
