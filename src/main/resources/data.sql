USE `issuefy`;

-- Inserting users
INSERT INTO `user` (github_id, email)
VALUES ('githubuser1', 'user1@example.com');
INSERT INTO `user` (github_id, email)
VALUES ('githubuser2', 'user2@example.com');
INSERT INTO `user` (github_id, email)
VALUES ('lvalentine6', 'user3@example.com');

-- Inserting organizations
INSERT INTO `org` (name)
VALUES ('organization1');
INSERT INTO `org` (name)
VALUES ('organization2');

-- Inserting repositories
INSERT INTO `repository` (org_id, name)
VALUES (1, 'repo-a1');
INSERT INTO `repository` (org_id, name)
VALUES (1, 'repo-a2');
INSERT INTO `repository` (org_id, name)
VALUES (2, 'repob-1');

-- Inserting issues
INSERT INTO `issue` (repository_id, name)
VALUES (1, 'issue-a1-1');
INSERT INTO `issue` (repository_id, name)
VALUES (1, 'issue-a1-2');
INSERT INTO `issue` (repository_id, name)
VALUES (2, 'issue-a2-1');
INSERT INTO `issue` (repository_id, name)
VALUES (3, 'issue-b1-1');

-- Inserting labels
INSERT INTO `label` (name)
VALUES ('bug');
INSERT INTO `label` (name)
VALUES ('feature');

-- Inserting subscriptions
INSERT INTO `subscribe` (repository_id, user_id)
VALUES (1, 1);
INSERT INTO `subscribe` (repository_id, user_id)
VALUES (1, 2);
INSERT INTO `subscribe` (repository_id, user_id)
VALUES (2, 1);
INSERT INTO `subscribe` (repository_id, user_id)
VALUES (3, 2);
INSERT INTO `subscribe` (repository_id, user_id)
VALUES (1, 3);
INSERT INTO `subscribe` (repository_id, user_id)
VALUES (2, 3);
INSERT INTO `subscribe` (repository_id, user_id)
VALUES (3, 3);
