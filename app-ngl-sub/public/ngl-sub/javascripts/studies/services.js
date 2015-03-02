"use strict";
 
 angular.module('ngl-sub.StudiesServices', []).
	factory('studiesCreateService', ['$http', 'mainService', 'lists', 'datatable', function($http, mainService, lists, datatable){
		
		var isInit = false;
		
		var initListService = function(){
			if(!isInit){
				createService.lists.refresh.projects();
				$http.get(jsRoutes.controllers.sra.api.Variables.get('existingStudyType').url)
				.success(function(data) {
				// initialisation de la variable createService.sraVariables.exitingStudyType utilisée dans create.scala.html
				createService.sraVariables.existingStudyType = data;																					
				});			
				isInit=true;
			}
		};
		
		
	
				
		var createService = {
				isRouteParam : false,
				lists : lists,
				form : undefined,
				sraVariables : {},
				setRouteParams:function($routeParams){
					var count = 0;
					for(var p in $routeParams){
						count++;
						break;
					}
					if(count > 0){
						this.isRouteParam = true;
						this.form = $routeParams;
					}
				},
						
						
				resetForm : function(){
					this.form = {};	
				},
										
				
				
			/**
			 * initialization of the service
			 */
			init : function($routeParams){
				initListService();
					
				//to avoid to lost the previous search
				if(angular.isDefined(mainService.getForm())){
					createService.form = mainService.getForm();
				}else{
					createService.resetForm();						
				}
					
				if(angular.isDefined($routeParams)){
					this.setRouteParams($routeParams);
				}
			}
		};
		
		
		return createService;

	}
]);
 