INSERT INTO role_user (role_name) VALUES
('backend-разработчик'),
('frontend-разработчик'),
('UI/UX-дизайнер'),
('devops-инженер'),
('тестировщик');



select * from role_user
-- Технологии для backend-разработчика (role_id = 1)
INSERT INTO technology (role_id, technology_name) VALUES
(11, 'C++'),
(11, 'C#'),
(11, 'JavaScript'),
(11, 'Java');

-- Технологии для UI/UX-дизайнера (role_id = 2)
INSERT INTO technology (role_id, technology_name) VALUES
(12, 'Figma'),
(12, 'Adobe XD'),
(12, 'Sketch'),
(12, 'Photoshop');

-- Технологии для frontend-разработчика (role_id = 3)
INSERT INTO technology (role_id, technology_name) VALUES
(13, 'HTML'),
(13, 'CSS'),
(13, 'JavaScript'),
(13, 'React');

-- Технологии для devops-инженера (role_id = 4)
INSERT INTO technology (role_id, technology_name) VALUES
(14, 'Docker'),
(14, 'Kubernetes'),
(14, 'Jenkins'),
(14, 'GitLab CI/CD');

-- Технологии для тестировщика (role_id = 5)
INSERT INTO technology (role_id, technology_name) VALUES
(15, 'Selenium'),
(15, 'Postman'),
(15, 'JMeter'),
(15, 'TestRail');



INSERT INTO category (worker_id, category_name) VALUES
(1, 'Логотипы'),
(1, 'Визитки'),
(1, 'Графический дизайн'),
(1, 'Полиграфия'),
(1, 'Брендинг');
