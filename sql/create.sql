alter table authority
drop
foreign key FKn0jr9c8garf53koe63e2ipa8q

drop table if exists authority

drop table if exists member

create table authority (
                           authority_id varchar(255) not null,
                           created_at datetime(6),
                           modified_at datetime(6),
                           authorities varchar(255),
                           member_id varchar(255),
                           primary key (authority_id)
) engine=InnoDB

create table member (
                        member_id varchar(255) not null,
                        created_at datetime(6),
                        modified_at datetime(6),
                        email varchar(255),
                        password varchar(255),
                        picture varchar(255),
                        register_id varchar(255),
                        registration_id varchar(255),
                        user_id varchar(255),
                        username varchar(255),
                        primary key (member_id)
) engine=InnoDB
create index member_email_index on member (email)
create index member_user_id_index on member (user_id)

alter table authority
    add constraint FKn0jr9c8garf53koe63e2ipa8q
        foreign key (member_id)
            references member (member_id)