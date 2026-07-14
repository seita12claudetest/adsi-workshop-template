-- Seed data for local development
INSERT INTO organizations (id, name, code) VALUES (1, 'サンプル株式会社', 'ORG001');

INSERT INTO departments (id, organization_id, name, code) VALUES (1, 1, '開発部', 'DEV');
INSERT INTO departments (id, organization_id, name, code) VALUES (2, 1, '人事部', 'HR');

INSERT INTO sections (id, department_id, name, code, manager_id) VALUES (1, 1, '第一開発課', 'DEV1', NULL);
INSERT INTO sections (id, department_id, name, code, manager_id) VALUES (2, 1, '第二開発課', 'DEV2', NULL);
INSERT INTO sections (id, department_id, name, code, manager_id) VALUES (3, 2, '人事課', 'HR1', NULL);

-- password: password123
INSERT INTO employees (id, employee_code, name, email, password, role, section_id, hire_date)
VALUES (1, 'EMP001', '田中太郎', 'tanaka@example.com', '$2b$12$eSl7eNDO0A4mwKPT/Fuvw.3whXQc94/vsGg0zsI1AO83QMsqCvLDy', 'ADMIN', 1, '2020-04-01');

INSERT INTO employees (id, employee_code, name, email, password, role, section_id, hire_date)
VALUES (2, 'EMP002', '佐藤花子', 'sato@example.com', '$2b$12$eSl7eNDO0A4mwKPT/Fuvw.3whXQc94/vsGg0zsI1AO83QMsqCvLDy', 'MANAGER', 1, '2019-04-01');

INSERT INTO employees (id, employee_code, name, email, password, role, section_id, hire_date)
VALUES (3, 'EMP003', '鈴木一郎', 'suzuki@example.com', '$2b$12$eSl7eNDO0A4mwKPT/Fuvw.3whXQc94/vsGg0zsI1AO83QMsqCvLDy', 'EMPLOYEE', 2, '2021-04-01');

-- Set managers
UPDATE sections SET manager_id = 2 WHERE id = 1;
UPDATE sections SET manager_id = 2 WHERE id = 2;
