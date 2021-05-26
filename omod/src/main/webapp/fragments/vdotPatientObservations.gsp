<%
	ui.decorateWith("kenyaui", "panel", [ heading: "NimeCONFIRM adherence trend" ])

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

<div>

	<fieldset>
		<legend>NimeCONFIRM observations</legend>

		<table class="simple-table">
			<tr>
				<th align="left" width="15%">Date</th>
				<th align="left" width="15%">Adherence score (%)</th>
				<th align="left" width="15%">Status</th>
				<th align="left" width="15%">Video timestamps</th>
			</tr>
			<tr>
				<td>12-May-2021</td>
				<td>90</td>
				<td>Active</td>
				<td>Morning,Midday,Evening </td>
			</tr>
			<tr>
				<td>11-May-2021</td>
				<td>80</td>
				<td>Active</td>
				<td>Morning,Midday,Evening </td>
			</tr>
			<tr>
				<td>10-May-2021</td>
				<td>70</td>
				<td>Active</td>
				<td>Morning,Midday,Evening </td>
			</tr>
			<tr>
				<td>09-May-2021</td>
				<td>90</td>
				<td>Active</td>
				<td>Morning,Midday,Evening </td>
			</tr>
		</table>

	</fieldset>

</div>
