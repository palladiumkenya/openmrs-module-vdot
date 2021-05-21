<%
    //ui.decorateWith("kenyaui", "panel", [ heading: "mUzima Queue Summary" ])
%>
<style>
.simple-table {
    border: solid 1px #DDEEEE;
    border-collapse: collapse;
    border-spacing: 0;
    font: normal 13px Arial, sans-serif;
}
.simple-table thead th {

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
        <legend>nimeConfirm Enrollment statistics</legend>
        <div>
            <table class="simple-table">
                <thead>
                <th width="50%">Enrollments</th>
                <th width="40%">Total</th>
                </thead>
                <tbody>
                <tr>
                    <td>Processed enrollments</td>
                    <td>${totalProcessed}</td>
                </tr>
                <tr>
                    <td>Pending enrollments</td>
                    <td>${pendingEnrollments}</td>
                </tr>
                </tbody>
            </table>
        </div>
   </fieldset>


</div>
