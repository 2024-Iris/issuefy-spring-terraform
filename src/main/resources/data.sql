USE `issuefy`;

-- Inserting users
INSERT INTO `user` (github_id, email) VALUES ('githubuser1', 'user1@example.com');
INSERT INTO `user` (github_id, email) VALUES ('githubuser2', 'user2@example.com');

-- Inserting organizations
INSERT INTO `org` (name) VALUES ('Organization A');
INSERT INTO `org` (name) VALUES ('Organization B');

-- Inserting repositories
INSERT INTO `repository` (org_id, name) VALUES (1, 'Repo A1');
INSERT INTO `repository` (org_id, name) VALUES (1, 'Repo A2');
INSERT INTO `repository` (org_id, name) VALUES (2, 'Repo B1');

-- Inserting issues
INSERT INTO `issue` (repository_id, name) VALUES (1, 'Issue A1-1');
INSERT INTO `issue` (repository_id, name) VALUES (1, 'Issue A1-2');
INSERT INTO `issue` (repository_id, name) VALUES (2, 'Issue A2-1');
INSERT INTO `issue` (repository_id, name) VALUES (3, 'Issue B1-1');

-- Inserting labels
INSERT INTO `label` (name) VALUES ('bug');
INSERT INTO `label` (name) VALUES ('feature');

-- Inserting subscriptions
INSERT INTO `subscribe` (repository_id, user_id) VALUES (1, 1);
INSERT INTO `subscribe` (repository_id, user_id) VALUES (1, 2);
INSERT INTO `subscribe` (repository_id, user_id) VALUES (2, 1);
INSERT INTO `subscribe` (repository_id, user_id) VALUES (3, 2);
