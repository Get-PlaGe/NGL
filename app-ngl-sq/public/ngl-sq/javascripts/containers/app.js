"use strict";

angular.module('home', ['commonsServices','ngRoute','ultimateDataTableServices','ui.bootstrap','ngl-sq.containersServices','dragndropServices'], function($routeProvider, $locationProvider) {
	$routeProvider.when('/containers/search/home', {
		templateUrl : jsRoutes.controllers.containers.tpl.Containers.search().url,
		controller : 'SearchCtrl'
	});
	$routeProvider.when('/containers/state/home', {
		templateUrl : jsRoutes.controllers.containers.tpl.Containers.search().url,
		controller : 'SearchStateCtrl'
	});
	$routeProvider.otherwise({redirectTo: jsRoutes.controllers.containers.tpl.Containers.home("search").url});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
});