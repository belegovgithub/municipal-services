CREATE TABLE eg_pgdetail
(
    id serial NOT NULL,
    tenantid character varying(256),
    merchantid character varying(256),
    secretkey character varying(256),
    username character varying(256),
    password character varying(256),
    createddate timestamp,
    lastmodifieddate timestamp,
    createdby bigint,
    lastmodifiedby bigint 
);

ALTER TABLE eg_pgdetail ADD CONSTRAINT eg_pgdetail_tenantid_key UNIQUE (tenantid); 
ALTER TABLE eg_pgdetail ADD CONSTRAINT eg_pgdetail_pkey PRIMARY KEY (id);