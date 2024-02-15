create table if not exists user_type
(
    id integer auto_increment not null primary key,
    name varchar(255),
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp on update current_timestamp
);

create table if not exists user
(
    id integer auto_increment not null primary key,
    first_name varchar(255) null,
    last_name varchar(255) null,
    email varchar(255) null,
    password varchar(255) null,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp on update current_timestamp,
    type_id integer null,
    foreign key (type_id) references user_type(id)
);

create table if not exists industry
(
    id integer auto_increment not null primary key,
    name varchar(255) null,
    description varchar(255) null,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

create table if not exists company
(
    id integer auto_increment not null primary key,
    name varchar(255) null,
    location varchar(255) null,
    flag varchar(255) null,
    industry_id integer null,
    foreign key (industry_id) references industry(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


create table if not exists workspace
(
    id char(36) not null primary key,
    name varchar(255) null,
    description varchar(255) null,
    company_id integer null,
    foreign key(company_id) references company(id),
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp on update current_timestamp
);

create table if not exists workspace_user
(
    id integer auto_increment not null primary key,
    workspace_id char(36) not null,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp on update current_timestamp,
    user_id integer not null,
    foreign key (workspace_id) references workspace(id),
    foreign key (user_id) references user(id)
);

create table if not exists campaign
(
    id char(36) not null primary key,
    campaign_name varchar(255) not null,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp on update current_timestamp,
    workspace_id char(36) not null,
    foreign key (workspace_id) references workspace(id)
);

create table if not exists stage
(
    id integer not null auto_increment primary key,
    name varchar(255) not null,
    description varchar(255) null,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp on update current_timestamp,
    workspace_id char(36) null,
    position_workspace integer null,
    followup integer null,
    next_update timestamp null,
    color varchar(255) null default "#FFFFFF",
    foreign key (workspace_id) references workspace(id)
);

create table if not exists interested
(
    id integer not null auto_increment primary key,
    event_type varchar(255) not null,
    workspace char(36) not null,
    campaign_id char(36) not null,
    campaign_name varchar(255) not null,
    lead_email varchar(255) null,
    title varchar(255) null,
    email varchar(255) null,
    website varchar(255) null,
    industry varchar(255) null,
    lastName varchar(255) null,
    firstName varchar(255) null,
    number_of_employees integer null,
    companyName varchar(255) null,
    linkedin_url varchar(255) null,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp on update current_timestamp,
    stage_id integer null,
    notes varchar(255) null,
    booked tinyint(1) NOT NULL DEFAULT 0,
    foreign key (workspace) references workspace(id),
    manager integer null,
    foreign key (manager) references user(id),
    next_update timestamp null
);

create table if not exists user_columns
(
    id integer not null auto_increment primary key,
    first_name tinyint(1) default 1,
    last_name tinyint(1) default 1,
    number_of_employees tinyint(1) default 0,
    linkedin tinyint(1) default 0,
    title tinyint(1) default 0,
    campaign_name tinyint(1) default 0,
    industry tinyint(1) default 0,
    company_name tinyint(1) default 1,
    interest_date tinyint(1) default 1,
    notes tinyint(1) default 1,
    website tinyint(1) default 0,
    user_id integer not null,
    manager tinyint(1) default 1,
    foreign key (user_id) references user(id) ON DELETE CASCADE,
    workspace_id char(36) null,
    foreign key (workspace_id) references workspace(id) ON DELETE CASCADE,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp on update current_timestamp,
    next_update tinyint(1) default 1
);

create table if not exists booked
(
    id integer not null auto_increment primary key,
    email varchar(255) null,
    first_name varchar(255) null,
    last_name varchar(255) null,
    name varchar(255) null,
    text_reminder_number varchar(255) null,
    timezone varchar(255) null,
    interested_id integer null,
    foreign key(interested_id) references interested(id),
    company_id integer null,
    foreign key (company_id) references company(id),
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp on update current_timestamp
);

create table if not exists questions_and_answers
(
    id integer not null auto_increment primary key,
    answer varchar(255) null,
    position integer null,
    question varchar(255) null,
    booked_id integer not null,
    foreign key (booked_id) references booked(id) ON DELETE CASCADE,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp on update current_timestamp
);

create table if not exists notifications
(
    id integer not null auto_increment primary key,
    title varchar(255) null,
    description varchar(255) null,
    is_active tinyint(1) default 1,
    user_id integer null,
    foreign key(user_id) references user(id),
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp on update current_timestamp
)