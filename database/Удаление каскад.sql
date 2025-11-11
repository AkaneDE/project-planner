ALTER TABLE worker_project
DROP CONSTRAINT worker_project_project_id_fkey,
ADD CONSTRAINT worker_project_project_id_fkey FOREIGN KEY (project_id) REFERENCES project(project_id) ON DELETE CASCADE;
ALTER TABLE worker_project
DROP CONSTRAINT worker_project_worker_id_fkey,
ADD CONSTRAINT worker_project_worker_id_fkey FOREIGN KEY (worker_id) REFERENCES worker(worker_id) ON DELETE CASCADE;


ALTER TABLE chat_project
DROP CONSTRAINT chat_project_project_id_fkey,
ADD CONSTRAINT chat_project_project_id_fkey FOREIGN KEY (project_id) REFERENCES project(project_id) ON DELETE CASCADE;

ALTER TABLE message_project
DROP CONSTRAINT message_project_chat_project_id_fkey,
ADD CONSTRAINT message_project_chat_project_id_fkey FOREIGN KEY (chat_project_id) REFERENCES chat_project(chat_project_id) ON DELETE CASCADE;

ALTER TABLE message_project
DROP CONSTRAINT message_project_worker_id_fkey,
ADD CONSTRAINT message_project_worker_id_fkey FOREIGN KEY (worker_id) REFERENCES worker(worker_id) ON DELETE CASCADE;

ALTER TABLE content_project
DROP CONSTRAINT content_project_worker_id_project_id_fkey,
ADD CONSTRAINT content_project_worker_id_project_id_fkey FOREIGN KEY (worker_id, project_id) REFERENCES worker_project(worker_id, project_id) ON DELETE CASCADE;

ALTER TABLE task
DROP CONSTRAINT task_project_id_fkey,
ADD CONSTRAINT task_project_id_fkey FOREIGN KEY (project_id) REFERENCES project(project_id) ON DELETE CASCADE;

ALTER TABLE chat_task
DROP CONSTRAINT chat_task_task_id_fkey,
ADD CONSTRAINT chat_task_task_id_fkey FOREIGN KEY (task_id) REFERENCES task(task_id) ON DELETE CASCADE;

ALTER TABLE message_task
DROP CONSTRAINT message_task_worker_id_fkey,
ADD CONSTRAINT message_task_worker_id_fkey FOREIGN KEY (worker_id) REFERENCES worker(worker_id) ON DELETE CASCADE;

ALTER TABLE message_task
DROP CONSTRAINT message_task_chat_task_id_fkey,
ADD CONSTRAINT message_task_chat_task_id_fkey FOREIGN KEY (chat_task_id) REFERENCES chat_task(chat_task_id) ON DELETE CASCADE;


ALTER TABLE worker_task
DROP CONSTRAINT worker_task_worker_id_fkey,
ADD CONSTRAINT worker_task_worker_id_fkey FOREIGN KEY (worker_id) REFERENCES worker(worker_id) ON DELETE CASCADE;

ALTER TABLE worker_task
DROP CONSTRAINT worker_task_task_id_fkey,
ADD CONSTRAINT worker_task_task_id_fkey FOREIGN KEY (task_id) REFERENCES task(task_id) ON DELETE CASCADE;


ALTER TABLE content_task
DROP CONSTRAINT content_task_worker_id_task_id_fkey, 
ADD CONSTRAINT content_task_worker_id_task_id_fkey FOREIGN KEY (worker_id, task_id) REFERENCES worker_task(worker_id, task_id) ON DELETE CASCADE;


ALTER TABLE notification_task
DROP CONSTRAINT notification_task_worker_id_task_id_fkey,
ADD CONSTRAINT notification_task_worker_id_task_id_fkey FOREIGN KEY (worker_id, task_id) REFERENCES worker_task(worker_id, task_id) ON DELETE CASCADE;

ALTER TABLE reminder_task
DROP CONSTRAINT reminder_task_worker_id_task_id_fkey,
ADD CONSTRAINT reminder_task_worker_id_task_id_fkey FOREIGN KEY (worker_id, task_id) REFERENCES worker_task(worker_id, task_id) ON DELETE CASCADE;

ALTER TABLE report_project
DROP CONSTRAINT report_project_worker_id_project_id_fkey,
ADD CONSTRAINT report_project_worker_id_project_id_fkey FOREIGN KEY (worker_id, project_id) REFERENCES worker_project(worker_id, project_id) ON DELETE CASCADE;
