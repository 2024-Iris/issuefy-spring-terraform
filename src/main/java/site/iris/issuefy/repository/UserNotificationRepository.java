package site.iris.issuefy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import site.iris.issuefy.entity.UserNotification;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

	int countByUserIdAndIsReadFalse(Long userId);

	List<UserNotification> findUserNotificationsByUserGithubId(String userId);

	@Modifying
	@Query("UPDATE UserNotification un SET un.isRead = true WHERE un.id IN :ids")
	void markAsRead(@Param("ids") List<Long> ids);

}
