create sequence hibernate_sequence;

create table employee (
    id bigint not null auto_increment,
    company_id int,
    firstname varchar,
    lastname varchar,
    birthday date
);
