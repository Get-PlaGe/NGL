"use strict"
angular.module('home').controller('SearchCtrl', ['$scope', 'datatable','lists','$filter','mainService','tabService','containerSupportsSearchService','$routeParams', 
                                                 function($scope, datatable, lists,$filter,mainService,tabService,containerSupportsSearchService,$routeParams) {
	$scope.datatableConfig = {
		search:{
			url:jsRoutes.controllers.containers.api.ContainerSupports.list()
		},
		pagination:{
			mode:'local'
		},
		order:{
			by:'traceInformation.creationDate',
			reverse:true,
			mode:'local'
		},
		edit:{
			active:Permissions.Check("writing")?true:false,
			columnMode:true
		},
		save:{
			active:Permissions.Check("writing")?true:false,
			url:jsRoutes.controllers.containers.api.ContainerSupports.updateStateBatch().url,
			batch:true,
			mode:'remote',
			method:'put',
			callback: function(reason, error){
				
				console.log("callback reason=" + reason);
				console.log("callback error=" + error);
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
		tabService.addTabs({label:Messages('containerSupports.tabs.search'),href:jsRoutes.controllers.containers.tpl.ContainerSupports.home("new").url,remove:false});
		tabService.activeTab(0);
	}
	if(angular.isUndefined($scope.getForm())){
		$scope.form = {};
		mainService.setForm($scope.form);
	}
	
	$scope.searchService = containerSupportsSearchService;
	$scope.searchService.init($routeParams, $scope.datatableConfig)

}]);


"use strict"
angular.module('home').controller('SearchStateCtrl', ['$scope','$location','$routeParams', 'datatable','lists','$filter','$http','mainService','tabService','containerSupportsSearchService', 
                                                      function($scope,$location,$routeParams, datatable, lists,$filter,$http,mainService,tabService,containerSupportsSearchService) {
	$scope.datatableConfig = {
			search:{
				url:jsRoutes.controllers.containers.api.ContainerSupports.list()
				
			},
			order:{
				by:'code'
			},
			edit:{
				active:Permissions.Check("writing")?true:false,
				columnMode:true
			},
			
			save:{
				active:Permissions.Check("writing")?true:false,
				url:function(line){return jsRoutes.controllers.containers.api.ContainerSupports.updateStateCode(line.code).url;},
				mode:'remote',
				method:'put',
				value:function(containerSupport){
					var updateForm = {};
					updateForm.stateCode = containerSupport.state.code;
					return updateForm;
				}
			}
	};

	$scope.reset = function(){
		$scope.searchService.resetForm();
	};
	
	$scope.search = function(){	
		$scope.searchService.search();
	};
	
	//init
	if(angular.isUndefined($scope.getHomePage())){
		mainService.setHomePage('state');
		tabService.addTabs({label:Messages('containerSupports.tabs.state'),href:jsRoutes.controllers.containers.tpl.ContainerSupports.home("new").url,remove:false});
		tabService.activeTab(0);
	}
	
	if(angular.isUndefined($scope.getForm())){
		$scope.form = {};
		mainService.setForm($scope.form);
	}else{
		$scope.form = mainService.getForm();			
	}
	
	$scope.searchService = containerSupportsSearchService;
	$scope.searchService.init($routeParams, $scope.datatableConfig)
	
	if($scope.form.project || $scope.form.type){
		$scope.search();
	}
		
}]);