INSERT INTO App_Role(name, description) VALUES
('User', 'User description'),
('Moderator', 'Moderator description'),
('Admin', 'Admin description');

INSERT INTO App_User(username, password, email, active, banned, role_id) VALUES
('user123', crypt('user123', gen_salt('bf')), 'user123@gmail.com', TRUE, DEFAULT, 1),
('user234', crypt('user234', gen_salt('bf')), 'user234@gmail.com', FALSE, DEFAULT, 1),
('banned123', crypt('banned123', gen_salt('bf')), 'banned123@gmail.com', FALSE, TRUE, 1),
('mod123', crypt('mod123', gen_salt('bf')), 'mod@gmail.com', TRUE, DEFAULT, 2),
('admin123', crypt('admin123', gen_salt('bf')), 'admin@gmail.com', TRUE, DEFAULT, 3);

INSERT INTO App_Category(name, description, active) VALUES
('Admin announcements', 'Admin announcements', DEFAULT),
('Moderator announcements', 'Moderator announcements', DEFAULT),
('General', 'For users', DEFAULT);

INSERT INTO App_Category_Role(category_id, role_id) VALUES
(1,3),
(2,2),
(3,1),
(3,2),
(3,3);

INSERT INTO App_Thread(title, category_id, user_id, active) VALUES
('Admin thread', 1, 5, TRUE),
('Moderator thread', 2, 4, TRUE),
('Welcome!', 3, 5, FALSE),
('Open thread', 3, 1, TRUE);

INSERT INTO app_post(content, thread_id, user_id) VALUES
('Admin post', 1, 5),
('Moderator post', 2, 4),
('Welcome note by admin', 3, 5),
('Discussion started by user', 4, 1);
