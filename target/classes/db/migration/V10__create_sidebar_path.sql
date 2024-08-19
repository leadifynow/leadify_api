create table if not exists paths (
    id integer auto_increment not null primary key,
    url varchar(255) null
);

create table if not exists user_path (
    id integer auto_increment not null primary key,
    type_id integer not null,
    url_id integer not null,
    foreign key (type_id) references user_type(id),
    foreign key (url_id) references paths(id)
);