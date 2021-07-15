CREATE TABLE public.eg_obm_chb_dtls
(
    id character varying(64) NOT NULL,
    tenantid character varying(64) NOT NULL,
    accountid character varying(64) NOT NULL,
    hallid character varying(64) NOT NULL,
    purpose character varying(64) NOT NULL,
    category character varying(64) NOT NULL,
    purposedescription character varying(500) NOT NULL,
	residenttypeid character varying(64) NOT NULL,
	timeslotid character varying(64) NOT NULL,
    fromdate bigint NOT NULL,
    todate bigint NOT NULL,
    applicationnumber character varying(64) NOT NULL,
    applicationdate bigint NOT NULL,
    action character varying(64),
    status character varying(64),
    createdby character varying(64),
    lastmodifiedby character varying(64),
    createdtime bigint,
    lastmodifiedtime bigint,
    approveddate bigint,
    CONSTRAINT eg_obm_chb_dtls_pkey PRIMARY KEY (id)
);

CREATE TABLE public.eg_obm_chb_applicationdocument
(
    id character varying(64) NOT NULL,
    documenttype character varying(64),
    filestoreid character varying(64) NOT NULL,
    chbdtlid character varying(64) NOT NULL,
    active boolean,
    createdby character varying(64),
    lastmodifiedby character varying(64),
    createdtime bigint,
    lastmodifiedtime bigint,
    CONSTRAINT eg_obm_chb_applicationdocument_pkey PRIMARY KEY (id),
    CONSTRAINT eg_obm_chb_applicationdocument_fkey1 FOREIGN KEY (chbdtlid)
        REFERENCES public.eg_obm_chb_dtls (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE public.eg_obm_chb_bank_dtls
(
    id character varying(64) NOT NULL,
    chbdtlid character varying(64) NOT NULL,
    accountholdername character varying(64) NOT NULL,
    accountnumber character varying(64) NOT NULL,
    nameofbank character varying(64),
    ifsccode character varying(64),
    accounttype character varying(64),
    createdby character varying(64),
    lastmodifiedby character varying(64),
    createdtime bigint,
    lastmodifiedtime bigint,
    CONSTRAINT eg_obm_chb_bank_dtls_pkey PRIMARY KEY (id),
    CONSTRAINT eg_obm_chb_bank_dtls_fkey1 FOREIGN KEY (chbdtlid)
        REFERENCES public.eg_obm_chb_dtls (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE SEQUENCE public.SEQ_EG_OBM_CHB_APL INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1;