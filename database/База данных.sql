CREATE TABLE role_user (
    role_id SERIAL PRIMARY KEY,
    role_name VARCHAR(128) NOT NULL UNIQUE
);

select * from role_worker
select * from worker_technology
select * from technology
worker_technology


CREATE TABLE worker (
    worker_id SERIAL PRIMARY KEY,
    worker_nickname VARCHAR(128) NOT NULL UNIQUE,
    worker_name VARCHAR(128),
    worker_lastname VARCHAR(128),
    worker_patronymic VARCHAR(128),
    worker_password VARCHAR(256) NOT NULL,
	worker_filepath VARCHAR(2048),
    worker_email VARCHAR(128) NOT NULL UNIQUE
);

CREATE TABLE friends_list (
    user_id INT4 NOT NULL,
    worker_id INT4 NOT NULL,
    CONSTRAINT fk_friends_user FOREIGN KEY (user_id) REFERENCES worker(worker_id),
    CONSTRAINT fk_friends_worker FOREIGN KEY (worker_id) REFERENCES worker(worker_id),
    CONSTRAINT uq_friends_unique_pair UNIQUE (user_id, worker_id)
);

CREATE TABLE role_worker (
    role_id INT4 NOT NULL,
    worker_id INT4 NOT NULL,
    PRIMARY KEY (role_id, worker_id),
    FOREIGN KEY (role_id) REFERENCES role_user(role_id),
    FOREIGN KEY (worker_id) REFERENCES worker(worker_id)
);

CREATE TABLE technology (
    technology_id SERIAL PRIMARY KEY,
    role_id INT4 NOT NULL,
    technology_name VARCHAR(128) NOT NULL,
    FOREIGN KEY (role_id) REFERENCES role_user(role_id)
);

CREATE TABLE worker_technology (
    worker_id INT4 NOT NULL,
    technology_id INT4 NOT NULL,
    PRIMARY KEY (worker_id, technology_id),
    FOREIGN KEY (worker_id) REFERENCES worker(worker_id),
    FOREIGN KEY (technology_id) REFERENCES technology(technology_id)
);

CREATE TABLE category (
    category_id SERIAL PRIMARY KEY,
    worker_id INT4 NOT NULL,
    category_name VARCHAR(128) NOT NULL,
    FOREIGN KEY (worker_id) REFERENCES worker(worker_id),
    CONSTRAINT uq_worker_category UNIQUE (worker_id, category_name)
);


CREATE TABLE project (
    project_id SERIAL PRIMARY KEY,
    worker_id INT4  NOT NULL,
    category_id INT4,
    project_name VARCHAR(128) NOT NULL,
    project_description VARCHAR(2048),
    project_daystart DATE NOT NULL,
    project_deadline DATE NOT NULL,
	project_status BOOL NOT NULL DEFAULT TRUE,
    FOREIGN KEY (category_id) REFERENCES category(category_id),
    FOREIGN KEY (worker_id) REFERENCES worker(worker_id)
);

CREATE TABLE worker_project (
    worker_id INT4 NOT NULL,
    project_id INT4 NOT NULL,
    PRIMARY KEY (worker_id, project_id),
    FOREIGN KEY (project_id) REFERENCES project(project_id),
    FOREIGN KEY (worker_id) REFERENCES worker(worker_id)
);

CREATE TABLE chat_project (
    chat_project_id SERIAL PRIMARY KEY,
    project_id INT4 NOT NULL,
    FOREIGN KEY (project_id) REFERENCES project(project_id)
);

CREATE TABLE message_project (
    message_p_id SERIAL PRIMARY KEY,
    chat_project_id INT4 NOT NULL,
    worker_id INT4 NOT NULL,
    message_p_text VARCHAR(1024) NOT NULL,
    message_p_time DATE NOT NULL,
    FOREIGN KEY (chat_project_id) REFERENCES chat_project(chat_project_id),
    FOREIGN KEY (worker_id) REFERENCES worker(worker_id)
);

CREATE TABLE content_project (
    content_p_id SERIAL PRIMARY KEY,
    worker_id INT4  NOT NULL,
    project_id INT4  NOT NULL,
    content_p_name VARCHAR(128) NOT NULL,
    content_p_filepath VARCHAR(2048) NOT NULL,
    content_p_text VARCHAR(1024),
    FOREIGN KEY (worker_id, project_id) REFERENCES worker_project(worker_id, project_id)
);

CREATE TABLE task (
    task_id SERIAL PRIMARY KEY,
    project_id INT4 NOT NULL,
    task_name VARCHAR(128) NOT NULL,
    task_description VARCHAR(2048) NOT NULL,
    task_daystart DATE NOT NULL,
    task_deadline DATE NOT NULL,
    task_status BOOL NOT NULL DEFAULT TRUE,
    FOREIGN KEY (project_id) REFERENCES project(project_id)
);

CREATE TABLE chat_task (
    chat_task_id SERIAL PRIMARY KEY,
    task_id INT4 NOT NULL,
    FOREIGN KEY (task_id) REFERENCES task(task_id)
);

CREATE TABLE message_task (
    message_task_id SERIAL PRIMARY KEY,
    worker_id INT4 NOT NULL,
    chat_task_id INT4 NOT NULL,
    message_task_text VARCHAR(1024) NOT NULL,
    message_task_time DATE NOT NULL,
    FOREIGN KEY (worker_id) REFERENCES worker(worker_id),
    FOREIGN KEY (chat_task_id) REFERENCES chat_task(chat_task_id)
);

CREATE TABLE worker_task (
    worker_id INT4 NOT NULL,
    task_id INT4 NOT NULL,
	PRIMARY KEY (worker_id, task_id),
    FOREIGN KEY (worker_id) REFERENCES worker(worker_id),
    FOREIGN KEY (task_id) REFERENCES task(task_id)
);

CREATE TABLE content_task (
    content_task_id SERIAL PRIMARY KEY,
    worker_id INT4 NOT NULL,
    task_id INT4 NOT NULL,
    content_task_name VARCHAR(128) NOT NULL,
    content_task_filepath VARCHAR(2048) NOT NULL,
    content_task_text VARCHAR(1024),
	FOREIGN KEY (worker_id, task_id) REFERENCES worker_task(worker_id, task_id)

);

select * from content_task
select * from notification_task

CREATE TABLE notification_task (
    notification_task_id SERIAL PRIMARY KEY,
	worker_id INT4 NOT NULL,
    task_id INT4 NOT NULL,
	notification_task_text VARCHAR(1024) NOT NULL,
	notification_task_time TIMESTAMP NOT NULL,
    notification_task_viewed BOOL NOT NULL  DEFAULT FALSE,
    notification_task_accepted BOOL,
	FOREIGN KEY (worker_id, task_id) REFERENCES worker_task(worker_id, task_id)
);

CREATE TABLE reminder_task (
    reminder_task_id SERIAL PRIMARY KEY,
	worker_id INT4 NOT NULL,
    task_id INT4 NOT NULL,
	reminder_task_text VARCHAR(1024) NOT NULL,
    reminder_task_time TIMESTAMP NOT NULL,
    reminder_task_state BOOL NOT NULL DEFAULT FALSE,
	FOREIGN KEY (worker_id, task_id) REFERENCES worker_task(worker_id, task_id)
);

CREATE TABLE report_project (
    report_id SERIAL PRIMARY KEY,
    worker_id INT4 NOT NULL,
    project_id INT4 NOT NULL,
    report_name VARCHAR(128) NOT NULL,
    report_filepath VARCHAR(2048) NOT NULL,
    report_text VARCHAR(2048),
    FOREIGN KEY (worker_id, project_id) REFERENCES worker_project(worker_id, project_id)
);


delete from project
