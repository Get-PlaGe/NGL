"use strict";
 
angular.module('home', ['commonsServices','ngRoute','datatableServices','basketServices','ui.bootstrap','ngl-reagent.kitCatalogsService'], function($routeProvider, $locationProvider) {
	$routeProvider.when('/reagent-catalogs/new/home', {
		templateUrl : jsRoutes.controllers.reagents.tpl.KitCatalogs.createOrEdit().url,
		controller : 'CreationKitsCtrl'
	});
	
	$routeProvider.when('/reagent-catalogs/search/home', {
		templateUrl : jsRoutes.controllers.reagents.tpl.KitCatalogs.search().url,
		controller : 'SearchKitsCtrl'
	});
	
	$routeProvider.when('/reagent-catalogs/kits/:kitCatalogCode', {
		templateUrl : jsRoutes.controllers.reagents.tpl.KitCatalogs.createOrEdit().url,
		controller : 'CreationKitsCtrl'
	});
	
	$routeProvider.otherwise({redirectTo: '/reagent-catalogs/new/home'});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
});