CREATE TABLE "users"
(
    "u_id"            bigserial PRIMARY KEY,
    "u_name"          varchar(25)  NOT NULL,
    "u_email"         varchar(320) NOT NULL,
    "u_password"      varchar(50)  NOT NULL,
    "design_theme_id" bigint       NOT NULL
);

CREATE TABLE "design_themes"
(
    "dt_id"   bigserial PRIMARY KEY,
    "dt_name" varchar(10) NOT NULL
);

CREATE TABLE "diagrams"
(
    "d_id"                    bigserial PRIMARY KEY,
    "d_name"                  varchar(100) NOT NULL,
    "d_creation_date"         timestamp    NOT NULL,
    "d_modified_date"         timestamp,
    "d_code"                  text,
    "group_id"                bigint,
    "owner_id"                bigint       NOT NULL,
    "diagram_access_level_id" bigint       NOT NULL
);

CREATE TABLE "diagram_access_levels"
(
    "dal_id"   bigserial PRIMARY KEY,
    "dal_name" varchar(25) NOT NULL
);

CREATE TABLE "diagram_history"
(
    "dh_id"            bigserial PRIMARY KEY,
    "dh_modified_date" timestamp    NOT NULL,
    "dh_name"          varchar(100) NOT NULL,
    "dh_code"          text,
    "diagram_id"       bigint       NOT NULL,
    "user_id"          bigint       NOT NULL
);

CREATE TABLE "groups"
(
    "g_id"                  bigserial PRIMARY KEY,
    "g_name"                varchar(100) NOT NULL,
    "g_creation_date"       timestamp    NOT NULL,
    "owner_id"              bigint       NOT NULL,
    "group_access_level_id" bigint       NOT NULL
);

CREATE TABLE "group_access_levels"
(
    "gal_id"   bigserial PRIMARY KEY,
    "gal_name" varchar(25) NOT NULL
);

CREATE TABLE "group_users"
(
    "gu_id"         bigserial PRIMARY KEY,
    "gu_entry_date" timestamp NOT NULL,
    "group_id"      bigint    NOT NULL,
    "user_id"       bigint    NOT NULL,
    "role_id"       bigint    NOT NULL
);

CREATE TABLE "group_users_roles"
(
    "gur_id"   bigserial PRIMARY KEY,
    "gur_name" varchar(15) NOT NULL
);

ALTER TABLE "users"
    ADD FOREIGN KEY ("design_theme_id") REFERENCES "design_themes" ("dt_id");

ALTER TABLE "diagrams"
    ADD FOREIGN KEY ("group_id") REFERENCES "groups" ("g_id");

ALTER TABLE "diagrams"
    ADD FOREIGN KEY ("owner_id") REFERENCES "users" ("u_id");

ALTER TABLE "diagrams"
    ADD FOREIGN KEY ("diagram_access_level_id") REFERENCES "diagram_access_levels" ("dal_id");

ALTER TABLE "diagram_history"
    ADD FOREIGN KEY ("diagram_id") REFERENCES "diagrams" ("d_id");

ALTER TABLE "diagram_history"
    ADD FOREIGN KEY ("user_id") REFERENCES "users" ("u_id");

ALTER TABLE "groups"
    ADD FOREIGN KEY ("owner_id") REFERENCES "users" ("u_id");

ALTER TABLE "groups"
    ADD FOREIGN KEY ("group_access_level_id") REFERENCES "group_access_levels" ("gal_id");

ALTER TABLE "group_users"
    ADD FOREIGN KEY ("group_id") REFERENCES "groups" ("g_id");

ALTER TABLE "group_users"
    ADD FOREIGN KEY ("user_id") REFERENCES "users" ("u_id");

ALTER TABLE "group_users"
    ADD FOREIGN KEY ("role_id") REFERENCES "group_users_roles" ("gur_id");



INSERT INTO "design_themes" ("dt_name")
VALUES ('light'),
       ('dark');

INSERT INTO "diagram_access_levels" ("dal_name")
VALUES ('read access'),
       ('read and write access'),
       ('access is closed');

INSERT INTO "group_access_levels" ("gal_name")
VALUES ('entry access'),
       ('access is closed');

INSERT INTO "group_users_roles" ("gur_name")
VALUES ('participant'),
       ('admin');