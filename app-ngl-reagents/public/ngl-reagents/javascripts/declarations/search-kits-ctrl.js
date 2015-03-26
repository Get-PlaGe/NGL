"use strict"

angular.module('home').controller('SearchKitsCtrl', ['$scope', 'datatable','lists','$filter','mainService','tabService','kitsSearchService','$routeParams', function($scope, datatable, lists,$filter,mainService,tabService,kitsSearchService,$routeParams) {
	$scope.datatableConfig = {
		search:{
			url:jsRoutes.controllers.reagents.api.Kits.list()
		},
		order:{
			by:'name',
			reverse:true
		},
		edit:{
			active:false
		},
		save:{
			active:false
		},
		show : {
			active:true,
			showButton : true,
			add:function(line){
				tabService.addTabs({label:line.barCode,href:jsRoutes.controllers.reagents.tpl.Kits.get(line.code).url,remove:true});
			}
		}
	};

	$scope.search = function(){		
		$scope.searchService.search();
	};
	
	$scope.reset = function(){
		$scope.searchService.resetForm();		
	};
	
	//init
	$scope.datatable = datatable($scope.datatableConfig);		
	if(angular.isUndefined($scope.getHomePage())){
		mainService.setHomePage('new');
		tabService.addTabs({label:Messages('kitCatalogs.tabs.search'),href:jsRoutes.controllers.reagents.tpl.Kits.home("search").url,remove:false});
		tabService.activeTab(0);
	}
	if(angular.isUndefined($scope.getForm())){
		$scope.form = {};
		mainService.setForm($scope.form);
	}
	
	$scope.searchService = kitsSearchService;
	$scope.searchService.init($routeParams, $scope.datatableConfig)
}]);