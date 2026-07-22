DROP INDEX IF EXISTS idx_classes_course_id;
DROP INDEX IF EXISTS idx_user_classes_class_id;
DROP INDEX IF EXISTS idx_user_notifications_user_id;

CREATE INDEX idx_classes_course_id_number_deleted_at
    ON classes (course_id, number, deleted_at);

CREATE INDEX idx_classes_course_id_active_deleted_at_shift
    ON classes (course_id, active, deleted_at, shift);

CREATE INDEX idx_user_classes_class_id_role_class
    ON user_classes (class_id, role_class);

CREATE INDEX idx_user_notifications_visible_by_user
    ON user_notifications (user_id, dismissed_at, created_at);

CREATE INDEX idx_user_notifications_unread_by_user
    ON user_notifications (user_id, read_at, dismissed_at);
