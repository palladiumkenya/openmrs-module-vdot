var CALENDAR = function () {
    var patientO = "{\n" +
        "\t\"dateofEnrollment\": \"2021-04-07\",\n" +
        "\t\"dates\": [\"2021-04-07 18:55:12\", \"2021-04-08 07:33:41\", \"2021-04-08 19:10:53\", \"2021-04-09 07:18:34\", \"2021-04-09 19:05:52\", \"2021-04-10 07:10:29\", \"2021-04-10 19:11:12\", \"2021-04-11 07:07:34\", \"2021-04-11 19:12:46\", \"2021-04-12 06:59:52\", \"2021-04-12 19:46:01\", \"2021-04-13 07:07:40\", \"2021-04-13 18:52:05\", \"2021-04-14 07:00:23\", \"2021-04-14 19:00:22\", \"2021-04-15 07:02:22\", \"2021-04-15 19:05:18\", \"2021-04-16 07:01:01\", \"2021-04-16 18:57:34\", \"2021-04-17 19:00:31\", \"2021-04-18 07:02:11\", \"2021-04-18 19:06:12\", \"2021-04-19 07:13:58\", \"2021-04-19 18:52:03\", \"2021-04-20 07:04:51\", \"2021-04-20 19:01:15\", \"2021-04-21 07:00:55\", \"2021-04-21 18:52:30\", \"2021-04-22 07:07:28\", \"2021-04-22 18:58:56\", \"2021-04-23 06:55:45\", \"2021-04-23 18:37:52\", \"2021-04-24 06:51:42\", \"2021-04-24 18:59:00\", \"2021-04-25 06:52:00\", \"2021-04-25 18:56:13\", \"2021-04-26 06:59:41\", \"2021-04-27 06:45:30\", \"2021-04-27 18:54:07\", \"2021-04-28 06:46:26\", \"2021-04-28 18:51:01\", \"2021-04-29 06:41:41\", \"2021-04-29 18:52:03\", \"2021-04-30 06:46:19\", \"2021-04-30 18:56:10\", \"2021-05-01 06:49:38\", \"2021-05-01 18:51:33\", \"2021-05-02 07:03:05\", \"2021-05-02 18:50:54\", \"2021-05-03 06:51:49\", \"2021-05-03 19:12:13\", \"2021-05-04 06:45:22\", \"2021-05-04 19:04:51\", \"2021-05-05 06:45:25\", \"2021-05-05 18:52:15\", \"2021-05-06 06:47:33\", \"2021-05-06 18:51:50\", \"2021-05-07 06:47:03\", \"2021-05-07 18:52:06\", \"2021-05-08 06:59:17\", \"2021-05-08 18:55:48\", \"2021-05-09 06:39:46\", \"2021-05-09 19:00:28\", \"2021-05-10 06:49:49\", \"2021-05-10 18:52:14\", \"2021-05-11 06:51:25\", \"2021-05-12 06:51:36\"]\n" +
        "}";
    var patientobject = JSON.parse(patientO);

    var monthSelect = document.getElementById('monthSelect');
    var btnSearch = document.getElementById('btnSearch');

    /*    jq('#btnSearch').bind('click', function() {
            alert('User clicked on "foo."');
        });*/
    btnSearch.addEventListener('click', funSwitchMonth);

    var wrap,
        label,
        months = [
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December",
        ];

    var thisMth = '';

    function init(newWrap) {
        var currentMth = ("0" + (new Date().getMonth() + 1)).slice(-2);
        thisMth = new Date().getFullYear() + '-' + currentMth;
        monthSelect.value = thisMth;
        monthSelect.setAttribute('max', thisMth);

        wrap = jq(newWrap || "#cal");
        label = wrap.find("#label");
        var curr = label.text().trim().split(" "),
            calendar,
            tempYear = parseInt(curr[1], 10);
        calendar = createCal(new Date().getFullYear(), new Date().getMonth());

        jq("#cal-frame", wrap)
            .find(".curr")
            .removeClass("curr")
            .addClass("temp")
            .end()
            .prepend(calendar.calendar())
            .find(".temp")
            .fadeOut("slow", function () {
                jq(this).remove();
            });

        label.text(calendar.label);
    }

    function switchMonth(next, month, year) {
        var curr = label.text().trim().split(" "),
            calendar,
            tempYear = parseInt(curr[1], 10);

        calendar = createCal(year, month);

        jq("#cal-frame", wrap)
            .find(".curr")
            .removeClass("curr")
            .addClass("temp")
            .end()
            .prepend(calendar.calendar())
            .find(".temp")
            .fadeOut("slow", function () {
                jq(this).remove();
            });

        label.text(calendar.label);
    }

    function createCal(year, month) {
        var currentYear = year;
        var currentMonth = ("0" + (month + 1)).slice(-2);
        var myDate = new Date(year + '-' + currentMonth);
        var day = 1,
            i,
            j,
            haveDays = true,
            startDay = new Date(year, month, day).getDay(),
            daysInMonths = [
                31,
                (year % 4 == 0 && year % 100 != 0) || year % 400 == 0 ? 29 : 28,
                31,
                30,
                31,
                30,
                31,
                31,
                30,
                31,
                30,
                31,
            ],
            calendar = [];

        if (createCal.cache[year]) {
            if (createCal.cache[year][month]) {
                return createCal.cache[year][month];
            }
        } else {
            createCal.cache[year] = {};
        }

        i = 0;
        while (haveDays) {
            calendar[i] = [];
            for (j = 0; j < 7; j++) {
                if (i === 0) {
                    if (j === startDay) {
                        calendar[i][j] = day++;
                        startDay++;
                    }
                } else if (day <= daysInMonths[month]) {
                    calendar[i][j] = day++;
                } else {
                    calendar[i][j] = "";
                    haveDays = false;
                }
                if (day > daysInMonths[month]) {
                    haveDays = false;
                }
            }
            i++;
        }
        if (calendar[6]) {
            for (i = 0; i < calendar[6].length; i++) {
                if (calendar[6][i] !== "") {
                    calendar[5][i] =
                        "<span>" +
                        calendar[5][i] +
                        "</span><span>" +
                        calendar[6][i] +
                        "</span>";
                }
            }
            calendar = calendar.slice(0, 5);
        }

        for (i = 0; i < calendar.length; i++) {
            var row = "<tr>";
            var icontaken =
                '<i style = "color:green; font-size: 18px;" class="fas fa-check-circle"></i>';
            var iconmissed =
                '<i style = "font-size: 18px;" class="far fa-times-circle"></i>';
            var frequency = 2;
            /*if (patientobject.patientRegimen != null && patientobject.patientRegimen !== ''){
              frequency = patientobject.patientRegimen.frequency;
            }*/


            var dates = patientobject.dates;
            min = currentYear + '-' + currentMonth + '-' + "01 00:00:00";
            max = currentYear + '-' + currentMonth + '-' + endOfMonth(myDate).getDate().toString() + " 23:59:59";
            // var thismonthsdates = dates.filter(date => date > min && date < max); not ready for ES6

            var thismonthsdates = dates.filter(function (date) {
                return date > min && date < max;
            });

            for (j = 0; j < 7; j++) {
                var morningchk = false;
                var eveningchk = false;
                if (calendar[i][j] == undefined || calendar[i][j] == "") {
                    row = row.concat("<td></td>");
                } else {
                    if (frequency === 2) {
                        for (var k = 0; k < thismonthsdates.length; k++) {
                            var date2 = new Date(Date.parse(thismonthsdates[k]));

                            var date = date2.getDate();

                            if (date === calendar[i][j]) {
                                var hour = date2.getHours();
                                if (hour >= 0 && hour < 12) {
                                    morningchk = true;
                                } else if (hour >= 12 && hour < 24) {
                                    eveningchk = true;
                                }
                            }
                        }
                        row = row.concat("<td>" + calendar[i][j] + "<br/>");
                        if (morningchk == true) {
                            row = row.concat(icontaken + "<br/>");
                        } else {
                            row = row.concat(iconmissed + "<br/>");
                        }
                        if (eveningchk == true) {
                            row = row.concat(icontaken);
                        } else {
                            row = row.concat(iconmissed);
                        }

                        row = row.concat("</td>");
                    } else {
                        var taken = false;
                        for (var k = 0; k < thismonthsdates.length; k++) {
                            var date2 = new Date(Date.parse(thismonthsdates[k]));

                            var date = date2.getDate();

                            if (date === calendar[i][j]) {
                                taken = true;
                            }
                        }
                        if (taken == true) {
                            row = row.concat(
                                "<td>" + calendar[i][j] + "<br/>" + icontaken + "</td>"
                            );
                        } else {
                            row = row.concat(
                                "<td>" + calendar[i][j] + "<br/>" + iconmissed + "</td>"
                            );
                        }
                    }
                }
            }

            row = row.concat("</tr>");
            calendar[i] = row;
        }

        calendar = jq("<table>" + calendar.join("") + "</table>").addClass("curr");

        jq("td:empty", calendar).addClass("nil");
        //if (patientobject.patientRegimen !== null) {
        //enrollmentdate = new Date(patientobject.patientRegimen.startDate);
        //enrollmentdate = new Date("2020-05-03");

        //}
        // if (month === new Date().getMonth()) {
        //   $("td", calendar)
        //     .filter(function () {
        //       return $(this).text() === new Date().getDate().toString();
        //     })
        //     .addClass("today");
        // }

        createCal.cache[year][month] = {
            calendar: function () {
                return calendar.clone();
            },
            label: months[month] + " " + year,
        };

        return createCal.cache[year][month];
    }

    createCal.cache = {};

    function endOfMonth(date) {

        return new Date(date.getFullYear(), date.getMonth() + 1, 0);

    }

    return {
        init: init,
        switchMonth: switchMonth,
        createCal: createCal,
    };

    function funSwitchMonth() {
        var month = new Date(monthSelect.value);
        var YYYY = month.getFullYear();
        var M = month.getMonth();

        switchMonth(null, M, YYYY);

    }
};
