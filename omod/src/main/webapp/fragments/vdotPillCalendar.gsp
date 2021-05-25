<%
	ui.decorateWith("kenyaui", "panel", [ heading: "Patient Pill Calendar" ])


	ui.includeCss("vdot", "fontawesome/css/all.min.css")
	ui.includeCss("vdot", "sb-admin-2.min.css")
	ui.includeCss("vdot", "bootstrap/bootstrap.min.css")
	ui.includeCss("vdot", "customcalendar.css")

	ui.includeJavascript("vdot", "bootstrap/bootstrap.bundle.min.js")
	ui.includeJavascript("vdot", "jquery-easing/jquery.easing.min.js")
	ui.includeJavascript("vdot", "sb-admin-2.min.js")
	ui.includeJavascript("vdot", "customcalendar.js")

%>


<style>
.simple-table {
	border: solid 1px #DDEEEE;
	border-collapse: collapse;
	border-spacing: 0;
	font: normal 15px Arial, sans-serif;
}
.simple-table thead th {
	background-color: #DDEFEF;
	border: solid 1px #DDEEEE;
	color: #336B6B;
	padding: 10px;
	text-align: left;
	text-shadow: 1px 1px 1px #fff;
}
.simple-table td {
	border: solid 1px #DDEEEE;
	color: #333;
	padding: 5px;
	text-shadow: 1px 1px 1px #fff;
}
</style>

<div class="card shadow mb-4">
	<div class="card-header py-3">

	</div>
	<div class="card-body h-100">

		<div class="h-100 justify-content-center align-items-center">

			<form action="" method="POST" onsubmit="event.preventDefault();">
				<div class="form-group">
					<label class="h6 font-weight-bold">Select Month</label>
					<div class="row">
						<div class="col-lg-8">
							<input type="month" id="monthSelect" name="calendar-month"
								   min="2021-02" class="form-control">
						</div>
						<div class="col-lg-4">
							<button type="button" id="btnSearch"
									class="btn btn-outline-primary waves-effect"
									style="width: 100%;">View
							</button>
						</div>
					</div>
				</div>

			</form>

			<hr class="my-4"/>

			<div id="cal">
				<div class="header">
					<span class="left hook"></span>
					<span class="month-year" id="label"> June 20&0 </span>
					<span class="right hook"></span>

				</div>
				<table id="days">

					<td>sun</td>
					<td>mon</td>
					<td>tue</td>
					<td>wed</td>
					<td>thu</td>
					<td>fri</td>
					<td>sat</td>

				</table>
				<div id="cal-frame">
					<table class="curr">
						<tbody>
						<tr>
						</tr>
						</tbody>
					</table>
				</div>
			</div>
		</div>
	</div>
</div>

<script type="text/javascript">
    jQuery(function () {

        patientO = JSON.stringify(${ pillCalendar });
        var cal = CALENDAR();
        cal.init();
    });
</script>
