package site.iris.issuefy.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import site.iris.issuefy.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

}
