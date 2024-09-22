CREATE SEQUENCE IF NOT EXISTS adv_pic_seq START WITH 1 INCREMENT BY 1;

CREATE SEQUENCE IF NOT EXISTS adv_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE adv
(
    id          BIGINT  NOT NULL,
    title       VARCHAR(255),
    description VARCHAR(255),
    price       DECIMAL,
    status      BOOLEAN NOT NULL,
    create_date TIMESTAMP WITHOUT TIME ZONE,
    user_id     BIGINT,
    CONSTRAINT pk_adv PRIMARY KEY (id)
);

CREATE TABLE adv_pics
(
    id             BIGINT NOT NULL,
    url            VARCHAR(255),
    external_token VARCHAR(255),
    adv_id         BIGINT,
    CONSTRAINT pk_adv_pics PRIMARY KEY (id)
);

ALTER TABLE adv
    ADD CONSTRAINT FK_ADV_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE adv_pics
    ADD CONSTRAINT FK_ADV_PICS_ON_ADV FOREIGN KEY (adv_id) REFERENCES adv (id);

ALTER TABLE users
    ALTER COLUMN first_name TYPE VARCHAR(50) USING (first_name::VARCHAR(50));

ALTER TABLE users
    ALTER COLUMN last_name TYPE VARCHAR(50) USING (last_name::VARCHAR(50));

ALTER TABLE users
    ALTER COLUMN phone TYPE VARCHAR(13) USING (phone::VARCHAR(13));