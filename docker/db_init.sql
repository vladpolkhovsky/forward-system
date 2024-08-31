CREATE SCHEMA IF NOT EXISTS forward_system;

CREATE TABLE IF NOT EXISTS forward_system.users
(
    id               bigint primary key,
    username         varchar(100) unique not null,
    firstname        varchar(255)        not null,
    lastname         varchar(255)        not null,
    surname          varchar(255),
    roles            varchar(512)        not null,
    contact          varchar(512)        not null,
    contact_telegram varchar(512)        not null,
    email            varchar(512),
    other            varchar(65536),
    payment          varchar(255)        not null,
    password         varchar(100)        not null,
    created_at       timestamp           not null
);

CREATE TABLE IF NOT EXISTS forward_system.authors
(
    id         bigint primary key references forward_system.users (id),
    created_by bigint not null references forward_system.users (id)
);

create table if not exists forward_system.disciplines
(
    id   bigint primary key,
    name varchar(2048) unique not null
);

create table if not exists forward_system.discipline_quality
(
    name varchar(255) primary key
);

create table if not exists forward_system.author_disciplines
(
    id                 bigint primary key,
    author_id          bigint       not null references forward_system.authors (id),
    discipline_quality varchar(255) not null references forward_system.discipline_quality (name),
    discipline_id      bigint       not null references forward_system.disciplines (id)
);

create table if not exists forward_system.order_statuses
(
    name varchar(255) primary key
);

create table if not exists forward_system.orders
(
    id                    bigint primary key,
    name                  varchar(2048) not null,
    tech_number           int           not null,
    order_status          varchar(255)  not null references forward_system.order_statuses (name),
    work_type             varchar(255)  not null,
    discipline_id         bigint        not null references forward_system.disciplines (id),
    subject               varchar(2048) not null,
    originality           int           not null,
    verification_system   varchar(255),
    additional_dates      varchar(65536),
    intermediate_deadline timestamp,
    deadline              timestamp     not null,
    other                 varchar(65536),
    taking_cost           int           not null,
    author_cost           int           not null,
    created_at            timestamp     not null,
    created_by            bigint        not null references forward_system.users (id)
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

create table if not exists forward_system.chat_messages
(
    id                bigint primary key,
    chat_id           bigint       not null references forward_system.chats (id),
    from_user_id      bigint references forward_system.users (id),
    chat_message_type varchar(255) not null references forward_system.chat_message_types (name),
    is_system_message boolean      not null,
    is_hidden         boolean      not null,
    created_at        timestamp    not null,
    content           varchar(65536)
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
    review_message varchar(65536),
    review_verdict varchar(65536),
    review_mark    int,
    review_file_id bigint references forward_system.attachments (id),
    is_reviewed    boolean   not null,
    is_accepted    boolean   not null,
    reviewed_by    bigint references forward_system.users (id),
    review_date    timestamp,
    created_at     timestamp not null
);

create table if not exists forward_system.security_block
(
    id                 bigint primary key,
    user_id            bigint    not null references forward_system.users (id),
    message_id         bigint references forward_system.chat_messages (id),
    reason             varchar(65536),
    is_permanent_block boolean   not null,
    created_at         timestamp not null
);

create table if not exists forward_system.notification_data
(
    id                bigint primary key,
    user_id           bigint        not null references forward_system.users (id),
    unique_number     bigint        not null,
    subscription_data varchar(8192) not null
);

INSERT INTO forward_system.users (id, username, firstname, lastname, surname, roles, contact, contact_telegram, email,
                                  other, payment, password, created_at)
VALUES (0, 'admin', 'Админ', 'Админов', 'Админович', 'OWNER,ADMIN,MANAGER,HR', '+888888888', '+888888888',
        'vpl@mail.ru', '123', '321', '$2a$10$V5PwvJ4Q0GkdGVeLITkjh.FxzzgnwAMJ8FYi1L42Bb4b4QymzmyPC',
        '2024-07-31 13:50:46.688700');

insert into forward_system.order_statuses
values ('CREATED'),
       ('DISTRIBUTION'),
       ('ADMIN_REVIEW'),
       ('IN_PROGRESS'),
       ('REVIEW'),
       ('GUARANTEE'),
       ('FINALIZATION'),
       ('CLOSED');

insert into forward_system.order_participants_type
values ('CATCHER'),
       ('OWNER'),
       ('HOST'),
       ('AUTHOR'),
       ('DECLINE_AUTHOR'),
       ('MAIN_AUTHOR'),
       ('EXPERT');

insert into forward_system.chat_type
values ('REQUEST_ORDER_CHAT'),
       ('ORDER_CHAT'),
       ('ADMIN_TALK_CHAT'),
       ('OTHER_CHAT');

insert into forward_system.chat_message_types
values ('NEW_ORDER'),
       ('NEW_CHAT'),
       ('MESSAGE');

insert into forward_system.discipline_quality
values ('EXCELLENT'),
       ('GOOD'),
       ('MAYBE');

insert into forward_system.disciplines
values (1, 'Бух. учет'),
       (2, 'Аудит'),
       (3, 'Финансы'),
       (4, 'Налоги'),
       (5, 'Экономика'),
       (6, 'Микро-, макро- экономика'),
       (7, 'Экономика предприятия'),
       (8, 'Экономика труда'),
       (9, 'Экономическая безопасность'),
       (10, 'Экономический анализ'),
       (11, 'Мировая экономика'),
       (12, 'ВЭД'),
       (13, 'Международные валютно-кредитные отношения'),
       (14, 'Международные финансово-экономические рассчеты'),
       (15, 'Международные стандарты финансовой отчетности'),
       (16, 'Менеджмент организации'),
       (17, 'Производственный менеджмент'),
       (18, 'Инновационный менеджмент'),
       (19, 'Кадровый менеджмент'),
       (20, 'Технический менеджмент'),
       (21, 'Риск-менеджмент'),
       (22, 'Стратегический менеджмент'),
       (23, 'Финансовый менеджмент'),
       (24, 'Теория управления'),
       (25, 'Управление качеством'),
       (26, 'Антикризисное управление'),
       (27, 'Управление персоналом'),
       (28, 'Маркетинговые технологии управления'),
       (29, 'Управление проектами'),
       (30, 'Управление запасами'),
       (31, 'Бизнес-администрированике'),
       (32, 'Управленческий анализ'),
       (33, 'Планирование и проектирование организаций'),
       (34, 'Моделирование бизнес-процессов'),
       (35, 'Бизнес-анализ'),
       (36, 'Стратегический анализ'),
       (37, 'Стратегическое планирование'),
       (38, 'Бизнес-планирование'),
       (39, 'Проектная деятельность'),
       (40, 'Теория организации'),
       (41, 'АХД'),
       (42, 'Организационное развитие'),
       (43, 'Коммерция'),
       (44, 'Маркетинг'),
       (45, 'Производственный маркетинг'),
       (46, 'Туризм'),
       (47, 'Гостиничное дело'),
       (48, 'Инновации'),
       (49, 'Банковское дело'),
       (50, 'ГМУ'),
       (51, 'Товароведение'),
       (52, 'Эконометрика'),
       (53, 'Деньги'),
       (54, 'Рынок ценных бумаг'),
       (55, 'Страхование'),
       (56, 'Торговое дело'),
       (57, 'Ценообразование и оценка бизнеса'),
       (58, 'Логистика'),
       (59, 'Инвестиции'),
       (60, 'Таможенное дело'),
       (61, 'Языкознание и филология'),
       (62, 'Лингвистика'),
       (63, 'Лексикология'),
       (64, 'Русский язык'),
       (65, 'Английский язык'),
       (66, 'Немецкий язык'),
       (67, 'Перевод'),
       (68, 'Искусство'),
       (69, 'Дизайн'),
       (70, 'Музыка'),
       (71, 'Теория и история искусств'),
       (72, 'Архитектура и урбанистика'),
       (73, 'Скульптура'),
       (74, 'Режиссура'),
       (75, 'Хореография'),
       (76, 'Драматургия'),
       (77, 'Музейное дело'),
       (78, 'Фольклор'),
       (79, 'Актерское дело'),
       (80, 'Журналистика'),
       (81, 'Телевидение'),
       (82, 'Реклама и PR'),
       (83, 'Издетельское дело'),
       (84, 'Связи с общественностью'),
       (85, 'Деловая коммуникация'),
       (86, 'Риторика'),
       (87, 'Религоведение'),
       (88, 'Теология'),
       (89, 'Культурология'),
       (90, 'Востоковедение'),
       (91, 'Африканистика'),
       (92, 'История'),
       (93, 'Краеведение'),
       (94, 'Геральдика и генеалогия'),
       (95, 'Социология'),
       (96, 'Социальная работа'),
       (97, 'Социально-педагогическая работа'),
       (98, 'Педагогика'),
       (99, 'Коррекционная педагогика'),
       (100, 'Дефектология'),
       (101, 'Дошкольное образование'),
       (102, 'Методика преподавания'),
       (103, 'Музыкальное образование'),
       (104, 'Психология'),
       (105, 'Педагогическая психология'),
       (106, 'Клиническая психология'),
       (107, 'Социальная психология'),
       (108, 'Психоанализ'),
       (109, 'Конфликтология'),
       (110, 'Международные отношения'),
       (111, 'Политология'),
       (112, 'Библиотечное дело'),
       (113, 'Документоведение'),
       (114, 'Обществознание'),
       (115, 'Философия'),
       (116, 'Литература'),
       (117, 'Безопасность жизнедеятельности'),
       (118, 'Логика'),
       (119, 'Физическая культура'),
       (120, 'Статистика'),
       (121, 'Криминалистика'),
       (122, 'Уголовное право'),
       (123, 'Административное право'),
       (124, 'Прокурорский надзор'),
       (125, 'Криминология'),
       (126, 'Уголовный процесс'),
       (127, 'Административный процесс'),
       (128, 'Теория государства и права'),
       (129, 'История политических и правовых учений'),
       (130, 'История государства и права'),
       (131, 'Римское частное право'),
       (132, 'Международное частное право'),
       (133, 'Международное публичное право'),
       (134, 'Международное гуманитарное право'),
       (135, 'Право прав человека'),
       (136, 'Гражданское право'),
       (137, 'Семейное право'),
       (138, 'Корпоративное право'),
       (139, 'Жилищное право'),
       (140, 'Земельное право'),
       (141, 'Право интеллектуальной собственности'),
       (142, 'Арбитражный процесс'),
       (143, 'Гражданский процесс'),
       (144, 'Конституционное право'),
       (145, 'Муниципальное право'),
       (146, 'Право соц. обеспечения'),
       (147, 'Трудовое право'),
       (148, 'Охрана труда'),
       (149, 'Экологическое право'),
       (150, 'Медицинское право'),
       (151, 'Логопедия');
