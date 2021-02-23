<% programs.each { descriptor -> %>
${ ui.includeFragment("vdot", "program/programHistory", [ patient: patient, program: descriptor.target, showClinicalData: showClinicalData ]) }
<% } %>