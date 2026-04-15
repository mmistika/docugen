-- Enums
CREATE TYPE template_version_status AS ENUM ('draft', 'active', 'retired');
CREATE TYPE document_status AS ENUM ('draft', 'final');

-- Tables
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    auth0_id VARCHAR(255) UNIQUE,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(25),
    surname VARCHAR(25),
    image BYTEA,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE organisations (
    organisation_id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE memberships (
    membership_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
    organisation_id INTEGER NOT NULL REFERENCES organisations(organisation_id) ON DELETE RESTRICT
);

CREATE TABLE roles (
    role_id SERIAL PRIMARY KEY,
    organisation_id INTEGER NOT NULL REFERENCES organisations(organisation_id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL,
    UNIQUE (organisation_id, name)
);

CREATE TABLE permissions (
    permission_id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);

INSERT INTO permissions (name) VALUES
    ('template:view'),
    ('template:update'),
    ('document:view'),
    ('document:generate'),
    ('organisation:manage'),
    ('members:manage');

CREATE TABLE roles_permissions (
    role_id INTEGER REFERENCES roles(role_id) ON DELETE CASCADE,
    permission_id INTEGER REFERENCES permissions(permission_id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE memberships_roles (
    membership_id INTEGER REFERENCES memberships(membership_id) ON DELETE CASCADE,
    role_id INTEGER REFERENCES roles(role_id) ON DELETE CASCADE,
    PRIMARY KEY (membership_id, role_id)
);

CREATE TABLE templates (
    template_id SERIAL PRIMARY KEY,
    organisation_id INTEGER REFERENCES organisations(organisation_id) ON DELETE RESTRICT,
    name VARCHAR(255) NOT NULL,
    UNIQUE (organisation_id, name)
);

CREATE TABLE template_versions (
    template_version_id SERIAL PRIMARY KEY,
    template_id INTEGER NOT NULL REFERENCES templates(template_id) ON DELETE RESTRICT,
    version INTEGER NOT NULL,
    manifest TEXT NOT NULL,
    content TEXT NOT NULL,
    status template_version_status DEFAULT 'draft',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (template_id, version)
);

CREATE TABLE documents (
    document_id SERIAL PRIMARY KEY,
    template_version_id INTEGER NOT NULL REFERENCES template_versions(template_version_id),
    organisation_id INTEGER NOT NULL REFERENCES organisations(organisation_id),
    name VARCHAR(255) NOT NULL,
    status document_status DEFAULT 'draft',
    data TEXT DEFAULT '',
    file BYTEA NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_logs (
    log_id SERIAL PRIMARY KEY,
    organisation_id INTEGER NOT NULL REFERENCES organisations(organisation_id) ON DELETE RESTRICT,
    user_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
    entity_type VARCHAR(50) NOT NULL ,
    entity_id INTEGER NOT NULL ,
    action VARCHAR(50) NOT NULL ,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata TEXT DEFAULT ''
);

-- Constraints

CREATE OR REPLACE FUNCTION initialize_organisation_admin()
    RETURNS TRIGGER AS $$
DECLARE
    admin_role_id INT;
BEGIN
    INSERT INTO roles (organisation_id, name)
    VALUES (NEW.organisation_id, 'ADMIN')
    RETURNING role_id INTO admin_role_id;

    INSERT INTO roles_permissions (role_id, permission_id)
    SELECT admin_role_id, permission_id FROM permissions;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_init_org_admin
    AFTER INSERT ON organisations
    FOR EACH ROW EXECUTE FUNCTION initialize_organisation_admin();

-- Indexes
CREATE INDEX idx_audit_org_timestamp ON audit_logs(organisation_id, timestamp DESC);