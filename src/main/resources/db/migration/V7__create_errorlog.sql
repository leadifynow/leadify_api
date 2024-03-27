CREATE TABLE error_log (
    id int auto_increment primary key,
    created_at timestamp default current_timestamp,
    error_message TEXT,
    data TEXT
)