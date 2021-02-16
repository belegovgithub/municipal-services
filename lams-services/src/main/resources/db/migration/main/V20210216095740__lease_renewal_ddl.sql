ALTER TABLE public.eg_lams_esign_detail
    ALTER COLUMN status TYPE character varying(200) COLLATE pg_catalog."default";

ALTER TABLE public.eg_lams_esign_detail
    ALTER COLUMN errorcode TYPE character varying(50) COLLATE pg_catalog."default";