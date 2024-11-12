ALTER TABLE simplelog.logtype
    ADD CONSTRAINT logtype_unique_name UNIQUE (name);

INSERT INTO simplelog.logtype (name)
    VALUES ('DEVICE_ASSIGNED')
    ON CONFLICT DO NOTHING;
    
INSERT INTO simplelog.logtype (name)
    VALUES ('DEVICE_DEASSIGNED')
    ON CONFLICT DO NOTHING;    
