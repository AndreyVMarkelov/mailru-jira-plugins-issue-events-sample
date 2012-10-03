AJS.$(document).ready(function () {
    AJS.$('#create_link').remove();
})

function deleteEventKind(event, baseUrl, id) {
    event.preventDefault();
    JIRA.SmartAjax.makeRequest({
        url: baseUrl + "/rest/godeventsws/1.0/geservice/deleteeventkind",
        type: "POST",
        dataType: "json",
        data: {"id": id},
        error: function(xhr, ajaxOptions, thrownError) {
            alert(xhr.responseText);
        },
        success: function(result) {
            window.location.reload();
        }
    });
}

function addEventKind(event, baseUrl) {
    event.preventDefault();
    var md = new AJS.Dialog({
        width:400,
        height:250,
        id:"addek-dialog",
        closeOnOutsideClick: true
    });
    md.addHeader(dialogTitle);
    md.addPanel("load_panel", panelBody);
    md.addButton(addBtn, function(){
        if (!AJS.$("#namefld").val()) {
            alert('The name field must be set');
            return;
        }

        JIRA.SmartAjax.makeRequest({
            url: baseUrl + "/rest/godeventsws/1.0/geservice/addeventkind",
            type: "POST",
            dataType: "json",
            data: {"name": AJS.$("#namefld").val(), "comment": AJS.$("#commentfld").val(), "type": AJS.$("#evType :selected").val()},
            error: function(xhr, ajaxOptions, thrownError) {
                alert(xhr.responseText);
            },
            success: function(result) {
                window.location.reload();
            }
        });
    });
    md.addCancel(cancelBtn, function(){ md.hide(); });
    md.show();
}

function deleteCompDep(event, baseUrl, id) {
    event.preventDefault();
    JIRA.SmartAjax.makeRequest({
        url: baseUrl + "/rest/godeventsws/1.0/geservice/deletecompdep",
        type: "POST",
        dataType: "json",
        data: {"id": id},
        error: function(xhr, ajaxOptions, thrownError) {
            alert(xhr.responseText);
        },
        success: function(result) {
            window.location.reload();
        }
    });
}

function updateEvent(event, baseUrl) {
    event.preventDefault();
    AJS.$('body').css('cursor', 'wait');
    JIRA.SmartAjax.makeRequest({
        url: baseUrl + "/rest/godeventsws/1.0/geservice/updatecurreventdlg",
        type: "POST",
        dataType: "json",
        data: AJS.$("#agpform").serialize(),
        error: function(xhr, ajaxOptions, thrownError) {
            alert(xhr.responseText);
        },
        success: function(result) {
            window.opener.location.reload(true);
            self.close();
        }
    });
}

function updateProject(event, baseUrl) {
    event.preventDefault();
    AJS.$('body').css('cursor', 'wait');
    JIRA.SmartAjax.makeRequest({
        url: baseUrl + "/rest/godeventsws/1.0/geservice/updategprojectdlg",
        type: "POST",
        dataType: "json",
        data: AJS.$("#agpform").serialize(),
        error: function(xhr, ajaxOptions, thrownError) {
            alert(xhr.responseText);
        },
        success: function(result) {
            window.opener.location.reload(true);
            self.close();
        }
    });
}