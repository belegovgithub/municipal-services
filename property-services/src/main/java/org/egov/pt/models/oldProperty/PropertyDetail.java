package org.egov.pt.models.oldProperty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.egov.pt.models.Institution;
import org.egov.pt.models.OwnerInfo;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


/**
 * PropertyDetail
 */
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2018-05-11T14:12:44.497+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class PropertyDetail {
       /* @JsonProperty("id")
        private String id;*/

    /**
     * Source of a assessment data. The properties will be created in a system based on the data avaialble in their manual records or during field survey. There can be more from client to client.
     */

    @Valid
    @JsonProperty("institution")
    private OldInstitution institution;

    @Size(max=256)
    @JsonProperty("tenantId")
    private String tenantId;

    @Valid
    @JsonProperty("citizenInfo")
    private OldOwnerInfo citizenInfo;


    public enum SourceEnum {
        MUNICIPAL_RECORDS("MUNICIPAL_RECORDS"),

        FIELD_SURVEY("FIELD_SURVEY"),

        LEGACY_RECORD("LEGACY_RECORD"),

        SYSTEM("SYSTEM");

        private String value;

        SourceEnum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static SourceEnum fromValue(String text) {
            for (SourceEnum b : SourceEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    @JsonProperty("source")
    private SourceEnum source;


    public enum StatusEnum {

        ACTIVE("ACTIVE"),

        INACTIVE("INACTIVE");


        private String value;

        StatusEnum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static StatusEnum fromValue(String text) {
            for (StatusEnum b : StatusEnum.values()) {
                if (String.valueOf(b.value).equalsIgnoreCase(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    @JsonProperty("status")
    private StatusEnum status;

    @Size(max=64)
    @JsonProperty("usage")
    private String usage;

    @JsonProperty("noOfFloors")
    private Long noOfFloors;
    
    @JsonProperty("noOfFlats")
    private Long noOfFlats;

    @JsonProperty("landArea")
    private Float landArea;

    @JsonProperty("buildUpArea")
    private Float buildUpArea;

    @Valid
    @JsonProperty("units")
    private List<OldUnit> units;

    @JsonProperty("documents")
    @Valid
    private Set<OldDocument> documents;

    @JsonProperty("additionalDetails")
    private Object additionalDetails;

    @NotNull
    @Size(max=64)
    @JsonProperty("financialYear")
    private String financialYear;

    @Size(max=64)
    @JsonProperty("propertyType")
    private String propertyType;

    @Size(max=64)
    @JsonProperty("propertySubType")
    private String propertySubType;

    @Size(max=64)
    @JsonProperty("assessmentNumber")
    private String assessmentNumber;

    @JsonProperty("assessmentDate")
    private Long assessmentDate;

    @Size(max=64)
    @JsonProperty("usageCategoryMajor")
    private String usageCategoryMajor;

    @Size(max=64)
    @JsonProperty("usageCategoryMinor")
    private String usageCategoryMinor;

    @Size(max=64)
    @JsonProperty("ownershipCategory")
    private String ownershipCategory;

    @Size(max=64)
    @JsonProperty("subOwnershipCategory")
    private String subOwnershipCategory;

    @JsonProperty("adhocExemption")
    private BigDecimal adhocExemption;

    @JsonProperty("adhocPenalty")
    private BigDecimal adhocPenalty;

    @Size(max=1024)
    @JsonProperty("adhocExemptionReason")
    private String adhocExemptionReason;

    @Size(max=1024)
    @JsonProperty("adhocPenaltyReason")
    private String adhocPenaltyReason;

    @JsonProperty("owners")
    @Valid
    @NotNull
    @Size(min=1)
    private Set<OldOwnerInfo> owners;


    @JsonProperty("auditDetails")
    private OldAuditDetails auditDetails;

    @JsonProperty("calculation")
    private Calculation calculation;



    /**
     * Property can be created from different channels Eg. System (properties created by ULB officials), CFC Counter (From citizen faciliation counters) etc. Here we are defining some known channels, there can be more client to client.
     */
    public enum ChannelEnum {
        SYSTEM("SYSTEM"),

        CFC_COUNTER("CFC_COUNTER"),

        CITIZEN("CITIZEN"),

        DATA_ENTRY("DATA_ENTRY"),

        MIGRATION("MIGRATION");

        private String value;

        ChannelEnum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static ChannelEnum fromValue(String text) {
            for (ChannelEnum b : ChannelEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    @JsonProperty("channel")
    private ChannelEnum channel;


    public PropertyDetail addUnitsItem(OldUnit unitsItem) {
        if (this.units == null) {
            this.units = new ArrayList<>();
        }
        if(!this.units.contains(unitsItem))
            this.units.add(unitsItem);
        return this;
    }

    public PropertyDetail addDocumentsItem(OldDocument documentsItem) {
        if (this.documents == null) {
            this.documents = new HashSet<>();
        }
        this.documents.add(documentsItem);
        return this;
    }


    public PropertyDetail addOwnersItem(OldOwnerInfo ownersItem) {
        if (this.owners == null) {
            this.owners = new HashSet<>();
        }
        this.owners.add(ownersItem);
        return this;
    }

}
