CREATE TABLE public.eg_birth_cert_request_audit
(
    id character varying(64)  NOT NULL,
    birthcertificateno character varying(25) ,
    createdby character varying(64) ,
    createdtime bigint,
    birthdtlid character varying(64) ,
    lastmodifiedtime bigint,
    lastmodifiedby character varying(64) ,
    filestoreid character varying(256) ,
    status character varying(25) ,
    additionaldetail jsonb
);