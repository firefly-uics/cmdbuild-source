<%@ page language="java" %>
<%@ page session="true" %>
<%@ page import="org.cmdbuild.filters.AuthFilter" %>
<!-- Closing Rest session -->
	<script type="text/javascript">
	var token = readCookie("RestSessionToken");
	closeRestSession('http://localhost:8080/cmdbuild/services/rest/v2/sessions/' + token,
			function (response) {
				console.log("REST: Authentication success");
		});

	function closeRestSession(url, callbackFunction)
	{
		this.bindFunction = function (caller, object) {
			return function() {
				return caller.apply(object, [object]);
			};
		};

		this.stateChange = function (object) {
			if (this.request.readyState==4)
				this.callbackFunction(this.request.responseText);
		};

		this.getRequest = function() {
			if (window.ActiveXObject)
				return new ActiveXObject('Microsoft.XMLHTTP');
			else if (window.XMLHttpRequest)
				return new XMLHttpRequest();
			return false;
		};

		this.postBody = (arguments[2] || "");

		this.callbackFunction=callbackFunction;
		this.url=url;
		this.request = this.getRequest();
		
		if(this.request) {
			var req = this.request;
			req.onreadystatechange = this.bindFunction(this.stateChange, this);

			req.open("DELETE", url, true);
			req.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
			req.send(this.postBody);
		}
	}
	function readCookie(name) {
        var nameEQ = name + "=";
        var ca = document.cookie.split(';');
        for (var i = 0; i < ca.length; i++) {
            var c = ca[i];
            while (c.charAt(0) == ' ') c = c.substring(1, c.length);
            if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
        }
        return null;
    }
	</script>
<!-- End closing Rest session -->
	
<%
	if (session != null) {
		session.invalidate();
	}
	response.sendRedirect(AuthFilter.LOGIN_URL);
%>
