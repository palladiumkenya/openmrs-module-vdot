<%
	ui.decorateWith("kenyaemr", "standardPage", [ patient: currentPatient, visit: currentVisit ])

	def visit = currentVisit
%>
<div class="ke-page-content">
	${ ui.includeFragment("vdot", "form/enterHtmlForm", [ patient: currentPatient, encounter: encounter, visit: visit, returnUrl: returnUrl ]) }
</div>