CREATE SCHEMA IF NOT EXISTS simplelog;

CREATE TABLE simplelog.logtype(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR NOT NULL
);

CREATE TABLE simplelog.log(
    id BIGSERIAL PRIMARY KEY,
    device VARCHAR NOT NULL,
    created TIMESTAMP NOT NULL,
    type_ref BIGINT,
    CONSTRAINT fk_logtype
          FOREIGN KEY(type_ref)
            REFERENCES simplelog.logtype(id)
);
