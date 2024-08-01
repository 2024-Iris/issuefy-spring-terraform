USE `issuefy`;

-- Inserting users
INSERT INTO `user` (github_id, email)
VALUES ('githubuser1', 'user1@example.com'),
       ('githubuser2', 'user2@example.com'),
       ('lvalentine6', 'user3@example.com');

-- Inserting organizations
INSERT INTO `organization` (name, gh_org_id)
VALUES ('2024-Iris', 166014585),
       ('Org2', 166014586),
       ('Org3', 166014587);

-- Inserting repositories
INSERT INTO `repository` (org_id, name, gh_repo_id, updated_at)
VALUES (1, 'issuefy-test', 814324604, CURRENT_TIMESTAMP),
       (1, 'issuefy-spring', 785651152, CURRENT_TIMESTAMP),
       (1, 'issuefy-vue', 783652373, CURRENT_TIMESTAMP),
       (1, 'awesome-vue', 814324605, '2024-05-01 10:30:00'),
       (2, 'react-native', 814324606, '2024-05-02 11:45:00'),
       (1, 'tensorflow', 814324607, '2024-05-03 09:15:00'),
       (3, 'flutter', 814324608, '2024-05-04 14:20:00'),
       (2, 'vscode', 814324609, '2024-05-05 16:30:00'),
       (1, 'angular', 814324610, '2024-05-06 08:45:00'),
       (3, 'rust', 814324611, '2024-05-07 12:10:00'),
       (2, 'kubernetes', 814324612, '2024-05-08 17:55:00'),
       (1, 'bitcoin', 814324613, '2024-05-09 07:30:00'),
       (3, 'django', 814324614, '2024-05-10 13:40:00'),
       (2, 'node', 814324615, '2024-05-11 10:20:00'),
       (1, 'swift', 814324616, '2024-05-12 15:15:00'),
       (3, 'spring-boot', 814324617, '2024-05-13 11:50:00'),
       (2, 'docker', 814324618, '2024-05-14 09:25:00'),
       (1, 'electron', 814324619, '2024-05-15 14:05:00'),
       (3, 'pytorch', 814324620, '2024-05-16 16:40:00'),
       (2, 'laravel', 814324621, '2024-05-17 08:35:00'),
       (1, 'ruby-on-rails', 814324622, '2024-05-18 12:55:00'),
       (3, 'go', 814324623, '2024-05-19 10:10:00'),
       (2, 'opencv', 814324624, '2024-05-20 15:30:00'),
       (1, 'express', 814324625, '2024-05-21 09:50:00'),
       (3, 'scikit-learn', 814324626, '2024-05-22 13:15:00'),
       (2, 'ansible', 814324627, '2024-05-23 11:40:00'),
       (1, 'nginx', 814324628, '2024-05-24 16:20:00'),
       (3, 'postgresql', 814324629, '2024-05-25 08:05:00');

-- Inserting issues
INSERT INTO `issue` (repository_id, title, gh_issue_id, is_read, state, created_at)
VALUES (1, 'issue-a1-1', 1234, 0, 'open', '2024-06-01 12:30:00'),
       (1, 'issue-a1-2', 5678, 0, 'open', '2024-06-02 21:00:00'),
       (2, 'issue-a2-1', 5679, 0, 'open', '2024-06-03 03:14:10'),
       (3, 'issue-b1-1', 2000, 0, 'open', '2024-06-04 08:29:55');

-- Inserting labels
INSERT INTO `label` (name, color)
VALUES ('bug', 'zzzz'),
       ('feature', 'xxxx');

-- Inserting subscriptions
INSERT INTO `subscription` (repository_id, user_id, is_repo_starred)
VALUES (1, 1, 0),
       (1, 2, 0),
       (2, 1, 0),
       (3, 2, 0),
       (1, 3, 0),
       (2, 3, 0),
       (3, 3, 0);

-- Additional subscriptions for user 3
INSERT INTO `subscription` (user_id, repository_id, is_repo_starred)
VALUES
(3, 4, 0), (3, 5, 0), (3, 6, 0), (3, 7, 0), (3, 8, 1), (3, 9, 0), (3, 10, 1),
(3, 11, 0), (3, 12, 1), (3, 13, 0), (3, 14, 1), (3, 15, 0), (3, 16, 1), (3, 17, 0),
(3, 18, 0), (3, 19, 0), (3, 20, 0), (3, 21, 0), (3, 22, 0), (3, 23, 0), (3, 24, 0),
(3, 25, 0), (3, 26, 0), (3, 27, 0), (3, 28, 0);

-- Inserting user_issue_star (예시)
INSERT INTO `user_issue_star` (user_id, issue_id)
VALUES (3, 1),
       (3, 3);