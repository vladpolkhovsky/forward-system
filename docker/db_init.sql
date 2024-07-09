CREATE SCHEMA IF NOT EXISTS forward_system;

CREATE TABLE IF NOT EXISTS forward_system.users
(
    id              bigint primary key,
    username        varchar(100) unique not null,
    fio             varchar(255)        not null,
    roles           varchar(512)        not null,
    contact         varchar(512)        not null,
    contactTelegram varchar(512)        not null,
    email           varchar(512)        not null,
    other           varchar(2048),
    payment         varchar(255)        not null,
    password        varchar(100)        not null,
    createdAt       timestamp           not null
);


CREATE TABLE IF NOT EXISTS forward_system.authors
(
    id bigint primary key references users(id),
    subjects varchar(2048) not null,
    quality  varchar(512) not null,
    createdBy bigint not null references users(id)
);

create table if not exists forward_system.order_statuses
(
    name varchar(255) primary key
);

create table if not exists forward_system.orders
(
    id bigint primary key,
    name varchar(2048) not null,
    orderStatus varchar(255) not null references order_statuses(name),
    workType varchar(255) not null,
    discipline varchar(2048) not null,
    subject varchar(2048) not null,
    originality int not null,
    verificationSystem varchar(255),
    intermediateDeadlines timestamp not null,
    deadline timestamp not null,
    other varchar(2048),
    takingCost int not null,
    authorCost int not null
);

create table if not exists forward_system.order_participants_type
(
    name varchar(255) primary key
);

create table if not exists forward_system.order_participants
(
    id bigint primary key,
    orderId bigint not null references orders(id),
    userId bigint not null references users(id),
    type varchar(255) not null references order_participants_type(name)
);

create table if not exists forward_system.attachments(
    id bigint primary key,
    filename varchar(2048) not null,
    filepath varchar(2048) not null
);

create table if not exists forward_system.order_attachments(
    id bigint primary key,
    orderId bigint not null references orders(id),
    attachmentId bigint not null references attachments(id)
);