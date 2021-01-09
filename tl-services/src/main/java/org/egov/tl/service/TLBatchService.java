package org.egov.tl.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tl.config.TLConfiguration;
import org.egov.tl.producer.Producer;
import org.egov.tl.repository.TLRepository;
import org.egov.tl.util.NotificationUtil;
import org.egov.tl.util.TradeUtil;
import org.egov.tl.web.models.SMSRequest;
import org.egov.tl.web.models.TradeLicense;
import org.egov.tl.web.models.TradeLicenseRequest;
import org.egov.tl.web.models.TradeLicenseSearchCriteria;
import org.egov.tl.web.models.TradeUnit;
import org.egov.tl.workflow.WorkflowIntegrator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.egov.tl.util.TLConstants.*;


@Slf4j
@Component
public class TLBatchService {



    private NotificationUtil util;

    private TLConfiguration config;

    private TLRepository repository;

    private EnrichmentService enrichmentService;

    private WorkflowIntegrator workflowIntegrator;

    private Producer producer;

    private TradeUtil tradeUtil;
    @Autowired
    public TLBatchService(NotificationUtil util, TLConfiguration config, TLRepository repository,
                          EnrichmentService enrichmentService, WorkflowIntegrator workflowIntegrator,
                          Producer producer,TradeUtil tradeUtil) {
        this.util = util;
        this.config = config;
        this.repository = repository;
        this.enrichmentService = enrichmentService;
        this.workflowIntegrator = workflowIntegrator;
        this.producer = producer;
        this.tradeUtil =tradeUtil;
    }







    /**
     * Searches trade licenses which are expiring and sends reminder sms to
     *  owner's of the licenses
     * @param serviceName
     * @param requestInfo
     */
    public void getLicensesAndPerformAction(String serviceName, String jobName, RequestInfo requestInfo){


        Long validTill = System.currentTimeMillis();

        if(jobName.equalsIgnoreCase(JOB_SMS_REMINDER))
            validTill = validTill + config.getReminderPeriod();

        List<String>  applicableTenants = tradeUtil.getModuleTeants(requestInfo, serviceName);
        if(!CollectionUtils.isEmpty(applicableTenants)) {
        	for (String tenantId : applicableTenants) {
        		TradeLicenseSearchCriteria criteria = TradeLicenseSearchCriteria.builder()
                        .businessService(serviceName)
                        .validTo(validTill)
                        .status(STATUS_APPROVED)
                        .tenantId(tenantId)
                        .limit(config.getPaginationSize())
                        .build();

                int offSet = 0;

                while (true){

                    log.info("current Offset: "+offSet);

                    List<TradeLicense> licenses = repository.getLicenses(criteria);
                    
                    if(CollectionUtils.isEmpty(licenses))
                        break;
                    
                    //Filter application based on constraints : start 
                    List<String> licNos =licenses.stream().map(l->l.getLicenseNumber()).collect(Collectors.toList());
                    log.info("Approved License pending for "+jobName+": "+licNos );
                    //Expire only the license for which renewal application started 
                    if(jobName.equalsIgnoreCase(JOB_EXPIRY)) {
                    	repository.checkAppNotRenewed(licenses);
                    	licNos =licenses.stream().map(l->l.getLicenseNumber()).collect(Collectors.toList());
                        log.info("Applicable TL which will expire  : "+licNos );
                    }
                    
                    if(jobName.equalsIgnoreCase(JOB_SMS_REMINDER)) {
                    	repository.checkAppNotInApprovalCycle(licenses);
                    	licNos =licenses.stream().map(l->l.getLicenseNumber()).collect(Collectors.toList());
                        log.info("Applicable TL sending for reminder : "+licNos );
                    }
                    //Filter application based on constraints : end                      
                    //Process only if license list are not empty
                    if(!CollectionUtils.isEmpty(licenses)) {
                    	licenses = enrichmentService.enrichTradeLicenseSearch(licenses, criteria, requestInfo);

                        if(jobName.equalsIgnoreCase(JOB_SMS_REMINDER))
                            sendReminderSMS(requestInfo, licenses);

                        else if(jobName.equalsIgnoreCase(JOB_EXPIRY)) {
                        	//Workflow through error if NewTL/EDITRenewal both goes for expiry
                        	List<TradeLicense> newTLs= licenses.stream().filter(l->l.getWorkflowCode().equals(DEFAULT_WORKFLOW)).collect(Collectors.toList());
                        	if(!CollectionUtils.isEmpty(newTLs)) {
                        		expireLicenses(requestInfo, newTLs);	
                        	}
                        	licenses.removeAll(newTLs);
                        	if(!CollectionUtils.isEmpty(licenses)) {
                        		expireLicenses(requestInfo, licenses);	
                        	} 
                        }
                    }
                        
                    
                                            

                    offSet = offSet + config.getPaginationSize();

                    criteria.setOffset(offSet);
                }
            }
        }
        


    }


    /**
     * Sends customized reminder sms to the owner's of the given licenses
     * @param licenses The licenses for which reminder has to be send
     */
    private void sendReminderSMS(RequestInfo requestInfo, List<TradeLicense> licenses){

        String tenantId = getStateLevelTenant(licenses.get(0).getTenantId());

        String localizationMessages = util.getLocalizationMessages(tenantId, requestInfo);

        List<SMSRequest> smsRequests = new LinkedList<>();

        for(TradeLicense license : licenses){
            try{

                String message = util.getReminderMsg(license, localizationMessages);
                Map<String,String > mobileNumberToOwner = new HashMap<>();

                license.getTradeLicenseDetail().getOwners().forEach(owner -> {
                    if(owner.getMobileNumber()!=null)
                        mobileNumberToOwner.put(owner.getMobileNumber(),owner.getName());
                });
                smsRequests.addAll(util.createSMSRequest(message,mobileNumberToOwner));
            }
            catch (Exception e){
                e.printStackTrace();
                producer.push(config.getReminderErrorTopic(), license);
            }
        }

        util.sendSMS(smsRequests, config.getIsReminderEnabled());

    }


    /**
     * Calls workflow with action expire on the given license
     * @param requestInfo
     * @param licenses Licenses to be expired
     */
    private void expireLicenses(RequestInfo requestInfo, List<TradeLicense> licenses){

        try {
            licenses.forEach(license -> {
                license.setAction(ACTION_EXPIRE);
                if(StringUtils.isEmpty(license.getWorkflowCode()))
                    license.setWorkflowCode(DEFAULT_WORKFLOW);
            });
            
            workflowIntegrator.callWorkFlow(new TradeLicenseRequest(requestInfo, licenses));
         
            producer.push(config.getUpdateWorkflowTopic(), new TradeLicenseRequest(requestInfo, licenses));
                       
        }
        catch (Exception e){
            e.printStackTrace();
            producer.push(config.getExpiryErrorTopic(), licenses);
        }



    }



    /**
     * Returns state level tenant
     * @param tenantId
     * @return
     */
    private String getStateLevelTenant(String tenantId){
        return tenantId.split("\\.")[0];
    }




}
