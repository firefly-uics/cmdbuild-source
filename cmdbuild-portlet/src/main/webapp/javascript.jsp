<%@page import="org.cmdbuild.portlet.utils.FieldUtils"%>
<%@page import="org.cmdbuild.portlet.operation.WSOperation"%>
<%@page import="org.cmdbuild.portlet.ws.*"%>
<%
    SOAPClient client = new SOAPClient(url, user, password);
    WSOperation operation = new WSOperation(client);
    User cmdbuildUser = operation.getUser(userEmail);
    request.getSession().setAttribute("cmdbuildUser", cmdbuildUser);
%>
<script type="text/javascript">

    jQuery(document).ready(function(){
        // --- Include custom CSS to header ---
    <%
        FieldUtils fieldUtils = new FieldUtils();
        String customCSS = fieldUtils.getCustomCSS(contextPath);
    %>
        jQuery("body").parent().append('<%= customCSS%>');
    });

    var CMDBuildCallbacks = {
        flexigridProcessCallback: function() {}
    }

    var CMDBuild = {
        Runtime: {
            ConfigurationUser: "<%= user %>",
            ConfigurationPassword: "<%= password %>",
            <%--ConfigurationGroup: "<%= group %>",--%>
            ConfigurationURL: "<%= url %>",
            ConfigurationSupportEmail: "<%= supportEmail %>",
            ConfigurationUserTable: "<%= cmdbusertable %>",
            ConfigurationShowStartProcesse: "<%= displayStartProcess %>",
            ConfigurationShowOpenedProcesses: "<%= displayOpenedProcesses %>",
            CMDBuildUserId: "<%= cmdbuildUser.getId()%>",
            CMDBuildUserName: "<%= cmdbuildUser.getName()%>",
            CMDBuildUserEmail: "<%= cmdbuildUser.getEmail()%>",
            CMDBuildUserGroup: "<%= cmdbuildUser.getGroup()%>",
            firstname: "<%= userFirstName %>",
            lastname: "<%= userLastname %>",
            username: "<%= connectedUser %>",
            email: "<%= userEmail %>",
            BaseURL: "<%= contextPath%>",
            displayStartProcess: <%= displayStartProcess %>,
            AttachmentCounter: 0
        }
    };

</script>
<!-- Core JS-->
<script src="<%= contextPath %>/js/core/CMDBuildAttachment.js" type="text/javascript" ></script>
<script src="<%= contextPath %>/js/core/CMDBuildCore.js" type="text/javascript" ></script>
<script src="<%= contextPath %>/js/core/CMDBuildEmail.js" type="text/javascript" ></script>
<script src="<%= contextPath %>/js/core/CMDBuildReport.js" type="text/javascript" ></script>
<script src="<%= contextPath %>/js/core/CMDBuildTemplateResolver.js" type="text/javascript" ></script>
<!-- Card JS -->
<script src="<%= contextPath %>/js/card/CMDBuildCreateCard.js" type="text/javascript" ></script>
<script src="<%= contextPath %>/js/card/CMDBuildModifyCard.js" type="text/javascript" ></script>
<!-- Workflow JS -->
<script src="<%= contextPath %>/js/workflow/CMDBuildAdvanceWorkflow.js" type="text/javascript" ></script>
<script src="<%= contextPath %>/js/workflow/CMDBuildLinkCard.js" type="text/javascript" ></script>
<script src="<%= contextPath %>/js/workflow/CMDBuildNotes.js" type="text/javascript" ></script>
<script src="<%= contextPath %>/js/workflow/CMDBuildProcess.js" type="text/javascript" ></script>
<script src="<%= contextPath %>/js/workflow/CMDBuildStartWorkflow.js" type="text/javascript" ></script>
<script src="<%= contextPath %>/js/workflow/CMDBuildWorkflowWidget.js" type="text/javascript" ></script>
<!--External JS -->
<script src="<%= contextPath %>/js/external/date.js" type="text/javascript"></script>
<script src="<%= contextPath %>/js/external/jquery.date_input.js" type="text/javascript"></script>
<script src="<%= contextPath %>/js/external/flexigrid.js" type="text/javascript"></script>
<script src="<%= contextPath %>/js/external/jquery.blockUI.js" type="text/javascript"></script>
<script src="<%= contextPath %>/js/external/jquery.wysiwyg.js" type="text/javascript"></script>
<script src="<%= contextPath %>/js/external/jquery.validate.js" type="text/javascript"></script>
<script src="<%= contextPath %>/js/external/ListAdapter.js" type="text/javascript" ></script>
<%
    
    String customJS = fieldUtils.getCustomJS(contextPath);
%>
<%= customJS%>