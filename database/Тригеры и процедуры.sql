
CREATE OR REPLACE FUNCTION create_task_notification()
RETURNS TRIGGER AS $$
DECLARE
    task_name TEXT;
    project_owner_id INT;
BEGIN
    -- Получаем название задачи
    SELECT t.task_name INTO task_name
    FROM task t
    WHERE t.task_id = NEW.task_id;

    -- Получаем ID владельца проекта, к которому относится задача
    SELECT p.worker_id INTO project_owner_id
    FROM task t
    JOIN project p ON t.project_id = p.project_id
    WHERE t.task_id = NEW.task_id;

    -- Если назначаемый сотрудник — это владелец проекта, не создаём уведомление
    IF NEW.worker_id = project_owner_id THEN
        RETURN NEW;
    END IF;

    -- Вставляем уведомление
    INSERT INTO notification_task (
        worker_id,
        task_id,
        notification_task_text,
        notification_task_time,
        notification_task_viewed,
        notification_task_accepted
    )
    VALUES (
        NEW.worker_id,
        NEW.task_id,
        CONCAT('Вы назначены на задачу ', task_name),
        NOW(),
        FALSE,
        NULL
    );

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;



CREATE TRIGGER trg_create_task_notification
AFTER INSERT ON worker_task
FOR EACH ROW
EXECUTE FUNCTION create_task_notification();


CREATE OR REPLACE FUNCTION remove_worker_from_task()
RETURNS TRIGGER AS $$
BEGIN
    -- Проверяем, что значение было изменено и установлено в FALSE
    IF NEW.notification_task_accepted IS NOT TRUE AND OLD.notification_task_accepted IS DISTINCT FROM NEW.notification_task_accepted THEN
        DELETE FROM worker_task
        WHERE worker_id = NEW.worker_id
          AND task_id = NEW.task_id;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER trg_remove_worker_on_decline
AFTER UPDATE ON notification_task
FOR EACH ROW
EXECUTE FUNCTION remove_worker_from_task();






CREATE OR REPLACE FUNCTION delete_notification_on_worker_task_delete()
RETURNS TRIGGER AS $$
BEGIN
  DELETE FROM notification_task
  WHERE worker_id = OLD.worker_id AND task_id = OLD.task_id;
  RETURN OLD;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER trg_delete_notification
AFTER DELETE ON worker_task
FOR EACH ROW
EXECUTE FUNCTION delete_notification_on_worker_task_delete();

