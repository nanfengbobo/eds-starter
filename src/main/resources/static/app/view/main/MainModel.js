Ext.define('Start.view.main.MainModel', {
	extend: 'Ext.app.ViewModel',

	stores: {
		navigationStore: {
			type: 'tree',
			autoLoad: false,
			nodeParam: 'id',
			root: {
				expanded: true
			},
			proxy: {
				type: 'direct',
				directFn: 'navigationService.getNavigation'
			}
		},

		logLevels: {
			type: 'array',
			fields: [ 'level' ],
			data: [ [ 'ERROR' ], [ 'WARN' ], [ 'INFO' ], [ 'DEBUG' ] ]
		}
	}

});