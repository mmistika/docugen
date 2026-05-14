-- Enums
CREATE TYPE template_version_status AS ENUM ('DRAFT', 'ACTIVE', 'RETIRED');
CREATE TYPE document_status AS ENUM ('DRAFT', 'FINAL');

-- Tables
CREATE TABLE users
(
    user_id BIGSERIAL PRIMARY KEY,
    auth0_id VARCHAR(255) UNIQUE,
    email   VARCHAR(255) UNIQUE NOT NULL,
    name    VARCHAR(25),
    surname VARCHAR(25),
    image   BYTEA,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE organisations
(
    organisation_id BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE memberships
(
    membership_id   BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users (user_id) ON DELETE RESTRICT,
    organisation_id BIGINT NOT NULL REFERENCES organisations (organisation_id) ON DELETE RESTRICT
);

CREATE TABLE roles
(
    role_id         BIGSERIAL PRIMARY KEY,
    organisation_id BIGINT      NOT NULL REFERENCES organisations (organisation_id) ON DELETE CASCADE,
    name            VARCHAR(50) NOT NULL,
    UNIQUE (organisation_id, name)
);

CREATE TABLE permissions
(
    permission_id BIGSERIAL PRIMARY KEY,
    name          VARCHAR(100) UNIQUE NOT NULL
);

INSERT INTO permissions (name)
VALUES ('template:view'),
       ('template:update'),
       ('document:view'),
       ('document:generate'),
       ('organisation:manage'),
       ('members:manage'),
       ('statistics:view');

CREATE TABLE roles_permissions
(
    role_id       BIGINT REFERENCES roles (role_id) ON DELETE CASCADE,
    permission_id BIGINT REFERENCES permissions (permission_id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE memberships_roles
(
    membership_id BIGINT REFERENCES memberships (membership_id) ON DELETE CASCADE,
    role_id       BIGINT REFERENCES roles (role_id) ON DELETE CASCADE,
    PRIMARY KEY (membership_id, role_id)
);

CREATE TABLE templates
(
    template_id     BIGSERIAL PRIMARY KEY,
    organisation_id BIGINT REFERENCES organisations (organisation_id) ON DELETE RESTRICT,
    name            VARCHAR(255) NOT NULL,
    UNIQUE (organisation_id, name)
);

CREATE TABLE template_versions
(
    template_version_id BIGSERIAL PRIMARY KEY,
    template_id         BIGINT  NOT NULL REFERENCES templates (template_id) ON DELETE RESTRICT,
    version             INTEGER NOT NULL,
    manifest            TEXT    NOT NULL,
    content             TEXT    NOT NULL,
    status              template_version_status DEFAULT 'DRAFT',
    created_at          TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (template_id, version)
);

CREATE TABLE documents
(
    document_id         BIGSERIAL PRIMARY KEY,
    template_version_id BIGINT       NOT NULL REFERENCES template_versions (template_version_id),
    organisation_id     BIGINT       NOT NULL REFERENCES organisations (organisation_id),
    name                VARCHAR(255) NOT NULL,
    status              document_status DEFAULT 'DRAFT',
    data                TEXT            DEFAULT '',
    file                BYTEA        NOT NULL,
    created_at          TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE api_tokens
(
    token_id        BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255)        NOT NULL,
    hash            VARCHAR(255) UNIQUE NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active       BOOLEAN   DEFAULT TRUE,
    expires_at      TIMESTAMP,
    organisation_id BIGINT              NOT NULL REFERENCES organisations (organisation_id) ON DELETE CASCADE
);

CREATE TABLE audit_logs
(
    log_id          BIGSERIAL PRIMARY KEY,
    organisation_id BIGINT      NOT NULL REFERENCES organisations (organisation_id) ON DELETE RESTRICT,
    user_id         BIGINT      NOT NULL REFERENCES users (user_id) ON DELETE RESTRICT,
    entity_type     VARCHAR(50) NOT NULL,
    entity_id       BIGINT      NOT NULL,
    action          VARCHAR(50) NOT NULL,
    timestamp       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata        TEXT      DEFAULT ''
);

CREATE TABLE api_tokens_permissions
(
    token_id      BIGINT REFERENCES api_tokens (token_id) ON DELETE CASCADE,
    permission_id BIGINT REFERENCES permissions (permission_id) ON DELETE CASCADE,
    PRIMARY KEY (token_id, permission_id)
);

-- Constraints

CREATE OR REPLACE FUNCTION initialize_organisation_admin()
    RETURNS TRIGGER AS
$$
DECLARE
    admin_role_id INT;
BEGIN
    INSERT INTO roles (organisation_id, name)
    VALUES (NEW.organisation_id, 'ADMIN')
    RETURNING role_id INTO admin_role_id;

    INSERT INTO roles_permissions (role_id, permission_id)
    SELECT admin_role_id, permission_id
    FROM permissions;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_init_org_admin
    AFTER INSERT
    ON organisations
    FOR EACH ROW
EXECUTE FUNCTION initialize_organisation_admin();

-- Indexes
CREATE INDEX idx_audit_org_timestamp ON audit_logs (organisation_id, timestamp DESC);
CREATE INDEX idx_memberships_user_org ON memberships (user_id, organisation_id);
CREATE INDEX idx_memberships_org ON memberships (organisation_id);
CREATE INDEX idx_roles_org ON roles (organisation_id);
CREATE INDEX idx_templates_org ON templates (organisation_id);
CREATE INDEX idx_template_versions_latest ON template_versions (template_id, status);
CREATE INDEX idx_api_tokens_hash ON api_tokens (hash);