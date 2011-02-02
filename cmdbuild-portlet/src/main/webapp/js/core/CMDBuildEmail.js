/*
 * Author: Giuseppe Gortan
 */

var CMDBuildEmailProcessId;
var CMDBuildEmailClassname;

//Show email response
function CMDBuildShowEmailResponse(result){
    jQuery.unblockUI();
    jQuery("#CMDBuildEmailResponseText").append(result);
    jQuery('.wysiwyg').remove();
    jQuery("#CMDBuildEmailtext_hidden").remove();
    jQuery('#CMDBuildEmail').fadeOut();
    jQuery("#CMDBuildCmdbuild_tabs").show();
    jQuery("#CMDBuildProcess").show();
    jQuery("#CMDBuildTablecontainer").show();
    jQuery("#CMDBuildEmailresponse").draggable();
    jQuery("#CMDBuildEmailresponse").show();
}

function CMDBuildCloseEmailpopup() {
    jQuery('#CMDBuildEmailresponse').hide();
    jQuery("#CMDBuildEmailResponseText > *").remove();
}

//Show email window
function CMDBuildShowEmail(classname, processid) {
    CMDBuildEmailProcessId = processid;
    CMDBuildEmailClassname = classname;
    jQuery("#CMDBuildCustom").hide();
    jQuery("#CMDBuildTablecontainer").hide();
    jQuery("#CMDBuildProcess").hide();
    jQuery("#CMDBuildCmdbuild_tabs").hide();
    jQuery("#CMDBuildEmailresponse > *").remove();
    jQuery("input[name='to']").val("");
    jQuery("input[name='cc']").val("");
    jQuery("input[name='subject']").val("");
    jQuery("#CMDBuildEmailcontent > *").remove();
    jQuery("#CMDBuildEmailcontent").val("");
    jQuery('.wysiwyg').remove();
    jQuery('#CMDBuildEmailcontent').wysiwyg();
    jQuery('#CMDBuildEmail').fadeIn("fast");
    return false;
}

//Ajax call to send email
function CMDBuildSendEmail(){
    jQuery.blockUI({
        message: '<h1>Attendere prego ...</h1>',
        css: {
            border: 'none',
            padding: '15px',
            backgroundColor: '#000',
            '-webkit-border-radius': '10px',
            '-moz-border-radius': '10px',
            opacity: .5,
            color: '#fff'
        }
    });

    var body = jQuery("#CMDBuildEmailcontentIFrame").document();
    var content = jQuery(body).find('body').html();
    var hidden_field = '<input id="CMDBuildEmailtext_hidden" type=\"hidden\" name=\"emailtext\" />';
    jQuery("#CMDBuildEmailform").append(hidden_field);
    jQuery("#CMDBuildEmailtext_hidden").attr('value', content);
    var to = jQuery("input[name='to']").val();
    var cc = jQuery("input[name='cc']").val();
    var subject = jQuery("input[name='subject']").val();
    var emailtext = jQuery("input[name='emailtext']").val();
    var email = CMDBuild.Runtime['email'];
    var baseURL = CMDBuild.Runtime['BaseURL'];
    jQuery.ajax({
        type: 'POST',
        url: baseURL+'/SendMailServlet',
        //data: 'from='+email+'&to='+to+'&cc='+cc+'&subject='+subject+'&emailtext='+emailtext+'&type=workflow&processid='+CMDBuildEmailProcessId+"&classname="+CMDBuildEmailClassname,
        data: {
          from: email,
          to: to,
          cc: cc,
          subject: subject,
          emailtext: emailtext,
          type: 'workflow',
          processid: CMDBuildEmailProcessId,
          classname: CMDBuildEmailClassname
        },
        complete: function(response) {
            var result = response.responseText;
            CMDBuildShowEmailResponse(result);
        }
    });
}

//Close email section
function CMDBuildCloseEmail() {
    jQuery('#CMDBuildEmail').fadeOut("fast");
    jQuery("#CMDBuildCmdbuild_tabs").show();
    jQuery("#CMDBuildTablecontainer").show();
}

//Show email info popup
function CMDBuildShowEmailFieldInfo(){
    jQuery("#CMDBuildEmailfieldinfo").fadeIn("fast");
    jQuery("#CMDBuildEmailfieldinfo").dialog({
        buttons: {
            "Ok": function() {
                jQuery(this).dialog("close");
            }
        }
    });
}
