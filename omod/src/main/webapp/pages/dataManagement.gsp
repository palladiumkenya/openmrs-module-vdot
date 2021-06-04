<%
    ui.decorateWith("kenyaemr", "standardPage", [ layout: "sidebar" ])

    def menuItems = [
            [label: "Back", iconProvider: "kenyaui", icon: "buttons/back.png", label: "Back to NimeCONFIRM home", href: ui.pageLink("vdot", "vdotHome")]
    ]

%>
<style>
.mainBox {
    float: left;
}
.msg {
    width: 90%

}
.boxStyle {
    color: black;
    font-size: 1.2em;
    line-height: 1.5;
    border: 2px darkslategray solid;
    border-radius: 10px;
    padding: 10px 5px 5px 10px;
    margin: 5px 2px;
    width: 48%;
}

</style>

<div class="ke-page-sidebar">

    <div class="ke-panel-frame">
        ${ui.includeFragment("kenyaui", "widget/panelMenu", [heading: "Navigation", items: menuItems])}
    </div>
</div>

<div class="ke-page-content">
    <div class="mainBox boxStyle" style="width: 90%">
        ${ ui.includeFragment("vdot", "vdotEnrollmentStats") }

        <br/>
        <% if (pendingEnrollments > 0) { %>
        <span id="msgBox" style="color: green"></span><br/>
        <button id="postMessagetoNimeConfirm">Push to server</button>

        <% } else { %>
        <span style="font-size: medium">No new enrollment to push to nimeConfirm</span>


            <% } %>

    </div>
    <div class="mainBox boxStyle" style="margin-top: 50px; width: 90%">

        <br/>
        <br/>
        <span id="msgBox2" style="color: green"></span><br/>
        <button  id="pullMessagesFromVdot">Pull data from server</button>

        <br/>
    </div>
</div>

<script type="text/javascript">
    jq = jQuery;
    jQuery(function() {

        jq('#pullMessagesFromVdot').click(function() {
            jq.getJSON('${ ui.actionLink("vdot", "vdotPatientData", "getNimeConfirmVideoObs") }')
                .success(function(data) {
                    jq('#msgBox2').html("Vdot data pulled successfully");
                })
                .error(function(xhr, status, err) {
                    jq('#msgBox2').html("There was an error pulling Vdot messages");
                })
        });

    });

    jq(function() {
        jq('#postMessagetoNimeConfirm').click(function() {
            jq.getJSON('${ ui.actionLink("vdot", "vdotPatientObservations", "postEnrollmentMessage") }',
                {})
                .success(function(data) {
                    jq('#msgBox').html("Successfully posted to nimeConfirm");
                })
                .error(function(xhr, status, err) {
                    jq('#msgBox').html("Could not post to nimeConfirm. Kindly contact an admin user for help");
                })
        });
    });
</script>