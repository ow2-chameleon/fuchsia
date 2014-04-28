
function fromServletToCombo(source, target) {

    d3.select(target).attr("disabled","true").append("option").text("Loading..");

    (function poll() {
        setTimeout(function () {
            $.ajax({
                url: source,
                type: "GET",
                success: function (data) {

                    var dataJSON = JSON.parse(data.toString());

                    d3.select("body").select(target).attr("disabled",null);

                    sel = d3.select("body").select(target).selectAll("option");

                    selectionData=sel.data(dataJSON,function(a,b,c) {
                        if(b==0) {
                            return a;
                        }else {
                            return a.factoryName;
                        }
                    });

                    selectionData.enter().append("option").text(function (d) {
                        return d.factoryName;
                    });

                    selectionData.exit().remove();

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

    $("#exporterForm").submit(function (event) {
        event.preventDefault();
        var $form = $(this), url = $form.attr("action");
        var posting = $.get(url, $form.serialize());
        posting.done(function (data) {
            displayMessage(data, "#messagePlaceholderExporter");

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