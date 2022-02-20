INSERT INTO App_Role(name, description)
VALUES ('User', 'User description'),
       ('Moderator', 'Moderator description'),
       ('Admin', 'Admin description');

INSERT INTO App_User(username, password, email, active, banned, role_id)
VALUES ('user123', crypt('user123', gen_salt('bf')), 'user123@gmail.com', TRUE, DEFAULT, 1),
       ('user234', crypt('user234', gen_salt('bf')), 'user234@gmail.com', FALSE, DEFAULT, 1),
       ('banned123', crypt('banned123', gen_salt('bf')), 'banned123@gmail.com', FALSE, TRUE, 1),
       ('mod123', crypt('mod123', gen_salt('bf')), 'mod@gmail.com', TRUE, DEFAULT, 2),
       ('admin123', crypt('admin123', gen_salt('bf')), 'admin@gmail.com', TRUE, DEFAULT, 3);

INSERT INTO App_Category(name, description, active)
VALUES ('Admin announcements', 'Admin announcements (only admins can write)', DEFAULT),
       ('Moderator announcements', 'Moderator announcements (only mods and admins can write)', DEFAULT),
       ('General', 'For users', DEFAULT),
       ('Inactive', 'Inactive category', FALSE);

INSERT INTO App_Category_Role(category_id, role_id)
VALUES (1, 3),
       (2, 2),
       (3, 1),
       (3, 2),
       (3, 3),
       (4, 1),
       (4, 2),
       (4, 3);

