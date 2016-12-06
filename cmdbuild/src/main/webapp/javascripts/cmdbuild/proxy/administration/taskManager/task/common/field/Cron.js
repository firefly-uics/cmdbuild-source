(function () {

	Ext.define('CMDBuild.proxy.administration.taskManager.task.common.field.Cron', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.ArrayStore}
		 */
		getStoreExpression: function () {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.VALUE, CMDBuild.core.constants.Proxy.DESCRIPTION],
				data: [
					['0 * * * ?', CMDBuild.Translation.everyHour],
					['0 0 * * ?', CMDBuild.Translation.everyDay],
					['0 0 1 * ?', CMDBuild.Translation.everyMonth],
					['0 0 1 1 ?', CMDBuild.Translation.everyYear]
				]
			});
		}
	});

})();
