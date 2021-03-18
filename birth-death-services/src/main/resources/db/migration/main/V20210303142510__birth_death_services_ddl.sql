CREATE TABLE public.eg_death_dtls
(
    id character varying(64)  NOT NULL,
    registrationno character varying(64)  NOT NULL,
    hospitalname character varying(500) ,
    dateofreport timestamp without time zone,
    dateofdeath timestamp without time zone NOT NULL,
    firstname character varying(200) ,
    middlename character varying(200) ,
    lastname character varying(200) ,
    placeofdeath character varying(1000) ,
    informantsname character varying(200) ,
    informantsaddress character varying(1000) ,
    createdtime bigint,
    createdby character varying(64) ,
    lastmodifiedtime bigint,
    lastmodifiedby character varying(64) ,
    counter smallint,
    tenantid character varying(50)  NOT NULL,
    gender smallint NOT NULL,
    remarks character varying(1000) ,
    hospitalid character varying(64) ,
    eidno character varying(100) ,
    aadharno character varying(150) ,
    nationality character varying(100) ,
    religion character varying(100) ,
    icdcode character varying(200) ,
    age character varying(100) ,
    CONSTRAINT eg_death_dtls_pkey PRIMARY KEY (id),
    CONSTRAINT eg_death_dtls_ukey1 UNIQUE (registrationno, tenantid),
    CONSTRAINT eg_death_dtls_fkey1 FOREIGN KEY (hospitalid)
        REFERENCES public.eg_birth_death_hospitals (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE public.eg_death_father_info
(
    id character varying(64)  NOT NULL,
    firstname character varying(200) ,
    middlename character varying(200) ,
    lastname character varying(200) ,
    aadharno character varying(150) ,
    emailid character varying(150) ,
    mobileno character varying(150) ,
    createdtime bigint,
    createdby character varying(64) ,
    lastmodifiedtime bigint,
    lastmodifiedby character varying(64) ,
    deathdtlid character varying(64)  NOT NULL,
    CONSTRAINT eg_death_father_info_pkey PRIMARY KEY (id),
    CONSTRAINT eg_death_father_info_fkey1 FOREIGN KEY (deathdtlid)
        REFERENCES public.eg_death_dtls (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE public.eg_death_mother_info
(
    id character varying(64)  NOT NULL,
    firstname character varying(200) ,
    middlename character varying(200) ,
    lastname character varying(200) ,
    aadharno character varying(150) ,
    emailid character varying(150) ,
    mobileno character varying(150) ,
    createdtime bigint,
    createdby character varying(64) ,
    lastmodifiedtime bigint,
    lastmodifiedby character varying(64) ,
    deathdtlid character varying(64)  NOT NULL,
    CONSTRAINT eg_death_mother_info_pkey PRIMARY KEY (id),
    CONSTRAINT eg_death_mother_info_fkey1 FOREIGN KEY (deathdtlid)
        REFERENCES public.eg_death_dtls (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE public.eg_death_permaddr
(
    id character varying(64)  NOT NULL,
    buildingno character varying(1000) ,
    houseno character varying(1000) ,
    streetname character varying(1000) ,
    locality character varying(1000) ,
    tehsil character varying(1000) ,
    district character varying(100) ,
    city character varying(100) ,
    state character varying(100) ,
    pinno character varying(100) ,
    country character varying(100) ,
    createdby character varying(64) ,
    createdtime bigint,
    lastmodifiedby character varying(64) ,
    lastmodifiedtime bigint,
    deathdtlid character varying(64) ,
    CONSTRAINT eg_death_permaddr_pkey PRIMARY KEY (id),
    CONSTRAINT eg_death_permaddr_fkey1 FOREIGN KEY (deathdtlid)
        REFERENCES public.eg_death_dtls (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE public.eg_death_presentaddr
(
    id character varying(64)  NOT NULL,
    buildingno character varying(1000) ,
    houseno character varying(1000) ,
    streetname character varying(1000) ,
    locality character varying(1000) ,
    tehsil character varying(1000) ,
    district character varying(100) ,
    city character varying(100) ,
    state character varying(100) ,
    pinno character varying(100) ,
    country character varying(100) ,
    createdby character varying(64) ,
    createdtime bigint,
    lastmodifiedby character varying(64) ,
    lastmodifiedtime bigint,
    deathdtlid character varying(64) ,
    CONSTRAINT eg_death_presentaddr_pkey PRIMARY KEY (id),
    CONSTRAINT eg_death_presentaddr_fkey1 FOREIGN KEY (deathdtlid)
        REFERENCES public.eg_death_dtls (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE public.eg_death_spouse_info
(
    id character varying(64)  NOT NULL,
    firstname character varying(30) ,
    middlename character varying(30) ,
    lastname character varying(30) ,
    aadharno character varying(150) ,
    emailid character varying(150) ,
    mobileno character varying(150) ,
    createdtime bigint,
    createdby character varying(64) ,
    lastmodifiedtime bigint,
    lastmodifiedby character varying(64) ,
    deathdtlid character varying(64) ,
    CONSTRAINT eg_death_spouse_info_pkey PRIMARY KEY (id),
    CONSTRAINT eg_death_spouse_info_fkey1 FOREIGN KEY (deathdtlid)
        REFERENCES public.eg_death_dtls (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE public.eg_death_cert_request
(
    id character varying(64)  NOT NULL,
    deathcertificateno character varying(25)  NOT NULL,
    createdby character varying(64)  NOT NULL,
    createdtime bigint NOT NULL,
    deathdtlid character varying(64) ,
    filestoreid character varying(256) ,
    lastmodifiedtime bigint,
    lastmodifiedby character varying(64) ,
    status character varying(25) ,
    additionaldetail jsonb,
    embeddedurl character varying(64) ,
    dateofissue bigint,
    CONSTRAINT eg_death_cert_request_pkey PRIMARY KEY (id),
    CONSTRAINT eg_death_cert_request_fkey1 FOREIGN KEY (deathdtlid)
        REFERENCES public.eg_death_dtls (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE public.eg_death_cert_request_audit
(
    id character varying(64)  NOT NULL,
    deathcertificateno character varying(25)  NOT NULL,
    createdby character varying(64)  NOT NULL,
    createdtime bigint NOT NULL,
    deathdtlid character varying(64) ,
    filestoreid character varying(256) ,
    lastmodifiedtime bigint,
    lastmodifiedby character varying(64) ,
    status character varying(25) ,
    additionaldetail jsonb,
    embeddedurl character varying(64) ,
    dateofissue bigint
);