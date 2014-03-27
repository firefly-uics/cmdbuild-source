(function() {

	/**
	 * Core, wrap the form submission and the Ajax requests
	 */
	CMDBuild.ServiceProxy.core = {
		submitForm: function(p) {
			if (p.form) {
				p.form.submit({
					url: p.url,
					method: p.mothod,
					scope: p.scope || this,
					success: p.success || Ext.emptyFn,
					failure: p.failure || Ext.emptyFn,
					callback: p.callback || Ext.emptyFn
				});
			} else {
				throw CMDBuild.core.error.serviceProxy.NO_FORM;
			}
		},

		doRequest: function(p) {
			var successWithAdapter = Ext.Function.createInterceptor(p.success || Ext.emptyFn, function(response) {
				if (p.adapter) {
					var json =  Ext.JSON.decode(response.responseText);
					var adaptedJson = p.adapter(json);
					_debug("Adapted JSON result", json, adaptedJson);
					response.responseText = Ext.JSON.encode(adaptedJson);
				}
			});

			CMDBuild.Ajax.request({
				url: p.url,
				method: p.method,
				params: p.params || {},
				scope: p.scope || this,
				success: successWithAdapter,
				failure: p.failure || Ext.emptyFn,
				callback: p.callback || Ext.emptyFn,
				important: p.important
			});
		}
	};

	/* ===========================================
	 * Orphans
	 =========================================== */

	CMDBuild.ServiceProxy.doLogin = function(p) {
		CMDBuild.Ajax.request( {
			important: true,
			url: CMDBuild.ServiceProxy.url.login,
			method: 'POST',
			params: p.params,
			success: p.success || Ext.emptyFn,
			failure: p.failure || Ext.emptyFn,
			callback: p.callback || Ext.emptyFn,
			scope: p.scope || this
		});
	};

	// TODO duplicate in card section, remove this
	CMDBuild.ServiceProxy.getCardList = function(p) {
		CMDBuild.Ajax.request( {
			url: CMDBuild.ServiceProxy.url.cardList,
			method: 'GET',
			params: p.params,
			success: p.success,
			failure: p.failure,
			callback: p.callback
		});
	};

	CMDBuild.ServiceProxy.getCardBasicInfoList = function(className, success, cb, scope) {
		CMDBuild.ServiceProxy.core.doRequest({
			method: 'GET',
			url: CMDBuild.ServiceProxy.url.basicCardList,
			params: {
				ClassName: className,
				NoFilter: true
			},
			success: success,
			callback: cb,
			scope: scope
		});
	};

	/**
	 * @param {object} p
	 * @param {object} p.params
	 * @param {string} p.params.className
	 */
	CMDBuild.ServiceProxy.getFKTargetingClass = function(p) {
		p.url = CMDBuild.ServiceProxy.url.fkTargetClass;
		p.method = 'GET';
		CMDBuild.Ajax.request(p);
	};

	/* ===========================================
	 * Attributes
	 =========================================== */

	CMDBuild.ServiceProxy.attributes = {

		/**
		 *
		 * @param {object} p
		 * @param {string} p.params.className
		 */
		update: function(p) {
			p.method = 'POST';
			p.url = CMDBuild.ServiceProxy.url.attribute.update;
			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		/**
		 *
		 * @param {object} p
		 * @param {object} p.params
		 * @param {boolean} p.params.active
		 * @param {string} p.params.className
		 */
		read: function(p) {
			p.method = 'GET';
			p.url = CMDBuild.ServiceProxy.url.attribute.read;
			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		/**
		 *
		 * @param {object} p
		 * @param {object} p.params
		 * @param {string} p.params.name
		 * @param {string} p.params.className
		 */
		remove: function(p) {
			p.method = 'POST';
			p.url = CMDBuild.ServiceProxy.url.attribute.remove;
			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		/**
		 * @param {object} p
		 * @param {object} p.params
		 * @param {string} p.params.className
		 * @param {array[]} p.params.attributes [{name: "", index: ""}]
		 */
		reorder: function(p) {
			p.method = 'POST';
			p.url = CMDBuild.ServiceProxy.url.attribute.reorder;
			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		/**
		 * @param {object} p
		 * @param {object} p.params
		 * @param {string} p.params.className
		 * @param {object} p.params.attributes {attributename: position, ...}
		 */
		updateSortConfiguration: function(p) {
			p.method = 'POST';
			p.url = CMDBuild.ServiceProxy.url.attribute.updateSortConfiguration;
			CMDBuild.ServiceProxy.core.doRequest(p);
		}
	};

	// Alias
	_CMProxy = CMDBuild.ServiceProxy;

})();