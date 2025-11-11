const express = require('express');
const bcrypt = require('bcrypt');
const http = require('http');
const multer = require('multer');

const bodyParser = require('body-parser');

const path = require('path');
const fs = require('fs'); 

const fsp = require('fs').promises; 

const { Pool } = require('pg');

// Настройки подключения к базе данных
const pool = new Pool({
  user: 'postgres',      
  host: '192.168.0.26',          
  database: 'diplom_projectplanner',  
  password: '071018', 
  port: 5433,         
});



const app = express();
const PORT = 3000;

app.use(bodyParser.json());

// Настраиваем хранилище для файлов с использованием multer
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    const uploadDir = path.join(__dirname, 'uploads'); // Директория для сохранения файлов
    if (!fs.existsSync(uploadDir)) {
      fs.mkdirSync(uploadDir, { recursive: true });
    }
    cb(null, uploadDir);
  },
  filename: (req, file, cb) => {
    const uniqueName = `${Date.now()}-${file.originalname}`;
    cb(null, uniqueName); 
  },
});

const upload = multer({ storage });
//Вход пользователя
app.post('/login', async (req, res) => {
  const { worker_email, worker_password } = req.body;
  // Проверка обязательных полей
  if (!worker_email || !worker_password) {
    return res.status(400).json({ error: 'Логин или пароль отсутсвуют' });
  }
  try {
    // Получаем пользователя по логину
    const query = 'SELECT * FROM worker WHERE worker_email = $1';
    const result = await pool.query(query, [worker_email]);
    if (result.rows.length === 0) {
      return res.status(401).json({ error: 'Введён неправильный логин' });
    }
    const user = result.rows[0];
    // Сравнение введенного пароля с хешированным паролем в базе
    const isPasswordValid = await bcrypt.compare(worker_password, user.worker_password);
    if (!isPasswordValid) {
      return res.status(401).json({ error: 'Введён неправильный логин или пароль' });
    }
    res.status(200).json({
      message: 'Login successful',
      worker_id: user.worker_id
    });
  } catch (error) {
    console.error('Error during login:', error.message);
    res.status(500).json({ error: 'Internal server error' });
  }
});

//Регистрация пользователя
app.post('/registration', async (req, res) => {
  const { worker_nickname, worker_password, worker_email } = req.body;
  if (!worker_nickname || !worker_email || !worker_password) {
    return res.status(400).json({ error: 'Пропущено обязательное поле' });
  }
  try {
    const saltRounds = 10;
    const hashedPassword = await bcrypt.hash(worker_password, saltRounds);
    const insertUserQuery = `
        INSERT INTO worker (worker_nickname, worker_password, worker_email)
        VALUES ($1, $2, $3)
        RETURNING worker_id;
      `;
    const result = await pool.query(insertUserQuery, [
      worker_nickname,
      hashedPassword,
      worker_email
    ]);
    const userId = result.rows[0].worker_id;
    res.status(201).json({ message: 'Пользователь успешно создан', worker_id: userId });
  } catch (err) {
    if (err.code === '23505') {
      if (err.detail?.includes('worker_nickname')) {
        return res.status(400).json({ error: 'Имя пользователя уже занято' });
      }
      if (err.detail?.includes('worker_email')) {
        return res.status(400).json({ error: 'Почта уже используется' });
      }
    }
    console.error('Ошибка при создании пользователя:', err.message);
    res.status(500).json({ error: 'Внутренняя ошибка сервера' });
  }
});

app.get('/worker/:id', async (req, res) => {
  const workerId = parseInt(req.params.id);
  if (isNaN(workerId)) {
    return res.status(400).json({ error: 'Некорректный ID пользователя' });
  }
  const query = `
    SELECT 
      w.worker_id,
      w.worker_nickname,
      w.worker_name,
      w.worker_lastname,
      w.worker_patronymic,
      w.worker_phone,
      w.worker_email,
      w.worker_filepath,
      (
        SELECT json_agg(
          json_build_object(
            'role_id', r.role_id,
            'role_name', r.role_name
          )
        )
        FROM role_worker rw
        JOIN role_user r ON rw.role_id = r.role_id
        WHERE rw.worker_id = w.worker_id
      ) AS roles,
      (
        SELECT json_agg(
          json_build_object(
            'technology_id', t.technology_id,
            'technology_name', t.technology_name,
            'role_id', r.role_id
          )
        )
        FROM worker_technology wt
        JOIN technology t ON wt.technology_id = t.technology_id
        JOIN role_user r ON t.role_id = r.role_id
        WHERE wt.worker_id = w.worker_id
      ) AS technologies,
      (
        SELECT COUNT(*) FROM project p 
        WHERE p.worker_id = w.worker_id AND p.project_status = true
      ) AS active_projects_count,
      (
        SELECT COUNT(*) FROM project p 
        WHERE p.worker_id = w.worker_id AND p.project_status = false
      ) AS finished_projects_count,
      (
        SELECT EXISTS (
          SELECT 1
          FROM notification_task nt
          WHERE nt.worker_id = w.worker_id AND nt.notification_task_viewed = false
        )
      ) AS has_unread_notifications
    FROM worker w
    WHERE w.worker_id = $1;
  `;
  try {
    const result = await pool.query(query, [workerId]);

    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Пользователь не найден' });
    }

    res.status(200).json(result.rows[0]);
  } catch (error) {
    console.error('Ошибка при получении данных пользователя:', error.message);
    res.status(500).json({ error: 'Ошибка сервера при получении данных' });
  }
});


app.get('/worker/:id/photo', async (req, res) => {
    const userId = req.params.id;
    try {
        const result = await pool.query('SELECT worker_filepath FROM worker WHERE worker_id = $1', [userId]);
        if (result.rows.length === 0) {
            return res.status(404).send('User not found');
        }
        const filePath = result.rows[0].worker_filepath;
        if (!filePath) {
            return res.status(404).send('No photo available');
        }
        const absolutePath = path.join(__dirname, filePath);
        if (!fs.existsSync(absolutePath)) {
            return res.status(404).send('File not found');
        }
        res.sendFile(absolutePath);
    } catch (err) {
        console.error(err);
        res.status(500).send('Server error');
    }
});

app.get('/notifications/:workerId', async (req, res) => {
  const workerId = parseInt(req.params.workerId);
  if (isNaN(workerId)) {
    return res.status(400).json({ error: 'Некорректный ID пользователя' });
  }
  const query = `
    SELECT 
      nt.notification_task_id,
      nt.notification_task_text,
      TO_CHAR(nt.notification_task_time, 'DD.MM.YYYY') AS notification_task_time,
      TRIM(
        COALESCE(aw.worker_nickname, '') || ' ' ||
        COALESCE(aw.worker_name || ' ', '') ||
        COALESCE(aw.worker_lastname, '')
      ) AS author,
      nt.notification_task_viewed AS viewed,
      nt.notification_task_accepted AS accepted
    FROM notification_task nt
    JOIN task t ON nt.task_id = t.task_id
    JOIN project p ON t.project_id = p.project_id
    JOIN worker aw ON p.worker_id = aw.worker_id
    WHERE nt.worker_id = $1
    ORDER BY nt.notification_task_time DESC
  `;
  try {
    const result = await pool.query(query, [workerId]);
    res.status(200).json(result.rows);
  } catch (error) {
    console.error('Ошибка при получении уведомлений:', error.message);
    res.status(500).json({ error: 'Ошибка сервера при получении уведомлений' });
  }
});

app.put('/notifications/viewed', async (req, res) => {
  const notificationIds = req.body.notificationIds;
  if (!Array.isArray(notificationIds) || notificationIds.length === 0) {
    return res.status(400).json({ error: 'Передан некорректный массив ID уведомлений' });
  }
  const client = await pool.connect();
  try {
    const placeholders = notificationIds.map((_, i) => `$${i + 1}`).join(', ');
    const query = `
      UPDATE notification_task
      SET notification_task_viewed = TRUE
      WHERE notification_task_id IN (${placeholders})
    `;
    await client.query(query, notificationIds);
    res.status(200).json({ message: 'Статус уведомлений обновлён' });
  } catch (error) {
    console.error('Ошибка при обновлении уведомлений:', error.message);
    res.status(500).json({ error: 'Ошибка сервера при обновлении уведомлений' });
  } finally {
    client.release();
  }
});

app.put('/notifications/accept', async (req, res) => {
  const { notificationId, accepted } = req.body;
  if (typeof notificationId !== 'number' || typeof accepted !== 'boolean') {
    return res.status(400).json({ error: 'Некорректный формат данных: ожидается { notificationId: number, accepted: boolean }' });
  }
  const client = await pool.connect();
  try {
    const result = await client.query(
      `
      UPDATE notification_task
      SET notification_task_accepted = $1
      WHERE notification_task_id = $2
      `,
      [accepted, notificationId]
    );
    if (result.rowCount === 0) {
      return res.status(404).json({ error: 'Уведомление не найдено' });
    }
    res.status(200).json({ message: 'Статус принятия уведомления обновлён' });
  } catch (error) {
    console.error('Ошибка при обновлении статуса accepted:', error.message);
    res.status(500).json({ error: 'Ошибка сервера при обновлении уведомления' });
  } finally {
    client.release();
  }
});

app.get('/projects/:worker_id', async (req, res) => {
  const worker_id = parseInt(req.params.worker_id); 
  if (isNaN(worker_id)) {
    return res.status(400).json({ error: 'Неверный id пользователя' });
  }
  try {
    const queryp = `
          SELECT 
            p.project_id,
            p.project_name,
            TO_CHAR(p.project_deadline, 'DD.MM.YYYY') AS project_deadline,
            c.category_id,
            CASE 
              WHEN COALESCE(t.total_count, 0) = 0 THEN 0
              ELSE ROUND(100.0 * t.completed_count / t.total_count)
            END AS project_progress
          FROM 
            project p
          LEFT JOIN category c ON p.category_id = c.category_id
          LEFT JOIN (
            SELECT 
              task.project_id,
              COUNT(*) AS total_count,
              COUNT(*) FILTER (WHERE task.task_status = FALSE) AS completed_count
            FROM task
            GROUP BY task.project_id
          ) t ON t.project_id = p.project_id
          WHERE p.worker_id = $1;
        `;
    const resultp = await pool.query(queryp, [worker_id]);
    const queryc = `
        SELECT 
            category_id,
            category_name
        FROM category
        WHERE worker_id = $1;  
      `;
    const resultc = await pool.query(queryc, [worker_id]);
    const response = {
      projects: resultp.rows,
      categories: resultc.rows,
    };
    res.status(200).json(response);
  } catch (err) {
    console.error('Error fetching data:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});




app.get('/worker/by-nickname/:nickname', async (req, res) => {
  const nickname = req.params.nickname;
  try {
    const query = `
          SELECT 
              worker_id,
              worker_nickname,
              worker_name,
              worker_lastname,
              worker_filepath
          FROM worker
          WHERE worker_nickname = $1;
      `;
    const result = await pool.query(query, [nickname]);
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Пользователь не найден' });
    }
    res.status(200).json(result.rows[0]);
  } catch (err) {
    console.error('Ошибка при получении пользователя по никнейму:', err);
    res.status(500).json({ error: 'Ошибка сервера' });
  }
});

app.put('/worker/:id', upload.single('photo'), async (req, res) => {
  const workerId = parseInt(req.params.id);
  if (isNaN(workerId)) {
    return res.status(400).json({ error: 'Некорректный ID пользователя' });
  }
  const {
    nickname,
    name,
    lastname,
    patronymic,
    email,
    roles,
    technologies
  } = req.body;
  const client = await pool.connect();
  try {
    await client.query('BEGIN');
    await client.query(
      `UPDATE worker SET
        worker_nickname = $1,
        worker_name = $2,
        worker_lastname = $3,
        worker_patronymic = $4,
        worker_email = $5
      WHERE worker_id = $6`,
      [nickname, name, lastname, patronymic, email, workerId]
    );
    if (req.file) {
      const { rows } = await client.query(
        'SELECT worker_filepath FROM worker WHERE worker_id = $1',
        [workerId]
      );
      const oldFilePath = rows[0]?.worker_filepath;
      if (oldFilePath) {
        const absolutePath = path.resolve(oldFilePath);
        fs.unlink(absolutePath, (err) => {
          if (err) console.warn('Ошибка при удалении старого файла:', err.message);
        });
      }
      const photoPath = path.join('uploads', req.file.filename);
      await client.query(
        'UPDATE worker SET worker_filepath = $1 WHERE worker_id = $2',
        [photoPath, workerId]
      );
    }
    await client.query('DELETE FROM role_worker WHERE worker_id = $1', [workerId]);
    const parsedRoles = JSON.parse(roles);
    for (const role of parsedRoles) {
      await client.query(
        'INSERT INTO role_worker (worker_id, role_id) VALUES ($1, $2)',
        [workerId, role.role_id]
      );
    }
    await client.query('DELETE FROM worker_technology WHERE worker_id = $1', [workerId]);
    const parsedTechnologies = JSON.parse(technologies);
    for (const tech of parsedTechnologies) {
      await client.query(
        'INSERT INTO worker_technology (worker_id, technology_id) VALUES ($1, $2)',
        [workerId, tech.technology_id]
      );
    }
    await client.query('COMMIT');
    res.status(200).json({ message: 'Данные успешно обновлены' });
  } catch (error) {
    await client.query('ROLLBACK');
    console.error('Ошибка при обновлении пользователя:', error.message);
    res.status(500).json({ error: 'Ошибка при обновлении данных' });
  } finally {
    client.release();
  }
});

app.get('/categories-and-friends/:worker_id', async (req, res) => {
  const worker_id = parseInt(req.params.worker_id);
  if (isNaN(worker_id)) {
    return res.status(400).json({ error: 'Неверный id пользователя' });
  }
  try {
    const categoryQuery = `
          SELECT 
              category_id,
              category_name
          FROM category
          WHERE worker_id = $1;
      `;
    const categoryResult = await pool.query(categoryQuery, [worker_id]);
    // Запрос друзей пользователя (где user_id = worker_id)
    const friendsQuery = `
          SELECT 
              w.worker_id,
              w.worker_nickname,
              w.worker_name,
              w.worker_lastname,
              w.worker_filepath
          FROM friends_list f
          JOIN worker w ON f.worker_id = w.worker_id
          WHERE f.user_id = $1;
      `;
    const friendsResult = await pool.query(friendsQuery, [worker_id]);
    const response = {
      categories: categoryResult.rows,
      friends: friendsResult.rows
    };
    res.status(200).json(response);
  } catch (err) {
    console.error('Error fetching categories or friends:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});
// Добавление друга
app.post('/friends', async (req, res) => {
  const { user_id, worker_id } = req.body;
  if (!user_id || !worker_id) {
    return res.status(400).json({ error: 'user_id и worker_id обязательны' });
  }
  try {
    const query = `
          INSERT INTO friends_list (user_id, worker_id)
          VALUES 
              ($1, $2),
              ($2, $1)
          ON CONFLICT (user_id, worker_id) DO NOTHING;
      `;
    await pool.query(query, [user_id, worker_id]);
    res.status(201).json({ message: 'Запись добавлена (или уже существует)' });
  } catch (err) {
    console.error('Ошибка при добавлении друга:', err);
    res.status(500).json({ error: 'Ошибка сервера' });
  }
});

// Удаление друга 
app.delete('/friends/:user_id/:worker_id', async (req, res) => {
  const userId = parseInt(req.params.user_id);
  const workerId = parseInt(req.params.worker_id);
  if (isNaN(userId) || isNaN(workerId)) {
    return res.status(400).json({ error: 'Некорректные ID пользователей' });
  }
  try {
    const query = `
      DELETE FROM friends_list
      WHERE (user_id = $1 AND worker_id = $2)
         OR (user_id = $2 AND worker_id = $1);
    `;
    await pool.query(query, [userId, workerId]);
    res.status(200).json({ message: 'Друзья удалены' });
  } catch (err) {
    console.error('Ошибка при удалении друга:', err);
    res.status(500).json({ error: 'Ошибка сервера' });
  }
});


// Получение списка друзей (worker) по user_id
app.get('/workers/by-user/:userId', async (req, res) => {
  const userId = req.params.userId;
  try {
    const query = `
      SELECT 
        w.worker_id,
        w.worker_nickname,
        w.worker_name,
        w.worker_lastname,
        w.worker_filepath
      FROM friends_list f
      JOIN worker w ON f.worker_id = w.worker_id
      WHERE f.user_id = $1;
    `;
    const result = await pool.query(query, [userId]);
    res.status(200).json(result.rows);
  } catch (err) {
    console.error('Ошибка при получении списка друзей по user_id:', err);
    res.status(500).json({ error: 'Ошибка сервера' });
  }
});


app.get('/projects/assigned/:worker_id', async (req, res) => {
  const worker_id = parseInt(req.params.worker_id);
  if (isNaN(worker_id)) {
    return res.status(400).json({ error: 'Неверный id пользователя' });
  }
  try {
    const query = `
      SELECT DISTINCT
        p.project_id,
        p.project_name,
        TO_CHAR(p.project_deadline, 'DD.MM.YYYY') AS project_deadline,
        CASE 
          WHEN COALESCE(t.total_count, 0) = 0 THEN 0
          ELSE ROUND(100.0 * t.completed_count / t.total_count)
        END AS project_progress
      FROM 
        worker_task wt
      JOIN task tsk ON wt.task_id = tsk.task_id
      JOIN project p ON tsk.project_id = p.project_id
      LEFT JOIN (
        SELECT 
          t.project_id,
          COUNT(*) AS total_count,
          COUNT(*) FILTER (WHERE t.task_status = FALSE) AS completed_count
        FROM task t
        GROUP BY t.project_id
      ) t ON t.project_id = p.project_id
      WHERE wt.worker_id = $1;
    `;
    const result = await pool.query(query, [worker_id]);
    res.status(200).json({ projects: result.rows });
  } catch (err) {
    console.error('Error fetching assigned projects:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

//Создание проекта
app.post('/project', async (req, res) => {
  const {
    worker_id,
    project_name,
    project_description,
    project_daystart,
    project_deadline,
    category_id,
    team = [], 
  } = req.body; 
  if (!project_name || !project_daystart || !project_deadline) {
    return res.status(400).json({ error: 'Пропущены обязательные поля' });
  }
  function parseDate(dateString) {
    const [day, month, year] = dateString.split('.').map(Number);
    return new Date(year, month - 1, day); // Месяцы в JS начинаются с 0
  }
  const startDate = parseDate(project_daystart);
  const endDate = parseDate(project_deadline);
  if (isNaN(startDate) || isNaN(endDate)) {
    return res.status(400).json({ error: 'Invalid date format. Use DD.MM.YYYY' });
  }
  if (startDate > endDate) {
    return res
      .status(400)
      .json({ error: 'Дата окончания не может быть раньше даты начала' });
  }
  let c_id = null
  if (category_id == -1) c_id = null; else c_id = category_id
  const client = await pool.connect(); 
  try {
    await client.query('BEGIN'); 
    const projectResult = await client.query(
      `
          INSERT INTO Project (
            worker_id, project_name, project_description, 
            project_daystart, project_deadline, category_id
          ) VALUES ($1, $2, $3, $4, $5, $6)
          RETURNING project_id;
          `,
      [
        worker_id,
        project_name,
        project_description || null,
        project_daystart,
        project_deadline,
        c_id,
      ]
    );
    const project_id = projectResult.rows[0].project_id;
    const fullTeam = new Set(team);
    fullTeam.add(worker_id); 
    for (const user_id of fullTeam) {
      await client.query(
        `
        INSERT INTO worker_project (worker_id, project_id)
        VALUES ($1, $2);
        `,
        [user_id, project_id]
      );
    }
    await client.query('COMMIT'); 
    res.status(201).json({
      project_id,
      worker_id,
      project_name,
      project_description,
      project_daystart,
      project_deadline,
      category_id,
      team: Array.from(fullTeam),
    });
  } catch (error) {
    await client.query('ROLLBACK'); 
    console.error('Ошибка при создании проекта:', error.message);
    if (error.code === '23503') {
      res.status(400).json({ error: 'Ошибка данных команды' });
    } else if (error.code === '23505') {
      res.status(409).json({ error: 'Вы уже имеете проект с таким названием' });
    } else {
      res.status(500).json({ error: 'Internal server error' });
    }
  } finally {
    client.release(); 
  }
});

app.get('/project/:id', async (req, res) => {
  const projectId = parseInt(req.params.id);
  if (isNaN(projectId)) {
    return res.status(400).json({ error: 'Invalid project ID' });
  }
  try {
    const query = `
        SELECT 
          p.worker_id,
          p.project_name,
          p.project_description,
          p.project_status,
          TO_CHAR(p.project_daystart, 'DD.MM.YYYY') AS project_daystart,
          TO_CHAR(p.project_deadline, 'DD.MM.YYYY') AS project_deadline,
          c.category_name,
          (
            SELECT json_agg(
              json_build_object(
                'task_id', t.task_id,
                'task_name', t.task_name,
                'task_status', t.task_status
              )
            )
            FROM task t
            WHERE t.project_id = p.project_id
          ) AS tasks,
          (
            SELECT json_build_object(
              'report_title', rp.report_name,
              'report_type', rp.report_filepath
            )
            FROM report_project rp
            WHERE rp.project_id = p.project_id
            LIMIT 1
          ) AS report_title,
          (
  SELECT json_agg(
    json_build_object(
      'content_id', cp.content_p_id,
      'worker', json_build_object(
        'worker_id', w.worker_id,
        'worker_nickname', w.worker_nickname,
        'worker_name', w.worker_name,
        'worker_lastname', w.worker_lastname,
        'worker_filepath', w.worker_filepath
      ),
      'content_name', cp.content_p_name || LOWER(SUBSTRING(cp.content_p_filepath FROM '\.[^\.]+$')),
      'content_text', cp.content_p_text,
      'content_filepath', cp.content_p_filepath,
      'content_type', CASE 
          WHEN LOWER(cp.content_p_filepath) ~ '\.jpg$|\.jpeg$|\.png$|\.gif$|\.bmp$|\.webp$' THEN 'image'
          WHEN LOWER(cp.content_p_filepath) ~ '\.doc$|\.docx$|\.txt$' THEN 'doc'
          WHEN LOWER(cp.content_p_filepath) ~ '\.pdf$' THEN 'pdf'
          WHEN LOWER(cp.content_p_filepath) ~ '\.mp4$|\.avi$|\.mov$|\.mkv$' THEN 'mp4'
          WHEN LOWER(cp.content_p_filepath) ~ '\.mp3$|\.wav$|\.aac$' THEN 'mp3'
          WHEN LOWER(cp.content_p_filepath) ~ '\.xls$|\.xlsx$|\.csv$' THEN 'xls'
          ELSE 'other'
      END
    )
  )
  FROM content_project cp
  JOIN worker w ON w.worker_id = cp.worker_id
  WHERE cp.project_id = p.project_id
) AS contents,
          (
            SELECT json_agg(
              json_build_object(
                'worker_id', w.worker_id,
                'worker_nickname', w.worker_nickname,
                'worker_name', w.worker_name,
                'worker_lastname', w.worker_lastname,
                'worker_filepath', w.worker_filepath
              )
            )
            FROM worker_project tp
            JOIN worker w ON w.worker_id = tp.worker_id
            WHERE tp.project_id = p.project_id
          ) AS workers
        FROM project p
        LEFT JOIN category c ON p.category_id = c.category_id
        WHERE p.project_id = $1;
      `;

    const result = await pool.query(query, [projectId]);

    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Project not found' });
    }
    res.status(200).json(result.rows[0]);
  } catch (error) {
    console.error('Error fetching project:', error.message);
    res.status(500).json({ error: 'Internal server error' });
  }
});


app.get('/project/:id/workers', async (req, res) => {
  const projectId = parseInt(req.params.id);
  if (isNaN(projectId)) {
    return res.status(400).json({ error: 'Invalid project ID' });
  }
  try {
    const query = `
      SELECT 
        w.worker_id,
        w.worker_nickname,
        w.worker_name,
        w.worker_lastname,
        w.worker_filepath
      FROM worker_project tp
      JOIN worker w ON w.worker_id = tp.worker_id
      WHERE tp.project_id = $1;
    `;
    const result = await pool.query(query, [projectId]);
    res.status(200).json(result.rows);
  } catch (error) {
    console.error('Error fetching workers:', error.message);
    res.status(500).json({ error: 'Internal server error' });
  }
});

app.put('/project/:id', async (req, res) => {
  const project_id = parseInt(req.params.id, 10);
  const {
    project_name,
    project_description,
    project_daystart,
    project_deadline,
    category_id,
    team = [], 
  } = req.body;
  if (!project_name || !project_daystart || !project_deadline) {
    return res.status(400).json({ error: 'Пропущены обязательные поля' });
  }
  function parseDate(dateString) {
    const [day, month, year] = dateString.split('.').map(Number);
    return new Date(year, month - 1, day);
  }
  const startDate = parseDate(project_daystart);
  const endDate = parseDate(project_deadline);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  if (isNaN(startDate) || isNaN(endDate)) {
    return res.status(400).json({ error: 'Неверный формат даты. Используйте DD.MM.YYYY' });
  }
  if (startDate > endDate) {
    return res.status(400).json({ error: 'Дата окончания не может быть раньше даты начала' });
  }
  const c_id = category_id === -1 ? null : category_id;
  const client = await pool.connect();
  try {
    await client.query('BEGIN');
    const oldTeamRes = await client.query(
      `SELECT worker_id FROM worker_project WHERE project_id = $1`,
      [project_id]
    );
    const oldTeam = oldTeamRes.rows.map(row => row.worker_id);

    const removedMembers = oldTeam.filter(id => !team.includes(id));

    if (removedMembers.length > 0) {
      await client.query(
        `
        DELETE FROM worker_task
        WHERE worker_id = ANY($1::int[])
          AND task_id IN (
            SELECT task_id FROM task WHERE project_id = $2
          );
        `,
        [removedMembers, project_id]
      );
    }
    await client.query(
      `
      UPDATE Project SET
        project_name = $1,
        project_description = $2,
        project_daystart = $3,
        project_deadline = $4,
        category_id = $5
      WHERE project_id = $6
      `,
      [
        project_name,
        project_description || null,
        project_daystart,
        project_deadline,
        c_id,
        project_id,
      ]
    );
    await client.query(
      `DELETE FROM worker_project WHERE project_id = $1`,
      [project_id]
    );
    const result = await client.query(
      'SELECT worker_id FROM project WHERE project_id = $1',
      [project_id]
    );
    const worker_id = result.rows[0]?.worker_id;
    const fullTeam = new Set(team);
    fullTeam.add(worker_id); 
    for (const user_id of fullTeam) {
      await client.query(
        `
        INSERT INTO worker_project (worker_id, project_id)
        VALUES ($1, $2);
        `,
        [user_id, project_id]
      );
    }
    await client.query('COMMIT');
    res.status(200).json({
      message: 'Проект успешно обновлен',
      project_id,
      project_name,
      project_description,
      project_daystart,
      project_deadline,
      category_id: c_id,
      team,
    });
  } catch (error) {
    await client.query('ROLLBACK');
    console.error('Ошибка при обновлении проекта:', error.message);
    if (error.code === '23505') {
      res.status(409).json({ error: 'Проект с таким названием уже существует' });
    } else {
      res.status(500).json({ error: 'Ошибка сервера' });
    }
  } finally {
    client.release();
  }
});

app.put('/project/:id/toggle-status', async (req, res) => {
  const projectId = parseInt(req.params.id);
  if (isNaN(projectId)) {
    return res.status(400).json({ error: 'Некорректный ID проекта' });
  }
  try {
    const result = await pool.query(
      `
      UPDATE project
      SET project_status = NOT project_status
      WHERE project_id = $1
      RETURNING project_id, project_status;
      `,
      [projectId]
    );
    if (result.rowCount === 0) {
      return res.status(404).json({ error: 'Проект не найден' });
    }
    res.status(200).json({
      message: 'Статус проекта успешно обновлён',
      project: result.rows[0],
    });
  } catch (error) {
    console.error('Ошибка при обновлении статуса проекта:', error.message);
    res.status(500).json({ error: 'Внутренняя ошибка сервера' });
  }
});

app.delete('/project/:id', async (req, res) => {
  const project_id = parseInt(req.params.id);
  if (isNaN(project_id)) {
    return res.status(400).json({ error: 'Неверный ID проекта' });
  }
  try {
    const result = await pool.query(`
      DELETE FROM project
      WHERE project_id = $1
      RETURNING *;
    `, [project_id]);

    if (result.rowCount === 0) {
      return res.status(404).json({ error: 'Проект не найден' });
    }

    res.status(200).json({ message: 'Проект успешно удалён' });
  } catch (err) {
    console.error('Ошибка при удалении проекта:', err);
    res.status(500).json({ error: 'Ошибка сервера при удалении проекта' });
  }
});

app.post('/category', async (req, res) => {
  const { category_name, user_id } = req.body;

  if (!category_name || !user_id) {
    return res.status(400).json({ error: 'Отсутствует имя категории или ID пользователя' });
  }

  try {
    const insertQuery = `
      INSERT INTO category (category_name, worker_id)
      VALUES ($1, $2)
      RETURNING category_id, category_name;
    `;

    const result = await pool.query(insertQuery, [category_name, user_id]);

    const category = result.rows[0];

    res.status(201).json({
      category_id: category.category_id,
      category_name: category.category_name
    });
  } catch (err) {
    if (err.code === '23505') {
      res.status(409).json({ error: 'Категория с таким именем уже существует у пользователя' });
    } else {
      console.error('Ошибка при добавлении категории:', err);
      res.status(500).json({ error: 'Ошибка сервера при добавлении категории' });
    }
  }
});

app.post('/task', async (req, res) => {
  const { task_name, task_description, task_daystart, task_deadline, team_members } = req.body;
  const project_id = parseInt(req.query.project_id);
  if (!task_name || !task_daystart || !task_deadline || isNaN(project_id)) {
    return res.status(400).json({ error: 'Пропущены обязательные поля' });
  }
  const client = await pool.connect();
  try {
    await client.query('BEGIN');

    const insertTaskQuery = `
      INSERT INTO task (project_id, task_name, task_description, task_daystart, task_deadline)
      VALUES ($1, $2, $3, $4, $5)
      RETURNING task_id;
    `;
    const result = await client.query(insertTaskQuery, [
      project_id,
      task_name,
      task_description || '',
      task_daystart,
      task_deadline
    ]);
    const task_id = result.rows[0].task_id;

    if (Array.isArray(team_members)) {
      const insertWorkerTaskQuery = `
        INSERT INTO worker_task (worker_id, task_id)
        VALUES ($1, $2);
      `;

      for (const member of team_members) {
        await client.query(insertWorkerTaskQuery, [member, task_id]);
      }
    }

    await client.query('COMMIT');
    res.status(201).json({ message: 'Задача успешно создана', task_id });
  } catch (err) {
    await client.query('ROLLBACK');
    console.error('Error adding task:', err);
    res.status(500).json({ error: 'Failed to create task' });
  } finally {
    client.release();
  }
});

// Получение задачи по id
app.get('/task/:id', async (req, res) => {
  const taskId = parseInt(req.params.id);
  if (isNaN(taskId)) {
    return res.status(400).json({ error: 'Неверное id задачи' });
  }
  const query = `
    SELECT
    t.task_name,
    t.task_description,
    TO_CHAR(t.task_daystart, 'DD.MM.YYYY') AS task_daystart,
    TO_CHAR(t.task_deadline, 'DD.MM.YYYY') AS task_deadline,
    t.task_status,
  
    -- team_members
    COALESCE(
      json_agg(
        DISTINCT jsonb_build_object(
          'worker_id', w.worker_id,
          'worker_nickname', w.worker_nickname,
          'worker_name', w.worker_name,
          'worker_lastname', w.worker_lastname,
          'worker_filepath', w.worker_filepath
        )
      ) FILTER (WHERE w.worker_id IS NOT NULL),
      '[]'
    ) AS team_members,
  
    -- task_content
    -- task_content
COALESCE(
  json_agg(
    DISTINCT jsonb_build_object(
      'content_id', ct.content_task_id,
      'content_name', ct.content_task_name,
      'content_text', ct.content_task_text,
      'content_filepath', ct.content_task_filepath||LOWER(SUBSTRING(ct.content_task_filepath FROM '\.[^\.]+$')),
      'content_type', CASE 
        WHEN LOWER(ct.content_task_filepath) ~ '\.jpg$|\.jpeg$|\.png$|\.gif$|\.bmp$|\.webp$' THEN 'image'
        WHEN LOWER(ct.content_task_filepath) ~ '\.doc$|\.docx$|\.txt$' THEN 'doc'
        WHEN LOWER(ct.content_task_filepath) ~ '\.pdf$' THEN 'pdf'
        WHEN LOWER(ct.content_task_filepath) ~ '\.mp4$|\.avi$|\.mov$|\.mkv$' THEN 'mp4'
        WHEN LOWER(ct.content_task_filepath) ~ '\.mp3$|\.wav$|\.aac$' THEN 'mp3'
        WHEN LOWER(ct.content_task_filepath) ~ '\.xls$|\.xlsx$|\.csv$' THEN 'xls'
        ELSE 'other'
      END,
      'worker', jsonb_build_object(
        'worker_id', w2.worker_id,
        'worker_nickname', w2.worker_nickname,
        'worker_name', w2.worker_name,
        'worker_lastname', w2.worker_lastname,
        'worker_filepath', w2.worker_filepath
      )
    )
  ) FILTER (WHERE ct.content_task_id IS NOT NULL),
  '[]'
) AS task_content
  FROM task t
  LEFT JOIN worker_task wt ON wt.task_id = t.task_id
  LEFT JOIN worker w ON w.worker_id = wt.worker_id
  LEFT JOIN content_task ct ON ct.task_id = t.task_id
    AND ct.worker_id = wt.worker_id
  LEFT JOIN worker w2 ON w2.worker_id = ct.worker_id
  WHERE t.task_id = $1
  GROUP BY t.task_id;
  `;
  try {
    const result = await pool.query(query, [taskId]);

    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Задача не найдена' });
    }

    res.status(200).json(result.rows[0]);
  } catch (err) {
    console.error('Error fetching task:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

app.put('/task/:id', async (req, res) => {
  const task_id = parseInt(req.params.id);
  const {
    task_name,
    task_description,
    task_daystart,
    task_deadline,
    team_members
  } = req.body;
  if (!task_name || !task_daystart || !task_deadline || isNaN(task_id)) {
    return res.status(400).json({ error: 'Пропущены обязательные поля или некорректный ID' });
  }
  const client = await pool.connect();
  try {
    await client.query('BEGIN');
    const updateTaskQuery = `
      UPDATE task
      SET task_name = $1,
          task_description = $2,
          task_daystart = $3,
          task_deadline = $4
      WHERE task_id = $5;
    `;
    await client.query(updateTaskQuery, [
      task_name,
      task_description || '',
      task_daystart,
      task_deadline,
      task_id
    ]);
    await client.query(`DELETE FROM worker_task WHERE task_id = $1;`, [task_id]);
    if (Array.isArray(team_members)) {
      const insertWorkerTaskQuery = `
        INSERT INTO worker_task (worker_id, task_id)
        VALUES ($1, $2);
      `;
      for (const worker_id of team_members) {
        await client.query(insertWorkerTaskQuery, [worker_id, task_id]);
      }
    }
    await client.query('COMMIT');
    res.status(200).json({ message: 'Задача успешно обновлена' });
  } catch (err) {
    await client.query('ROLLBACK');
    console.error('Error updating task:', err);
    res.status(500).json({ error: 'Ошибка при обновлении задачи' });
  } finally {
    client.release();
  }
});

app.put('/task/:id/toggle-status', async (req, res) => {
  const taskId = parseInt(req.params.id);

  if (isNaN(taskId)) {
    return res.status(400).json({ error: 'Неверный id задачи' });
  }
  try {
    const result = await pool.query(
      `
      UPDATE task
      SET task_status = NOT task_status
      WHERE task_id = $1
      RETURNING task_id, task_status;
      `,
      [taskId]
    );
    if (result.rowCount === 0) {
      return res.status(404).json({ error: 'Задача не найдена' });
    }
    res.status(200).json({
      message: 'Задача успешно завершина',
      task: result.rows[0],
    });
  } catch (error) {
    console.error('Error toggling task status:', error.message);
    res.status(500).json({ error: 'Internal server error' });
  }
});

app.delete('/task/:id', async (req, res) => {
  const task_id = parseInt(req.params.id);
  if (isNaN(task_id)) {
    return res.status(400).json({ error: 'Неверный ID задачи' });
  }
  try {
    const result = await pool.query(`
      DELETE FROM task
      WHERE task_id = $1
      RETURNING *;
    `, [task_id]);

    if (result.rowCount === 0) {
      return res.status(404).json({ error: 'Задача не найдена' });
    }

    res.status(200).json({ message: 'Задача успешно удалена' });
  } catch (err) {
    console.error('Ошибка при удалении задачи:', err);
    res.status(500).json({ error: 'Ошибка сервера при удалении задачи' });
  }
});

// Получение всех ролей
app.get('/roles', async (req, res) => {
  try {
    const query = `SELECT role_id, role_name FROM role_user;`;
    const result = await pool.query(query);
    res.status(200).json(result.rows);
  } catch (err) {
    console.error('Error fetching roles:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Получение технологий по роли
app.get('/technologies', async (req, res) => {
  try {
    const query = `
          SELECT technology_id, technology_name, role_id
          FROM technology;
      `;
    const result = await pool.query(query);
    res.status(200).json(result.rows);
  } catch (err) {
    console.error('Error fetching technologies:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

app.post('/content-task/:worker_id/:task_id', upload.single('file'), async (req, res) => {
  const { title, text } = req.body;
  const worker_id = parseInt(req.params.worker_id);
  const task_id = parseInt(req.params.task_id);

  if (!worker_id || !task_id || !title || !req.file) {
    return res.status(400).json({ error: 'Required fields are missing' });
  }
  const filePath = path.join('uploads', req.file.filename);
  try {
    const query = `
      INSERT INTO content_task (
        worker_id,
        task_id,
        content_task_name,
        content_task_filepath,
        content_task_text
      ) VALUES ($1, $2, $3, $4, $5)
      RETURNING *;
    `;
    const values = [worker_id, task_id, title, filePath, text || null];
    const result = await pool.query(query, values);
    res.status(201).json({
      message: 'Файл успешно загружен и данные сохранены',
      content: result.rows[0],
    });
  } catch (error) {
    console.error('Ошибка при загрузке контента:', error.message);
    try {
      await fsp.unlink(filePath);
      console.log('Файл удалён после ошибки');
    } catch (unlinkErr) {
      console.error('Не удалось удалить файл:', unlinkErr.message);
    }

    res.status(500).json({ error: 'Ошибка сервера при сохранении данных' });
  }
});

app.post('/content-project/:worker_id/:project_id', upload.single('file'), async (req, res) => {
  const { title, text } = req.body;
  const worker_id = parseInt(req.params.worker_id, 10);
  const project_id = parseInt(req.params.project_id, 10);
  if (!worker_id || !project_id || !title || !req.file) {
    return res.status(400).json({ error: 'Required fields are missing' });
  }
  const filePath = path.join('uploads', req.file.filename);
  try {
    const query = `
      INSERT INTO content_project (
        worker_id,
        project_id,
        content_p_name,
        content_p_filepath,
        content_p_text
      ) VALUES ($1, $2, $3, $4, $5)
      RETURNING *;
    `;
    const values = [worker_id, project_id, title, filePath, text || null];
    const result = await pool.query(query, values);

    res.status(201).json({
      message: 'Файл успешно загружен и контент проекта сохранён',
      content: result.rows[0],
    });
  } catch (error) {
    console.error('Ошибка при сохранении контента проекта:', error.message);
    try {
      await fsp.unlink(filePath);
      console.log('Файл удалён после ошибки');
    } catch (unlinkErr) {
      console.error('Не удалось удалить файл:', unlinkErr.message);
    }

    res.status(500).json({ error: 'Ошибка сервера при сохранении данных' });
  }
});

app.get('/files/:filename', (req, res) => {
  const filePath = path.join(__dirname, '', req.params.filename);
  res.download(filePath, err => {
    if (err) {
      res.status(404).json({ error: 'Файл не найден' });
    }
  });
});

// Запуск сервера
app.listen(PORT, () => {
  console.log(`Сервер запущен на http://192.168.0.170:${PORT}`);
});