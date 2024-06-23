USE `issuefy`;

-- Inserting users
INSERT INTO `user` (github_id, email)
VALUES ('githubuser1', 'user1@example.com');
INSERT INTO `user` (github_id, email)
VALUES ('githubuser2', 'user2@example.com');
INSERT INTO `user` (github_id, email)
VALUES ('lvalentine6', 'user3@example.com');

-- Inserting organizations
INSERT INTO `organization` (name, gh_org_id)
VALUES ('organization1', 1);
INSERT INTO `organization` (name, gh_org_id)
VALUES ('organization2', 2);

-- Inserting repositories
INSERT INTO `repository` (org_id, name, gh_repo_id)
VALUES (1, 'repo-a1', 1);
INSERT INTO `repository` (org_id, name, gh_repo_id)
VALUES (1, 'repo-a2', 2);
INSERT INTO `repository` (org_id, name, gh_repo_id)
VALUES (2, 'repob-1', 3);

-- Inserting issues
INSERT INTO `issue` (repository_id, title, gh_issue_id, is_starred, is_read, state, created_at)
VALUES (1, 'issue-a1-1', 1234, 0, 0, 'open', '2024-06-01 12:30:00');
INSERT INTO `issue` (repository_id, title, gh_issue_id, is_starred, is_read, state, created_at)
VALUES (1, 'issue-a1-2', 5678, 0, 0, 'open', '2024-06-02 21:00:00');
INSERT INTO `issue` (repository_id, title, gh_issue_id, is_starred, is_read, state, created_at)
VALUES (2, 'issue-a2-1', 5679, 0, 0, 'open', '2024-06-03 03:14:10');
INSERT INTO `issue` (repository_id, title, gh_issue_id, is_starred, is_read, state, created_at)
VALUES (3, 'issue-b1-1', 2000, 0, 0, 'open', '2024-06-04 08:29:55');

-- Inserting labels
INSERT INTO `label` (name, color)
VALUES ('bug', 'zzzz');
INSERT INTO `label` (name, color)
VALUES ('feature', 'xxxx');

-- Inserting subscriptions
INSERT INTO `subscription` (repository_id, user_id)
VALUES (1, 1);
INSERT INTO `subscription` (repository_id, user_id)
VALUES (1, 2);
INSERT INTO `subscription` (repository_id, user_id)
VALUES (2, 1);
INSERT INTO `subscription` (repository_id, user_id)
VALUES (3, 2);
INSERT INTO `subscription` (repository_id, user_id)
VALUES (1, 3);
INSERT INTO `subscription` (repository_id, user_id)
VALUES (2, 3);
INSERT INTO `subscription` (repository_id, user_id)
VALUES (3, 3);
