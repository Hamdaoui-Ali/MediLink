-- Initial local reference data for MediLink.
-- Local admin password for development: Admin@12345

INSERT IGNORE INTO roles (name, description)
VALUES
    ('ADMIN', 'Platform administrator with system-level management access'),
    ('DOCTOR', 'Doctor account for managing availability and appointments'),
    ('PATIENT', 'Patient account for searching doctors and booking appointments');

INSERT IGNORE INTO specialties (name, description, status)
VALUES
    ('General Medicine', 'Primary care and general consultations', 'ACTIVE'),
    ('Cardiology', 'Heart and cardiovascular care', 'ACTIVE'),
    ('Dermatology', 'Skin, hair, and nail care', 'ACTIVE'),
    ('Pediatrics', 'Healthcare for infants, children, and adolescents', 'ACTIVE'),
    ('Orthopedics', 'Bones, joints, muscles, and movement care', 'ACTIVE');

INSERT IGNORE INTO users (
    role_id,
    full_name,
    email,
    password_hash,
    phone_number,
    account_status
)
SELECT
    roles.id,
    'Local Admin',
    'admin@medilink.local',
    '$2a$10$Hos.NExd60elcW4YSjuSAu5llNtm/ohMjIlXCXjVJupUMbjatMPX2',
    NULL,
    'ACTIVE'
FROM roles
WHERE roles.name = 'ADMIN';
