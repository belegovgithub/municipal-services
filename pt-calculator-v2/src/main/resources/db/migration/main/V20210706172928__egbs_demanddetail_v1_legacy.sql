CREATE TABLE egbs_demanddetail_v1_legacy
(
    id character varying(64)  NOT NULL,
    demanddtlid character varying(64)  NOT NULL,
    collectionamount numeric(12,2) NOT NULL,
    createdby character varying(256)  NOT NULL,
    createdtime bigint NOT NULL,
    lastmodifiedby character varying(256) ,
    lastmodifiedtime bigint,
    CONSTRAINT egbs_demanddetail_v1_legacy_pkey PRIMARY KEY (id),
    CONSTRAINT egbs_demanddetail_v1_legacy_uniquekey UNIQUE (demanddtlid)
) ;