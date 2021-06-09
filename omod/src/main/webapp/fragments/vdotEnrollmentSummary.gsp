<style>
    .ke-data-point {
        color: saddlebrown;
    }
    .ke-datapoint-value {
        font-style: italic;
    }
    fieldset {
        padding: 10px;
    }
</style>
<fieldset>
    <legend>Details of nimeCONFIRM enrollment</legend>
    <div>
        <div style="float: left; width: 35%;">

            <div>
                <span class="ke-data-point">Consent :</span>
                <span class="ke-datapoint-value">${enrollment.enrollmentData != null ? enrollment.enrollmentData.consent : ""}</span>
            </div>

            <div>
                <span class="ke-data-point">Date of enrollment:</span>
                <span class="ke-datapoint-value">15-May-2020</span>
            </div>
            <div>
                <span class="ke-data-point">Reason for enrollment:</span>
                <span class="ke-datapoint-value">${enrollment.enrollmentData != null ? enrollment.enrollmentData.enrollmentReason : ""}</span>
            </div>
        </div>

        <div style="float: left; width: 30%; text-align: left">
            <div>
                <span class="ke-data-point">Current regimen:</span>
                <span class="ke-datapoint-value">TDF + 3TC + EFV</span>
            </div>

            <div>
                <span class="ke-data-point">Regimen frequency:</span>
                <span class="ke-datapoint-value">Three times daily</span>
            </div>
        </div>
    </div>
    <div style="clear: both; height: 5px;"></div>
    <div>
        <span style="text-decoration: underline">Socio-economic status</span>
        <div style="clear: both; height: 5px;"></div>
        <div style="float: left; width: 35%;">

            <div>
                <span class="ke-data-point">Currently lives with :</span>
                <span class="ke-datapoint-value">${enrollment.baselineData != null ? enrollment.baselineData.livingWith : ""}</span>
            </div>

            <div>
                <span class="ke-data-point">Primary care-giver:</span>
                <span class="ke-datapoint-value">${enrollment.baselineData != null ? enrollment.baselineData.primaryCaregiver : ""}</span>
            </div>

            <div>
                <span class="ke-data-point">Has disclosed HIV status to:</span>
                <span class="ke-datapoint-value">${enrollment.baselineData != null ? enrollment.baselineData.membersAwareOfPatientStatus : ""}</span>
            </div>

            <div>
                <span class="ke-data-point">Household members living with HIV:</span>
                <span class="ke-datapoint-value">${enrollment.baselineData != null ? enrollment.baselineData.hivPositiveHouseholdMembers : ""}</span>
            </div>
        </div>

        <div style="float: left; width: 30%; text-align: left">
            <div>
                <span class="ke-data-point">Primary source of income:</span>
                <span class="ke-datapoint-value">${enrollment.baselineData != null ? enrollment.baselineData.primarySourceOfIncome : ""}</span>
            </div>

            <div>
                <span class="ke-data-point">Range of income:</span>
                <span class="ke-datapoint-value">${enrollment.baselineData != null ? enrollment.baselineData.householdIncome : ""}</span>
            </div>
            <div>
                <span class="ke-data-point">No of people in the household:</span>
                <span class="ke-datapoint-value">${enrollment.baselineData != null ? enrollment.baselineData.noOfHouseholdMembers : ""}</span>
            </div>
            <div>
                <span class="ke-data-point">Availability of pit latrine/toile:</span>
                <span class="ke-datapoint-value">${enrollment.baselineData != null ? enrollment.baselineData.hasLatrineOrToilet : ""}</span>
            </div>
            <div>
                <span class="ke-data-point">Source of drinking water:</span>
                <span class="ke-datapoint-value">${enrollment.baselineData != null ? enrollment.baselineData.sourceOfDrinkingWater : ""}</span>
            </div>
            <div>
                <span class="ke-data-point">Meals taken in a day:</span>
                <span class="ke-datapoint-value">${enrollment.baselineData != null ? enrollment.baselineData.noOfMealsPerDay : ""}</span>
            </div>
        </div>

        <div style="float: left; width: 30%; text-align: left">
            <div>
                <span class="ke-data-point">In school:</span>
                <span class="ke-datapoint-value">${enrollment.baselineData != null ? enrollment.baselineData.inSchool : ""}</span>
            </div>

            <div>
                <span class="ke-data-point">School level:</span>
                <span class="ke-datapoint-value">${enrollment.baselineData != null ? enrollment.baselineData.schoolLevel : ""}</span>
            </div>

            <div>
                <span class="ke-data-point">School type:</span>
                <span class="ke-datapoint-value">${enrollment.baselineData != null ? enrollment.baselineData.schoolType : ""}</span>
            </div>

            <div>
                <span class="ke-data-point">Distance to school:</span>
                <span class="ke-datapoint-value">${enrollment.baselineData != null ? enrollment.baselineData.distanceToSchool : ""}</span>
            </div>

            <div>
                <span class="ke-data-point">Means of transport to school:</span>
                <span class="ke-datapoint-value">${enrollment.baselineData != null ? enrollment.baselineData.meansOfTransport : ""}</span>
            </div>

        </div>
    </div>
</fieldset>

<div style="clear: both; height: 5px;"></div>