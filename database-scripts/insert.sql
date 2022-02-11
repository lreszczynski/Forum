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
('Admin announcements', 'Admin announcements (only admins can write)', DEFAULT),
('Moderator announcements', 'Moderator announcements (only mods and admins can write)', DEFAULT),
('General', 'For users', DEFAULT),
('Inactive', 'Inactive category', FALSE);

INSERT INTO App_Category_Role(category_id, role_id) VALUES
(1,3),
(2,2),
(3,1),
(3,2),
(3,3),
(4,1),
(4,2),
(4,3);

INSERT INTO App_Thread(title, category_id, user_id, active, pinned, create_date) VALUES
('Admin thread', 1, 5, TRUE, default, now() - INTERVAL '10 DAY'),
('Moderator thread', 2, 4, TRUE, default, now() - INTERVAL '10 DAY'),
('Welcome note! (pinned and closed, no one can reply)', 3, 5, FALSE, TRUE, now() - INTERVAL '10 DAY'),
('Welcome note! (pinned and closed, no one can reply) 2', 3, 5, FALSE, TRUE, now() - INTERVAL '10 DAY'),
('Welcome note! (pinned and closed, no one can reply) 3', 3, 5, FALSE, TRUE, now() - INTERVAL '10 DAY'),
('Thread by user', 3, 1, TRUE, default, now() - INTERVAL '2 YEAR'),
('Thread in inactive category', 4, 1, TRUE, default, now() - INTERVAL '2 YEAR');

INSERT INTO App_Post(content, thread_id, user_id, create_date) VALUES
('Admin post', 1, 5, now() - INTERVAL '10 DAY'),
('Moderator post', 2, 4, now() - INTERVAL '10 DAY'),
('Welcome post by admin', 3, 5, now() - INTERVAL '10 DAY'),
('Welcome post by admin', 4, 5, now() - INTERVAL '10 DAY'),
('Welcome post by admin', 5, 5, now() - INTERVAL '10 DAY'),
('Discussion started by user', 6, 1, now() - INTERVAL '2 YEAR'),
('Post example', 7, 1, now() - INTERVAL '2 YEAR');

do '
DECLARE myid App_Thread.id%TYPE;
DECLARE rand TIMESTAMP;
begin
for r in 1..100 loop
rand=NOW() - (random() * (interval ''90 days''));
INSERT INTO App_Thread(title, category_id, user_id, active, pinned, create_date) VALUES
(concat(''Random Thread id='',r), 3, 1, TRUE, default, rand) RETURNING id INTO myid;
INSERT INTO App_Post(content, thread_id, user_id, create_date) VALUES
(''Random Post'', myid, 1, rand);
INSERT INTO App_Post(content, thread_id, user_id, create_date) VALUES
(concat(''Post Post Post '',r), 6, 1, NOW() - interval ''1 YEAR'' + (r * (interval ''1 DAY'')));
end loop;
end;
' language plpgsql;

INSERT INTO App_Post(content, thread_id, user_id, create_date) VALUES
('Reply by banned user - freshest post', 6, 3, now() - INTERVAL '30 MINUTE');
