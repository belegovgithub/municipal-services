CREATE TABLE public.eg_lams_esign_detail
(
    id character varying(64) COLLATE pg_catalog."default" NOT NULL,
    txnid character varying(64) COLLATE pg_catalog."default",
    filestoreid character varying(64) COLLATE pg_catalog."default",
    status character varying(25) COLLATE pg_catalog."default",
    errorcode character varying(25) COLLATE pg_catalog."default",
    surveyid character varying(64) COLLATE pg_catalog."default",
    createdby character varying(64) COLLATE pg_catalog."default",
    lastmodifiedby character varying(64) COLLATE pg_catalog."default",
    createdtime bigint,
    lastmodifiedtime bigint,
    CONSTRAINT eg_lams_esign_detail_pkey PRIMARY KEY (id)
);

CREATE TABLE public.eg_lams_esign_detail_audit
(
    id character varying(64) COLLATE pg_catalog."default" NOT NULL,
    txnid character varying(64) COLLATE pg_catalog."default",
    filestoreid character varying(64) COLLATE pg_catalog."default",
    status character varying(25) COLLATE pg_catalog."default",
    errorcode character varying(25) COLLATE pg_catalog."default",
    surveyid character varying(64) COLLATE pg_catalog."default",
    createdby character varying(64) COLLATE pg_catalog."default",
    lastmodifiedby character varying(64) COLLATE pg_catalog."default",
    createdtime bigint,
    lastmodifiedtime bigint
);

ALTER TABLE public.eg_lams_leaserenewal
    ADD COLUMN fatherorhusbandname character varying(64);

ALTER TABLE public.eg_lams_leaserenewal
    ADD COLUMN months character varying(5);