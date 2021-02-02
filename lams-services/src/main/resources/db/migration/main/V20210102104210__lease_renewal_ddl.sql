ALTER TABLE public.eg_lams_leaserenewal
    ADD COLUMN fatherorhusbandname character varying(64);

ALTER TABLE public.eg_lams_leaserenewal
    ADD COLUMN months character varying(5);
    
ALTER TABLE public.eg_lams_leaserenewal_audit
    ADD COLUMN fatherorhusbandname character varying(64);

ALTER TABLE public.eg_lams_leaserenewal_audit
    ADD COLUMN months character varying(5);