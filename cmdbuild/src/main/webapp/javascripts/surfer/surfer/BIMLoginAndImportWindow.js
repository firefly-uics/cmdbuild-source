(function() {

	// Template constants
	var	CLOSE_ICON_ID = "dialog-bimserver-import-close",
		WINDOW_WRAPPER_ID = "dialog-bimserver-import",
		TAB_1_ID = "bimserver-import-step1",
		FORM_1_ID = "dialog-tab-bimserver1",
		TAB_2_ID = "bimserver-import-step2",
		FORM_2_ID = "dialog-tab-bimserver2",
		PROJECT_LIST_ID = "bimserver-projects",
		URL_INPUT_ID = "bimserver-login-url",
		PASSWOR_INPUT_ID = "bimserver-login-password",
		USERNAME_INPUT_ID = "bimserver-login-username",
		WINDOW_BACKGROUND_ID = "dialog-background",
		REMEMBER_ME_INPUT_ID = "bimserver-login-rememberme",
		TEMPLATE =	"<div id='" + WINDOW_WRAPPER_ID + "' class='dialog-frame'>" +
						"<div class='dialog-header'>" +
							"<div id='dialog-logo-bimserver'></div>" +
							"<a id ='" + CLOSE_ICON_ID + "' class='dialog-close'>x</a>" +
						"</div>" +
						"<div class='dialog-steps'>" +
							"<a id='" + TAB_1_ID + "' class='dialog-step dialog-step-active'>Login</a>" +
							"<a id='" + TAB_2_ID + "' class='dialog-step'>Choose a project</a>" +
						"</div>" +
						"<div class='dialog-main'>" +
							"<form id='dialog-tab-bimserver1' class='form-horizontal'>" +
								"<div class='dialog-form-item'>" +
									"<label class='control-label' for='bimserver-login-url'>BIMserver</label>" +
									"<input type='text' id='" + URL_INPUT_ID + "' value='http://79.59.135.234:9088/bimserver/'>" +
//									"<input type='text' id='" + URL_INPUT_ID + "' value='http://localhost:10080/bimserver-1.2/'>" +
								"</div>" +
								"<div class='dialog-form-item'>" +
									"<label class='control-label' for='" + USERNAME_INPUT_ID + "'>Email</label>" +
									"<input type='text' id='" + USERNAME_INPUT_ID + "' value='admin@tecnoteca.com' placeholder='Email'>" +
								"</div>" +
								"<div class='dialog-form-item'>" +
									"<label class='control-label' for='" + PASSWOR_INPUT_ID + "'>Password</label>" +
									"<input type='password' id='" + PASSWOR_INPUT_ID + "' value='admin' placeholder='Password'>" +
								"</div>" +
								"<div class='dialog-form-item'>" +
									"<label class='control-label' for='" + REMEMBER_ME_INPUT_ID + "'> Remember me </label>" +
									"<input type='checkbox' id='" + REMEMBER_ME_INPUT_ID + "' checked='checked'>" +
								"</div>" +
								"<div class='dialog-buttons'>" +
									"<button id='bimserver-login-submit' type=submit class='btn'>Sign in </button>" +
								"</div>" +
							"</form>" +
							"<form id='dialog-tab-bimserver2' class='dialog-tab' onsubmit='return false;' style='display:none;'>" +
								"<ul id='" + PROJECT_LIST_ID +"'></ul>" +
								"<div class='dialog-buttons'>" +
									"<button class='btn' id='bimserver-projects-submit' disabled='disabled'>" +
										"Open" +
									"</button>" +
									"<button class='btn' id='bimserver-projects-refresh' disabled='disabled'>" +
										"Refresh" +
									"</button>" +
									"<button class='btn' id='bimserver-projects-logout' disabled='disabled'>" +
										"Logout" +
									"</button>" +
								"</div>" +
							"</form>" +
						"</div>" +
					"</div>";

	BIMLoginAndImportWindow = function() {
		this.loginProxy = new BIMLoginProxy();

		render();
		bindEvents(this);

		var me = this;
		this.loginProxy.tryAutoLogin(function() {
			showProjectSelectionForm();
			refreshProjectList(me);
		});

	};

	/**
	 * Inject the DOM nodes in
	 * the page body
	 */
	function render() {
		$("body").append('<div id="' + WINDOW_BACKGROUND_ID + '"></div>');
		$("body").append(TEMPLATE);
	}

	/**
	 * Map UI events with
	 * logic functions
	 */
	function bindEvents(me) {
		$('#' + CLOSE_ICON_ID).click(destroy);

		$('#bimserver-projects-refresh').click( //
			function onRefreshButtonClick() {
				refreshProjectList(me);
				return false;
			}
		);

		($('#bimserver-projects-logout')).click( //
			function onLogoutButtonClick() {
				doLogout(me);
				return false;
			}
		);

		($('#bimserver-projects')).delegate('li', 'click', bimserverImportDialogSelect);

		// submit login form
		$('#' + FORM_1_ID).submit( //
			function() {
				doLogin(me);
				return false;
			} //
		);

		// submit of form to select a project
		($('#' + FORM_2_ID)).submit( //
			function() {
				var $selectedProject;
				$selectedProject = $('.bimserver-project-selected');

				if ($selectedProject.length === 0) {
					$("body").toastmessage('showToast', {
						text: 'Please, select a project',
						sticky: false,
						type: 'warning'
					}); 
				} else {
					destroy();
					var roid = $selectedProject.attr('bimserverroid');
					window._BIM_SCENE_MANAGER.loadProjectWithRoid(roid);
				}
			} //
		);

		// show the first tab
		($('#' + TAB_1_ID)).click(showLoginForm);

		// show the second tab
		($('#' + TAB_2_ID)).click(showProjectSelectionForm);
	}

	function showLoginForm() {
		// othis.bimserverImportDialogClearMessages();
		$("#" + TAB_1_ID).addClass('dialog-step-active');
		$("#" + TAB_2_ID).removeClass('dialog-step-active');
		$("#" + FORM_1_ID).show();
		return $("#" + FORM_2_ID).hide();
	}

	function showProjectSelectionForm() {
		// othis.bimserverImportDialogClearMessages();
		$("#" + TAB_2_ID).addClass('dialog-step-active');
		$("#" + TAB_1_ID).removeClass('dialog-step-active');
		$("#" + FORM_2_ID).show();
		return $("#" + FORM_1_ID).hide();
	}

	/**
	 * Remove the DOM nodes from
	 * the body
	 */
	function destroy() {
		$('#' + WINDOW_WRAPPER_ID).remove();
		$("#" + WINDOW_BACKGROUND_ID).fadeOut(800).remove();
	}

	/**
	 * Use the BIMServer API
	 * to try to log in the user
	 */
	function doLogin(me) {

		// othis.bimserverImportDialogClearMessages();

		// clean the project list
		$("#" + PROJECT_LIST_ID).html("");

		// retrieve the values and show
		// error message if something
		// is not well filled
		var formValues = getLoginFormValuesAndValidateThem(me);
		if (!me.loginFormValid) {
			return false;
		}

		_BIM_LOGGER.log(formValues);

		me.loginProxy.login({
			url: formValues.url,
			username: formValues.user,
			password: formValues.password,
			rememberMe: formValues.rememberMe,

			success: function() {
				showProjectSelectionForm();
				refreshProjectList(me);
			},

			failure: function() {
				$("body").toastmessage('showToast', {
					text: 'Sorry, but I\'m not able to log you in',
					sticky: false,
					type: 'error'
				}); 
			}
		});
	}

	function doLogout(me) {
		me.loginProxy.logout(showLoginForm);
	}

	/**
	 * Return a map like this
	 * 	{
	 * 		url: string,
	 * 		password: string,
	 * 		user: string,
 	 * 		rememberMe: boolean
	 * 	}
	 */
	function getLoginFormValuesAndValidateThem(me) {
		me.loginFormValid = true;
		var values = {};

		values.url = getInputValueAndValidate(URL_INPUT_ID, me);
		values.password = getInputValueAndValidate(PASSWOR_INPUT_ID, me);
		values.user = getInputValueAndValidate(USERNAME_INPUT_ID, me);
		values.rememberMe = $("#" + REMEMBER_ME_INPUT_ID).is(":checked");

		return values;
	}

	function getInputValueAndValidate(inputId, me) {
		var element = $("#" + inputId);
		var value = element.val() || "";
		if (value < 1) {
			element.addClass('error');
			me.loginFormValid = false;
		} else {
			element.removeClass('error');
		}

		return value;
	}

	function refreshProjectList(me) {
		var $projectList;

		$('#dialog-tab-bimserver2 button').attr('disabled', 'disabled');
		$('#bimserver-projects-submit').attr('disabled', 'disabled');
		$projectList = $('#bimserver-projects');
		$projectList.html("");

		function onGetAllProcessSuccess(data) {
			($('#bimserver-import-message-info')).html("Fetched all projects");

			if (!data) {
				return;
			}

			data.forEach(function(project) {
				if (project.lastRevisionId != -1) {
					$projectList.append("<li class='bimserver-project' bimserveroid='" + project.oid + "' bimserverroid='" + project.lastRevisionId + "'>" + project.name + "</li>");
				}
			});

			return ($('#dialog-tab-bimserver2 button')).removeAttr('disabled');
		}

		function onGetAllProcessFailure() {
			$("body").toastmessage('showToast', {
				text: "Couldn't fetch projects",
				sticky: false,
				type: 'error'
			});
		}

		var params =  { //
			onlyTopLevel : true, //
			onlyActive : true //
		};

		// TODO extract a proxy to not call
		// the API directly
		window._BIM_SERVER_API.call( //
			"Bimsie1ServiceInterface", //
			"getAllProjects", //
			params, //
			onGetAllProcessSuccess, //
			onGetAllProcessFailure //
		);
	}

	function bimserverImportDialogSelect(event) {
		$('.bimserver-project-selected').removeClass('bimserver-project-selected');
		$(event.target).addClass('bimserver-project-selected');
		return ($('#bimserver-projects-submit')).removeAttr('disabled');
	}

})();
