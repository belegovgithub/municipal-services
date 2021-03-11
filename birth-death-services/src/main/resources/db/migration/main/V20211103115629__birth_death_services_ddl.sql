ALTER TABLE eg_birth_cert_request
    ADD COLUMN dateofissue timestamp without time zone;
    
ALTER TABLE eg_death_cert_request
    ADD COLUMN dateofissue timestamp without time zone;