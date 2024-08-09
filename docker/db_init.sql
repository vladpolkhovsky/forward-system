CREATE SCHEMA IF NOT EXISTS forward_system;

CREATE TABLE IF NOT EXISTS forward_system.users
(
    id               bigint primary key,
    username         varchar(100) unique not null,
    firstname        varchar(255)        not null,
    lastname         varchar(255)        not null,
    surname          varchar(255)        not null,
    roles            varchar(512)        not null,
    contact          varchar(512)        not null,
    contact_telegram varchar(512)        not null,
    email            varchar(512)        not null,
    other            varchar(2048),
    payment          varchar(255)        not null,
    password         varchar(100)        not null,
    created_at       timestamp           not null
);

INSERT INTO forward_system.users (id, username, firstname, lastname, surname, roles, contact, contact_telegram, email,
                                  other, payment, password, created_at)
VALUES (0, 'admin', 'Админ', 'Админов', 'Админович', 'OWNER,ADMIN,MANAGER,HR', '+888888888', '+888888888',
        'vpl@mail.ru', '123', '321', '$2a$10$V5PwvJ4Q0GkdGVeLITkjh.FxzzgnwAMJ8FYi1L42Bb4b4QymzmyPC',
        '2024-07-31 13:50:46.688700');

CREATE TABLE IF NOT EXISTS forward_system.authors
(
    id         bigint primary key references forward_system.users (id),
    subjects   varchar(2048) not null,
    quality    varchar(512)  not null,
    created_by bigint        not null references forward_system.users (id)
);

create table if not exists forward_system.order_statuses
(
    name varchar(255) primary key
);

insert into forward_system.order_statuses
values ('CREATED'),
       ('DISTRIBUTION'),
       ('ADMIN_REVIEW'),
       ('IN_PROGRESS'),
       ('REVIEW'),
       ('GUARANTEE'),
       ('FINALIZATION'),
       ('CLOSED');

create table if not exists forward_system.orders
(
    id                    bigint primary key,
    name                  varchar(2048) not null,
    tech_number           int           not null,
    order_status          varchar(255)  not null references forward_system.order_statuses (name),
    work_type             varchar(255)  not null,
    discipline            varchar(2048) not null,
    subject               varchar(2048) not null,
    originality           int           not null,
    verification_system   varchar(255),
    intermediate_deadline timestamp     not null,
    deadline              timestamp     not null,
    other                 varchar(2048),
    taking_cost           int           not null,
    author_cost           int           not null,
    created_at            timestamp     not null
);

create table if not exists forward_system.update_order_request
(
    id                          bigint primary key,
    order_id                    bigint       not null references forward_system.orders (id),
    update_request_from_user_id bigint       not null references forward_system.users (id),
    catcher_ids                 varchar(1024),
    authors_ids                 varchar(1024),
    hosts_ids                   varchar(1024),
    experts_ids                 varchar(1024),
    new_status                  varchar(255) not null references forward_system.order_statuses (name),
    is_viewed                   boolean,
    is_accepted                 boolean,
    created_at                  timestamp
);

create table if not exists forward_system.order_participants_type
(
    name varchar(255) primary key
);

insert into forward_system.order_participants_type
values ('CATCHER'),
       ('OWNER'),
       ('HOST'),
       ('AUTHOR'),
       ('DECLINE_AUTHOR'),
       ('MAIN_AUTHOR'),
       ('EXPERT');

create table if not exists forward_system.order_participants
(
    id       bigint primary key,
    order_id bigint       not null references forward_system.orders (id),
    user_id  bigint       not null references forward_system.users (id),
    type     varchar(255) not null references forward_system.order_participants_type (name),
    fee      int
);

create table if not exists forward_system.attachments
(
    id       bigint primary key,
    filename varchar(2048) not null,
    filepath varchar(2048) not null
);

create table if not exists forward_system.order_attachments
(
    id            bigint primary key,
    order_id      bigint not null references forward_system.orders (id),
    attachment_id bigint not null references forward_system.attachments (id)
);

create table if not exists forward_system.chat_type
(
    name varchar(255) primary key
);

insert into forward_system.chat_type
values ('REQUEST_ORDER_CHAT'),
       ('ORDER_CHAT'),
       ('OTHER_CHAT');

create table if not exists forward_system.chats
(
    id                bigint primary key,
    chat_name         varchar(1024) not null,
    order_id          bigint references forward_system.orders (id),
    type              varchar(255)  not null references forward_system.chat_type (name),
    last_message_date timestamp     not null
);

create table if not exists forward_system.chat_members
(
    id      bigint primary key,
    chat_id bigint not null references forward_system.chats (id),
    user_id bigint not null references forward_system.users (id)
);

create table if not exists forward_system.chat_message_types
(
    name varchar(255) primary key
);

insert into forward_system.chat_message_types
values ('NEW_ORDER'),
       ('NEW_CHAT'),
       ('MESSAGE');

create table if not exists forward_system.chat_messages
(
    id                bigint primary key,
    chat_id           bigint       not null references forward_system.chats (id),
    from_user_id      bigint references forward_system.users (id),
    chat_message_type varchar(255) not null references forward_system.chat_message_types (name),
    is_system_message boolean      not null,
    created_at        timestamp    not null,
    content           varchar(16384)
);

create table if not exists forward_system.chat_message_options
(
    id                bigint primary key,
    message_id        bigint       not null references forward_system.chat_messages (id),
    order_participant varchar(255) not null references forward_system.order_participants_type (name),
    option_resolved   boolean      not null,
    content           varchar(2048),
    option_name       varchar(2058)
);

create table if not exists forward_system.chat_message_to_user
(
    id         bigint primary key,
    chat_id    bigint    not null references forward_system.chats (id),
    message_id bigint    not null references forward_system.chat_messages (id),
    user_id    bigint    not null references forward_system.users (id),
    created_at timestamp not null,
    is_viewed  boolean   not null
);

create table if not exists forward_system.chat_message_attachments
(
    id            bigint primary key,
    message_id    bigint not null references forward_system.chat_messages (id),
    attachment_id bigint not null references forward_system.attachments (id)
);

create table if not exists forward_system.reviews
(
    id             bigint primary key,
    order_id       bigint    not null references forward_system.orders (id),
    attachment_id  bigint    not null references forward_system.attachments (id),
    review_message varchar(16384),
    review_verdict varchar(16384),
    review_mark    int,
    review_file_id bigint references forward_system.attachments (id),
    is_reviewed    boolean   not null,
    is_accepted    boolean   not null,
    reviewed_by    bigint references forward_system.users (id),
    review_date    timestamp,
    created_at     timestamp not null
)