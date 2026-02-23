-- Users: API is aware of the concept of a user (no auth in scope).
CREATE TABLE users (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

-- Sleep log for "last night": date, time-in-bed interval, total time in bed, morning feeling.
CREATE TABLE sleep_logs (
    id                        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                   UUID            NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    sleep_date                DATE            NOT NULL,
    went_to_bed_at            TIME            NOT NULL,
    got_up_at                 TIME            NOT NULL,
    total_time_in_bed_minutes INTEGER         NOT NULL,
    morning_feeling           VARCHAR(10)     NOT NULL CHECK (morning_feeling IN ('BAD', 'OK', 'GOOD')),
    created_at                TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT uq_sleep_log_user_date UNIQUE (user_id, sleep_date)
);

CREATE INDEX idx_sleep_logs_user_id ON sleep_logs (user_id);
CREATE INDEX idx_sleep_logs_sleep_date ON sleep_logs (user_id, sleep_date DESC);

COMMENT ON TABLE sleep_logs IS 'One row per user per calendar day (sleep date).';
COMMENT ON COLUMN sleep_logs.sleep_date IS 'Calendar date of the sleep (e.g. today for last night).';
COMMENT ON COLUMN sleep_logs.morning_feeling IS 'How the user felt in the morning: BAD, OK, or GOOD.';
