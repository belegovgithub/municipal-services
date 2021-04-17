CREATE TABLE public.eg_emp_declaration_dtls
(
    id character varying(64)  NOT NULL,
    declaredby character varying(64)  NOT NULL,
    declaredon timestamp without time zone NOT NULL,
    agreed character(1) ,
    startdate timestamp without time zone,
    endddate timestamp without time zone,
    CONSTRAINT eg_emp_declaration_dtls_pkey PRIMARY KEY (id)
) ;