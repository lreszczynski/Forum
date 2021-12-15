INSERT INTO App_Role(name, description) VALUES
('User', 'User description'),
('Admin', 'Admin description'),
('Moderator', 'Moderator description');

INSERT INTO App_User(username, password, email, active, role_id) VALUES
('user123', crypt('user123', gen_salt('bf')), 'user123@gmail.com', TRUE, 1),
('user234', crypt('user234', gen_salt('bf')), 'user234@gmail.com', FALSE, 1),
('admin123', crypt('admin123', gen_salt('bf')), 'admin@gmail.com', TRUE, 2),
('mod123', crypt('mod123', gen_salt('bf')), 'mod@gmail.com', TRUE, 3);

INSERT INTO App_Category(name, description, active) VALUES
('Announcements', 'Important messages', DEFAULT),
('General', 'About everything', DEFAULT);

INSERT INTO App_Category_Role(category_id, role_id) VALUES
(1,2),
(2,1),
(2,2),
(2,3);

INSERT INTO App_Thread(title, category_id, user_id) VALUES
('Important note', 1, 3),
('Welcome!', 2, 4);

INSERT INTO app_post(content, thread_id, user_id) VALUES
('Important note post', 1, 3),
('Welcome post', 2, 4);
