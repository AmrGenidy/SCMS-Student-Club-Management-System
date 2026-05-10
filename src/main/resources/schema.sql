CREATE TABLE IF NOT EXISTS members (
    student_id     VARCHAR(8) PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    email          VARCHAR(100) NOT NULL,
    role           VARCHAR(20)  NOT NULL,
    password_hash  VARCHAR(128) NOT NULL
);

ALTER TABLE members ADD COLUMN IF NOT EXISTS password_hash VARCHAR(128) NOT NULL DEFAULT '';

CREATE TABLE IF NOT EXISTS events (
    event_id          INT PRIMARY KEY,
    name              VARCHAR(100) NOT NULL,
    date              DATE NOT NULL,
    location          VARCHAR(100) NOT NULL,
    quota             INT NOT NULL,
    current_attendees INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS event_signups (
    event_id   INT          NOT NULL,
    student_id VARCHAR(8)   NOT NULL,
    signed_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (event_id, student_id),
    FOREIGN KEY (event_id)   REFERENCES events(event_id)   ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES members(student_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id INT PRIMARY KEY,
    type           VARCHAR(20) NOT NULL,
    amount         DOUBLE PRECISION NOT NULL,
    description    TEXT,
    date           DATE NOT NULL
);

INSERT INTO members (student_id, name, email, role, password_hash)
VALUES (
    '12345678',
    'Admin User',
    'admin@ozu.edu.tr',
    'ADMIN',
    '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9'
)
ON CONFLICT (student_id) DO UPDATE
    SET role          = EXCLUDED.role,
        password_hash = EXCLUDED.password_hash;

INSERT INTO members (student_id, name, email, role, password_hash)
VALUES (
    '87654321',
    'Demo Member',
    'member@ozu.edu.tr',
    'MEMBER',
    '5600376e863d2f57a053518f324ad3840b0bc2348b573af281a7b7cbe7a228c6'
)
ON CONFLICT (student_id) DO UPDATE
    SET role          = EXCLUDED.role,
        password_hash = EXCLUDED.password_hash;

INSERT INTO transactions (transaction_id, type, amount, description, date)
VALUES (1, 'INCOME', 1000.0, 'Initial budget', CURRENT_DATE)
ON CONFLICT (transaction_id) DO NOTHING;
