
function fromSerletJsonToCombo(source, target) {

    (function poll() {
        setTimeout(function () {
            $.ajax({
                url: source,
                type: "GET",
                success: function (data) {

                    data = JSON.parse(data.toString());

                    console.log(JSON.stringify(data, null, 4));

                    sel = d3.select("body").select(target).selectAll("option");

                    sel.data(data).enter().append("option").text(function (d) {
                        return d.factoryName;
                    });

                    sel.data(data).exit().remove();

                },
                error: function (a, b, c) {
                    //Do something with this
                },
                contentType: "text/plain",
                dataType: "text",
                complete: poll,
                timeout: 2000
            })
        }, 5000);
    })();

}

function setupFormSubmission() {

    $("#importationLinkerForm").submit(function (event) {
        event.preventDefault();
        var $form = $(this), url = $form.attr("action");
        var posting = $.get(url, $form.serialize());
        posting.done(function (data) {
            displayMessage(data, "#messagePlaceholderLinker");
        });
    });

    $("#importerForm").submit(function (event) {
        event.preventDefault();
        var $form = $(this), url = $form.attr("action");
        var posting = $.get(url, $form.serialize());
        posting.done(function (data) {
            displayMessage(data, "#messagePlaceholderImporter");

        });

    });

}

function displayMessage(data, div) {
    jsData = JSON.parse(data.toString());
    $(div).removeClass("error");
    $(div).removeClass("success");

    if (data.toString() == "") {
        $(div).text("");
    } else {
        $(div).text(jsData.message);
        $(div).addClass(jsData.type);
    }

}