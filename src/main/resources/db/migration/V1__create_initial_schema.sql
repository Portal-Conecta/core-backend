CREATE TABLE users (
                       id            UUID        NOT NULL,
                       name          VARCHAR(150) NOT NULL,
                       email         VARCHAR(180) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       active        BOOLEAN      NOT NULL DEFAULT TRUE,
                       avatar_url    VARCHAR(2048),
                       type_user     VARCHAR(30)  NOT NULL,
                       created_at    TIMESTAMPTZ  NOT NULL,
                       updated_at    TIMESTAMPTZ  NOT NULL,
                       deleted_at    TIMESTAMPTZ,
                       created_by    UUID,
                       updated_by    UUID,
                       deleted_by    UUID,

                       CONSTRAINT pk_users PRIMARY KEY (id),
                       CONSTRAINT uk_users_email UNIQUE (email),
                       CONSTRAINT fk_users_created_by FOREIGN KEY (created_by) REFERENCES users (id),
                       CONSTRAINT fk_users_updated_by FOREIGN KEY (updated_by) REFERENCES users (id),
                       CONSTRAINT fk_users_deleted_by FOREIGN KEY (deleted_by) REFERENCES users (id)
);

CREATE TABLE courses (
                         id         UUID         NOT NULL,
                         name       VARCHAR(150) NOT NULL,
                         code       VARCHAR(50)  NOT NULL,
                         created_at TIMESTAMPTZ  NOT NULL,
                         updated_at TIMESTAMPTZ  NOT NULL,
                         deleted_at TIMESTAMPTZ,
                         created_by UUID,
                         updated_by UUID,
                         deleted_by UUID,

                         CONSTRAINT pk_courses PRIMARY KEY (id),
                         CONSTRAINT uk_courses_name UNIQUE (name),
                         CONSTRAINT uk_courses_code UNIQUE (code),
                         CONSTRAINT fk_courses_created_by FOREIGN KEY (created_by) REFERENCES users (id),
                         CONSTRAINT fk_courses_updated_by FOREIGN KEY (updated_by) REFERENCES users (id),
                         CONSTRAINT fk_courses_deleted_by FOREIGN KEY (deleted_by) REFERENCES users (id)
);

CREATE TABLE classes (
                         id         UUID         NOT NULL,
                         shift      VARCHAR(30)  NOT NULL,
                         number     INTEGER      NOT NULL,
                         name       VARCHAR(150) NOT NULL,
                         course_id  UUID         NOT NULL,
                         created_at TIMESTAMPTZ  NOT NULL,
                         updated_at TIMESTAMPTZ  NOT NULL,
                         deleted_at TIMESTAMPTZ,
                         created_by UUID,
                         updated_by UUID,
                         deleted_by UUID,

                         CONSTRAINT pk_classes PRIMARY KEY (id),
                         CONSTRAINT fk_classes_course_id FOREIGN KEY (course_id) REFERENCES courses (id),
                         CONSTRAINT fk_classes_created_by FOREIGN KEY (created_by) REFERENCES users (id),
                         CONSTRAINT fk_classes_updated_by FOREIGN KEY (updated_by) REFERENCES users (id),
                         CONSTRAINT fk_classes_deleted_by FOREIGN KEY (deleted_by) REFERENCES users (id)
);

CREATE INDEX idx_classes_course_id ON classes (course_id);

CREATE TABLE user_classes (
                              user_id    UUID        NOT NULL,
                              class_id   UUID        NOT NULL,
                              role_class VARCHAR(30) NOT NULL,
                              created_at TIMESTAMPTZ NOT NULL,

                              CONSTRAINT pk_user_classes PRIMARY KEY (user_id, class_id),
                              CONSTRAINT fk_user_classes_user_id  FOREIGN KEY (user_id)  REFERENCES users (id),
                              CONSTRAINT fk_user_classes_class_id FOREIGN KEY (class_id) REFERENCES classes (id)
);

CREATE INDEX idx_user_classes_user_id  ON user_classes (user_id);
CREATE INDEX idx_user_classes_class_id ON user_classes (class_id);

CREATE TABLE rooms (
                       id         UUID        NOT NULL,
                       number     INTEGER     NOT NULL,
                       type_room  VARCHAR(30) NOT NULL,
                       created_at TIMESTAMPTZ NOT NULL,
                       updated_at TIMESTAMPTZ NOT NULL,
                       deleted_at TIMESTAMPTZ,
                       created_by UUID,
                       updated_by UUID,
                       deleted_by UUID,

                       CONSTRAINT pk_rooms PRIMARY KEY (id),
                       CONSTRAINT fk_rooms_created_by FOREIGN KEY (created_by) REFERENCES users (id),
                       CONSTRAINT fk_rooms_updated_by FOREIGN KEY (updated_by) REFERENCES users (id),
                       CONSTRAINT fk_rooms_deleted_by FOREIGN KEY (deleted_by) REFERENCES users (id)
);