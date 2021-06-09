<%
    ui.decorateWith("kenyaemr", "standardPage", [ patient: currentPatient ])

%>

<div class="ke-page-content">
    <table cellpadding="0" cellspacing="0" border="0" width="100%">
        <tr>
            <td width="30%" valign="top">
                ${ ui.includeFragment("kenyaemr", "patient/patientSummary", [ patient: currentPatient ]) }
                <% if(enrolledInVdot) { %>
                    ${ ui.includeFragment("vdot", "vdotPillCalendar", [ patient: currentPatient ]) }
                <% } %>

            </td>
            <td width="55%" valign="top" style="padding-left: 5px">
                <% if(enrolledInVdot) { %>
                ${ ui.includeFragment("vdot", "vdotEnrollmentSummary", [ patient: currentPatient ]) }
                ${ui.includeFragment("vdot", "vdotPatientObservations", [patient: currentPatient])}
                <% } else { %> <h3>No enrollment history in nimeCONFIRM program</h3> <% }%>
            </td>
        </tr>
    </table>

</div>

