ALTER TABLE public.eg_birth_dtls
    ADD CONSTRAINT eg_birth_dtls_ukey1 UNIQUE (registrationno, tenantid);
    
ALTER TABLE public.eg_death_dtls
    ADD CONSTRAINT eg_death_dtls_ukey1 UNIQUE (registrationno, tenantid);
    
ALTER TABLE public.eg_birth_cert_request
    ADD COLUMN embeddedurl character varying(64);
    
ALTER TABLE public.eg_birth_cert_request_audit
    ADD COLUMN embeddedurl character varying(64);
    
ALTER TABLE public.eg_death_cert_request
    ADD COLUMN embeddedurl character varying(64);
    
ALTER TABLE public.eg_death_cert_request_audit
    ADD COLUMN embeddedurl character varying(64);