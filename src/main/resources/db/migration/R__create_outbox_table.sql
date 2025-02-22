CREATE TABLE IF NOT EXISTS outbox_messages
(
    id          UUID        NOT NULL PRIMARY KEY,
    type        VARCHAR(50) NOT NULL,
    status      VARCHAR(25) NOT NULL DEFAULT 'NEW' CHECK ( status IN ('NEW', 'DONE') ),
    object_id   BIGINT      NOT NULL,
    object_type VARCHAR(50) NOT NULL,
    payload     TEXT        NOT NULL,
    created_at  TIMESTAMP   NOT NULL DEFAULT LOCALTIMESTAMP,
    updated_at  TIMESTAMP   NOT NULL DEFAULT LOCALTIMESTAMP
);

COMMENT ON TABLE outbox_messages IS 'Messages for outbox';
COMMENT ON COLUMN outbox_messages.id IS 'Message identifier';
COMMENT ON COLUMN outbox_messages.type IS 'Message type';
COMMENT ON COLUMN outbox_messages.status IS 'Message status';
COMMENT ON COLUMN outbox_messages.object_id IS 'Object identifier';
COMMENT ON COLUMN outbox_messages.object_type IS 'Object type';
COMMENT ON COLUMN outbox_messages.payload IS 'Message payload';
COMMENT ON COLUMN outbox_messages.created_at IS 'Record creation time';
COMMENT ON COLUMN outbox_messages.updated_at IS 'Record update time';

CREATE INDEX outbox_messages_not_sent_idx ON outbox_messages (created_at) WHERE status = 'NEW';
