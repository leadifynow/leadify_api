ALTER TABLE user_columns
ADD COLUMN name tinyint(1) default 1,
ADD COLUMN event_name tinyint(1) default 1,
ADD COLUMN referral tinyint(1) default 1,
ADD COLUMN business tinyint(1) default 1;