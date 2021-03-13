ALTER TABLE eg_birth_dtls
    ALTER COLUMN registrationno SET NOT NULL ;

ALTER TABLE eg_birth_dtls
    ALTER COLUMN tenantid SET NOT NULL ;

ALTER TABLE eg_birth_dtls
    ALTER COLUMN firstname TYPE character varying(200) ;

ALTER TABLE eg_birth_dtls
    ALTER COLUMN middlename TYPE character varying(200) ;

ALTER TABLE eg_birth_dtls
    ALTER COLUMN lastname TYPE character varying(200) ;
    
ALTER TABLE eg_birth_father_info
    ALTER COLUMN firstname TYPE character varying(200) ;

ALTER TABLE eg_birth_father_info
    ALTER COLUMN middlename TYPE character varying(200) ;

ALTER TABLE eg_birth_father_info
    ALTER COLUMN lastname TYPE character varying(200) ;

ALTER TABLE eg_birth_father_info
    ALTER COLUMN birthdtlid SET NOT NULL;

ALTER TABLE eg_birth_mother_info
    ALTER COLUMN firstname TYPE character varying(200) ;

ALTER TABLE eg_birth_mother_info
    ALTER COLUMN middlename TYPE character varying(200) ;

ALTER TABLE eg_birth_mother_info
    ALTER COLUMN lastname TYPE character varying(200) ;

ALTER TABLE eg_birth_mother_info
    ALTER COLUMN birthdtlid SET NOT NULL;
    
ALTER TABLE eg_birth_permaddr
    ALTER COLUMN buildingno TYPE character varying(1000) ;

ALTER TABLE eg_birth_permaddr
    ALTER COLUMN houseno TYPE character varying(1000) ;

ALTER TABLE eg_birth_permaddr
    ALTER COLUMN streetname TYPE character varying(1000) ;

ALTER TABLE eg_birth_permaddr
    ALTER COLUMN locality TYPE character varying(1000) ;

ALTER TABLE eg_birth_permaddr
    ALTER COLUMN tehsil TYPE character varying(1000) ;

ALTER TABLE eg_birth_permaddr
    ALTER COLUMN district TYPE character varying(100) ;

ALTER TABLE eg_birth_permaddr
    ALTER COLUMN city TYPE character varying(100) ;

ALTER TABLE eg_birth_permaddr
    ALTER COLUMN state TYPE character varying(100) ;

ALTER TABLE eg_birth_permaddr
    ALTER COLUMN pinno TYPE character varying(100) ;

ALTER TABLE eg_birth_permaddr
    ALTER COLUMN country TYPE character varying(100) ;

ALTER TABLE eg_birth_presentaddr
    ALTER COLUMN buildingno TYPE character varying(1000) ;

ALTER TABLE eg_birth_presentaddr
    ALTER COLUMN houseno TYPE character varying(1000) ;

ALTER TABLE eg_birth_presentaddr
    ALTER COLUMN streetname TYPE character varying(1000) ;

ALTER TABLE eg_birth_presentaddr
    ALTER COLUMN locality TYPE character varying(1000) ;

ALTER TABLE eg_birth_presentaddr
    ALTER COLUMN tehsil TYPE character varying(1000) ;

ALTER TABLE eg_birth_presentaddr
    ALTER COLUMN district TYPE character varying(100) ;

ALTER TABLE eg_birth_presentaddr
    ALTER COLUMN city TYPE character varying(100) ;

ALTER TABLE eg_birth_presentaddr
    ALTER COLUMN state TYPE character varying(100) ;

ALTER TABLE eg_birth_presentaddr
    ALTER COLUMN pinno TYPE character varying(100) ;

ALTER TABLE eg_birth_presentaddr
    ALTER COLUMN country TYPE character varying(100) ;
    
    
ALTER TABLE eg_death_dtls
    ALTER COLUMN registrationno SET NOT NULL ;

ALTER TABLE eg_death_dtls
    ALTER COLUMN tenantid SET NOT NULL ;

ALTER TABLE eg_death_dtls
    ALTER COLUMN firstname TYPE character varying(200) ;

ALTER TABLE eg_death_dtls
    ALTER COLUMN middlename TYPE character varying(200) ;

ALTER TABLE eg_death_dtls
    ALTER COLUMN lastname TYPE character varying(200) ;
    
ALTER TABLE eg_death_father_info
    ALTER COLUMN firstname TYPE character varying(200) ;

ALTER TABLE eg_death_father_info
    ALTER COLUMN middlename TYPE character varying(200) ;

ALTER TABLE eg_death_father_info
    ALTER COLUMN lastname TYPE character varying(200) ;

ALTER TABLE eg_death_father_info
    ALTER COLUMN deathdtlid SET NOT NULL;

ALTER TABLE eg_death_mother_info
    ALTER COLUMN firstname TYPE character varying(200) ;

ALTER TABLE eg_death_mother_info
    ALTER COLUMN middlename TYPE character varying(200) ;

ALTER TABLE eg_death_mother_info
    ALTER COLUMN lastname TYPE character varying(200) ;

ALTER TABLE eg_death_mother_info
    ALTER COLUMN deathdtlid SET NOT NULL;
    
ALTER TABLE eg_death_permaddr
    ALTER COLUMN buildingno TYPE character varying(1000) ;

ALTER TABLE eg_death_permaddr
    ALTER COLUMN houseno TYPE character varying(1000) ;

ALTER TABLE eg_death_permaddr
    ALTER COLUMN streetname TYPE character varying(1000) ;

ALTER TABLE eg_death_permaddr
    ALTER COLUMN locality TYPE character varying(1000) ;

ALTER TABLE eg_death_permaddr
    ALTER COLUMN tehsil TYPE character varying(1000) ;

ALTER TABLE eg_death_permaddr
    ALTER COLUMN district TYPE character varying(100) ;

ALTER TABLE eg_death_permaddr
    ALTER COLUMN city TYPE character varying(100) ;

ALTER TABLE eg_death_permaddr
    ALTER COLUMN state TYPE character varying(100) ;

ALTER TABLE eg_death_permaddr
    ALTER COLUMN pinno TYPE character varying(100) ;

ALTER TABLE eg_death_permaddr
    ALTER COLUMN country TYPE character varying(100) ;

ALTER TABLE eg_death_presentaddr
    ALTER COLUMN buildingno TYPE character varying(1000) ;

ALTER TABLE eg_death_presentaddr
    ALTER COLUMN houseno TYPE character varying(1000) ;

ALTER TABLE eg_death_presentaddr
    ALTER COLUMN streetname TYPE character varying(1000) ;

ALTER TABLE eg_death_presentaddr
    ALTER COLUMN locality TYPE character varying(1000) ;

ALTER TABLE eg_death_presentaddr
    ALTER COLUMN tehsil TYPE character varying(1000) ;

ALTER TABLE eg_death_presentaddr
    ALTER COLUMN district TYPE character varying(100) ;

ALTER TABLE eg_death_presentaddr
    ALTER COLUMN city TYPE character varying(100) ;

ALTER TABLE eg_death_presentaddr
    ALTER COLUMN state TYPE character varying(100) ;

ALTER TABLE eg_death_presentaddr
    ALTER COLUMN pinno TYPE character varying(100) ;

ALTER TABLE eg_death_presentaddr
    ALTER COLUMN country TYPE character varying(100) ;
    
ALTER TABLE public.eg_death_mother_info DROP COLUMN education;

ALTER TABLE public.eg_death_mother_info DROP COLUMN proffession;

ALTER TABLE public.eg_death_mother_info DROP COLUMN nationality;

ALTER TABLE public.eg_death_mother_info DROP COLUMN religion;

ALTER TABLE public.eg_death_father_info DROP COLUMN education;

ALTER TABLE public.eg_death_father_info DROP COLUMN proffession;

ALTER TABLE public.eg_death_father_info DROP COLUMN nationality;

ALTER TABLE public.eg_death_father_info DROP COLUMN religion;

ALTER TABLE public.eg_death_spouse_info DROP COLUMN education;

ALTER TABLE public.eg_death_spouse_info DROP COLUMN proffession;

ALTER TABLE public.eg_death_spouse_info DROP COLUMN nationality;

ALTER TABLE public.eg_death_spouse_info DROP COLUMN religion;