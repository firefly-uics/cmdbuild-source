(function() {

	Ext.define('CMDBuild.core.proxy.CMProxyRelations', {
		alternateClassName: 'CMDBuild.ServiceProxy.relations', // Legacy class name

		statics: {
			/**
			 * @param (Object) p
			 * @param (Object) p.params
			 * @param (Int) p.params.cardId
			 * @param (String) p.params.className
			 * @param (Int) p.params.domainId
			 * @param (String) p.params.src '_1' | '_2'
			 * @param (Int) p.params.domainlimit
			 */
			getList: function(p) {
				adaptGetListRequestParameter(p);
				p.method = 'GET';
				p.url = CMDBuild.core.proxy.Index.relations.read;

				CMDBuild.ServiceProxy.core.doRequest(p);
			},

			modify: function(p) {
				p.method = 'POST';
				p.url = CMDBuild.core.proxy.Index.relations.update;

				CMDBuild.ServiceProxy.core.doRequest(p);
			},

			add: function(p) {
				p.method = 'POST';
				p.url = CMDBuild.core.proxy.Index.relations.create;

				CMDBuild.ServiceProxy.core.doRequest(p);
			},

			remove: function(p) {
				p.method = 'POST';
				p.url = CMDBuild.core.proxy.Index.relations.remove;

				CMDBuild.ServiceProxy.core.doRequest(p);
			},

			removeDetail: function(p) {
				p.method = 'POST';
				p.url = CMDBuild.core.proxy.Index.relations.removeDetail;

				CMDBuild.ServiceProxy.core.doRequest(p);
			},

			/**
			 * @param (Object) parameters
			 */
			getAlreadyRelatedCards: function(parameters) {
				CMDBuild.core.interfaces.Ajax.request({
					method: 'POST',
					url: CMDBuild.core.proxy.Index.relations.getAlreadyRelatedCards,
					params: parameters.params,
					scope: parameters.scope,
					success: parameters.success,
					callback: parameters.callback
				});
			}
		}
	});

	/**
	 * Currently the getList receive an object like this
	 * {
	 *		Id: me.card.get('Id'),
	 *		IdClass: me.entryType.getId(),
	 *		domainId: me.view.detail.get('id'),
	 *		src: me.view.detail.getDetailSide()
	 * }
	* for server refactoring we want
	*
	* cardId = Id
	* className -> take it from cache
	* domainId = domainId
	* src
	*/
	function adaptGetListRequestParameter(p) {
		var parameterName = CMDBuild.ServiceProxy.parameter;
		if (p.params && p.params.IdClass) {
			_debug('DEPRECATED: CMDBuild.ServiceProxy.relations.getList will change the request params as soon as possible', p.params);

			p.params[parameterName.CARD_ID] = p.params.Id;
			delete p.params.Id;
			p.params[parameterName.CLASS_NAME] = _CMCache.getEntryTypeNameById(p.params.IdClass);
			delete p.params.IdClass;
		}
	}

})();