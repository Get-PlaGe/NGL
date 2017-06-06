 "use strict";
 
 angular.module('ngl-bi.ReadSetsServices', []).
	factory('readSetsSearchService', ['$http', 'mainService', 'lists', 'datatable', function($http, mainService, lists, datatable){
		
		var getDefaultColumns = function(){
			var columns = [];
			
			columns.push({	property:"code",
							"header":Messages("readsets.code"),
				    	  	type :"text",		    	  	
				    	  	order:true,
				    	  	position:1});
			columns.push({	property:"runCode",
							header: Messages("readsets.runCode"),
							type :"text",
							group:true,
							order:true,
				    	  	position:2});
			columns.push({	property:"laneNumber",
							header: Messages("readsets.laneNumber"),
							type :"text",
							order:true,
				    	  	position:3});
			columns.push({	property:"projectCode",
							header: Messages("readsets.projectCode"),
							type :"text",
							order:true,
							group:true,
							groupMethod:"countDistinct",
				    	  	position:4});			
			columns.push({	property:"sampleCode",
							header: Messages("readsets.sampleCode"),
							type :"text",
							group:true,
							groupMethod:"countDistinct",
							order:true,
				    	  	position:5});
			columns.push({	property:"runSequencingStartDate",
							header: Messages("runs.sequencingStartDate"),
							type :"date",
							order:true,
				    	  	position:6});
			if(mainService.getHomePage() == 'search'){
					columns.push({	"property":"state.code",
									"filter":"codes:'state'",
									"header":Messages("readsets.stateCode"),
									"type":"text",
									"order":true,
									"position":7
					});
					
					columns.push({	"property":"productionValuation.valid",
									"filter":"codes:'valuation'",
									"header":Messages("readsets.productionValuation.valid"),
									"type":"text",
									"order":true,
									"position":70
					});
					
					columns.push({	"property":"productionValuation.resolutionCodes",
									"header":Messages("readsets.productionValuation.resolutions"),
									"filter":"codes:'resolution'",
									"groupMethod":"collect",
									"render":'<div bt-select ng-model="cellValue" bt-options="valid.name as valid.name group by valid.category.name for valid in searchService.lists.getResolutions()" ng-edit="false"></div>',
									"type":"text",
									"hide":true,
									"position":72
					});
					
					columns.push({	"property":"bioinformaticValuation.valid",
									"filter":"codes:'valuation'",
									"header":Messages("readsets.bioinformaticValuation.valid"),
									"type":"text",
									"order":true,
									"position":80
					});
					
					columns.push({	"property":"bioinformaticValuation.resolutionCodes",
									"header":Messages("readsets.bioinformaticValuation.resolutions"),
									"filter":"codes:'resolution'",
									"groupMethod":"collect",
									"render":'<div bt-select ng-model="cellValue" bt-options="valid.name as valid.name group by valid.category.name for valid in searchService.lists.getResolutions()" ng-edit="false"></div>',
									"type":"text",
									"hide":true,
									"position":82
					});
			}else if(mainService.getHomePage() == 'valuation'){
					columns.push({	"property":"state.code",
									"filter":"codes:'state'",
									"header":Messages("readsets.stateCode"),
									"type":"text",
									"order":true,
									"position":7
					});
					
					columns.push({	"property":"productionValuation.valid",
									"filter":"codes:'valuation'",
									"header":Messages("readsets.productionValuation.valid"),
									"type":"text",
									"order":true,
									"edit":true,
									"choiceInList":true,
									"listStyle":'bt-select',
							    	"possibleValues":'searchService.lists.getValuations()',
							    	"position":70
					});
					
					columns.push({	"property":"productionValuation.criteriaCode",
									"filter":"codes:'valuation_criteria'",
									"header":Messages("readsets.productionValuation.criteria"),
									"type":"text",
									"edit":true,
									"choiceInList":true,
									"listStyle":'bt-select',
							    	"possibleValues":'searchService.lists.getValuationCriterias()',
							    	"position":71
				    });
					
					columns.push({	"property":"productionValuation.resolutionCodes",
									"header":Messages("readsets.productionValuation.resolutions"),
									"render":'<div bt-select ng-model="value.data.productionValuation.resolutionCodes" bt-options="valid.code as valid.name group by valid.category.name for valid in searchService.lists.getResolutions()" ng-edit="false"></div>',
									"type":"text",
									"edit":true,
									"choiceInList":true,
									"listStyle":'bt-select-multiple',
							    	"possibleValues":'searchService.lists.getResolutions()',
							    	"groupBy":'category.name',
							    	"position":72
							    		
					});
					
					columns.push({	"property":"bioinformaticValuation.valid",
									"filter":"codes:'valuation'",
									"header":Messages("readsets.bioinformaticValuation.valid"),
									"type":"text",
									"order":true,
									"edit":true,
									"choiceInList":true,
									"listStyle":'bt-select',
							    	"possibleValues":'searchService.lists.getValuations()',
							    	"position":80
					});	
					
					columns.push({	"property":"bioinformaticValuation.resolutionCodes",
									"header":Messages("readsets.bioinformaticValuation.resolutions"),
									"render":'<div bt-select ng-model="value.data.bioinformaticValuation.resolutionCodes" bt-options="valid.code as valid.name group by valid.category.name for valid in searchService.lists.getResolutions()" ng-edit="false"></div>',
									"type":"text",
									"edit":true,
									"choiceInList":true,
									"listStyle":'bt-select-multiple',
							    	"possibleValues":'searchService.lists.getResolutions()',
							    	"groupBy":'category.name',
							    	"position":82
					});
					
			}else if(mainService.getHomePage() == 'state'){
					columns.push({	"property":"state.code",
									"filter":"codes:'state'",
									"header":Messages("readsets.stateCode"),
									"type":"text",
									"edit":true,
									"order":true,
									"choiceInList":true,
									"listStyle":'bt-select',
							    	"possibleValues":'searchService.lists.getStates()',
							    	"position":7
					});
					
					columns.push({	"property":"productionValuation.valid",
									"filter":"codes:'valuation'",
									"header":Messages("readsets.productionValuation.valid"),
									"type":"text",
									"order":true,
									"position":70    	
					});
					
					columns.push({	"property":"bioinformaticValuation.valid",
									"filter":"codes:'valuation'",
									"header":Messages("readsets.bioinformaticValuation.valid"),
									"type":"text",
									"order":true,
									"position":80
					});
					
			}else if(mainService.getHomePage() == 'batch'){
					columns.push({	"property":"state.code",
									"filter":"codes:'state'",
									"header":Messages("readsets.stateCode"),
									"type":"text",
									"order":true,
							    	"choiceInList":true,
							    	"listStyle":'bt-select',
							    	"possibleValues":'searchService.lists.getStates()',
							    	"position":7
					});
					columns.push({	"property":"productionValuation.valid",
									"filter":"codes:'valuation'",
									"header":Messages("readsets.productionValuation.valid"),
									"type":"text",
									"order":true,
									"position":70    	
					});
					columns.push({	"property":"bioinformaticValuation.valid",
									"filter":"codes:'valuation'",
									"header":Messages("readsets.bioinformaticValuation.valid"),
									"type":"text",
									"order":true,
									"position":80
				    });
					columns.push({	"property":"properties.isSentCCRT.value",
									"header":Messages("readsets.properties.isSentCCRT"),
									"type":"boolean",
									"edit":true,
									"position":90
					});
					columns.push({	"property":"properties.isSentCollaborator.value",
									"header":Messages("readsets.properties.isSentCollaborator"),
									"type":"boolean",
									"edit":true,
									"position":91
					});
			}
			
			return columns;
		};
		
		var isInit = false;
		
		var initListService = function(){
			if(!isInit){
				lists.refresh.projects();
				lists.refresh.states({objectTypeCode:"ReadSet", display:true},'statetrue');
				lists.refresh.states({objectTypeCode:"ReadSet"});			
				lists.refresh.valuationCriterias({objectTypeCode:"ReadSet", orderBy:'name'});
				lists.refresh.types({objectTypeCode:"Run"},"runTypes");
				lists.refresh.types({objectTypeCode:"ReadSet"},"readSetTypes");
				lists.refresh.runs();
				lists.refresh.instruments({categoryCodes:["illumina-sequencer","extseq","nanopore-sequencer"]});
				//TODO Warn if pass to one application page
				lists.refresh.reportConfigs({pageCodes:["readsets"+"-"+mainService.getHomePage()]});
				lists.refresh.reportConfigs({pageCodes:["readsets-addcolumns"]}, "readsets-addcolumns");
				lists.refresh.filterConfigs({pageCodes:["readsets-addfilters"]}, "readsets-addfilters");
				
				lists.refresh.resolutions({objectTypeCode:"ReadSet"});
				
				lists.refresh.users();
				isInit=true;
			}
		};
		
		var searchService = {
				getDefaultColumns:getDefaultColumns,
				datatable:undefined,
				isRouteParam:false,
				lists : lists,
				form : undefined,
				reportingConfigurationCode:undefined,
				reportingConfiguration:undefined,
				additionalColumns:[],
				additionalFilters:[],
				selectedAddColumns:[],
				regexColumn:undefined,
				
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
				
				updateForm : function(){
					if (mainService.isHomePage('valuation')) {
						if(!this.isRouteParam && (this.form.stateCodes === undefined || this.form.stateCodes.length === 0)) {
							//No stateCodes selected, the filter by default (on the only two possible states for the valuation) is applied
							this.form.stateCodes = ["IW-VQC", "IP-VQC", "IW-VBA"];
						}		
					}
					this.form.includes = [];
					if(this.reportingConfiguration){
						for(var i = 0 ; i < this.reportingConfiguration.columns.length ; i++){
							if(this.reportingConfiguration.columns[i].queryIncludeKeys && this.reportingConfiguration.columns[i].queryIncludeKeys.length > 0){
								this.form.includes = this.form.includes.concat(this.reportingConfiguration.columns[i].queryIncludeKeys);
							}else{
								this.form.includes.push(this.reportingConfiguration.columns[i].property.replace('.value',''));	
							}
						}
					}else{
						this.form.includes = ["default"];
					}
					
					for(var i = 0 ; i < this.selectedAddColumns.length ; i++){
						//remove .value if present to manage correctly properties (single, list, etc.)
						if(this.selectedAddColumns[i].queryIncludeKeys && this.selectedAddColumns[i].queryIncludeKeys.length > 0){
							this.form.includes = this.form.includes.concat(this.selectedAddColumns[i].queryIncludeKeys);
						}else{
							this.form.includes.push(this.selectedAddColumns[i].property.replace('.value',''));	
						}
						
					}
				},
				
				convertForm : function(){
					var _form = angular.copy(this.form);
					if(_form.fromDate)_form.fromDate = moment(_form.fromDate, Messages("date.format").toUpperCase()).valueOf();
					if(_form.toDate)_form.toDate = moment(_form.toDate, Messages("date.format").toUpperCase()).valueOf();		
					return _form
				},
				
				resetForm : function(){
					this.form = {};									
				},
				
				resetSampleCodes : function(){
					this.form.sampleCodes = [];									
				},
				
				search : function(){
					this.updateForm();
					mainService.setForm(this.form);
					this.datatable.search(this.convertForm());
				},
				
				refreshSamples : function(){
					if(this.form.projectCodes && this.form.projectCodes.length > 0){
						this.lists.refresh.samples({projectCodes:this.form.projectCodes});
					}
				},
				valuationStates : [{code:"IW-VQC",name:Codes("state.IW-VQC")},{code:"IP-VQC",name:Codes("state.IP-VQC")},{code:"IW-VBA",name:Codes("state.IW-VBA")}],
				states : function(){
					if (mainService.isHomePage('valuation')) {
						return this.valuationStates;
					}else{
						return this.lists.get('statetrue');
					}
				},
				/**
				 * Update column when change reportingConfiguration
				 */
				updateColumn : function(){
					this.initAdditionalColumns();
					if(this.reportingConfigurationCode){
						$http.get(jsRoutes.controllers.reporting.api.ReportingConfigurations.get(this.reportingConfigurationCode).url,{searchService:this, datatable:this.datatable})
								.success(function(data, status, headers, config) {
									config.searchService.reportingConfiguration = data;
									config.searchService.search();
									config.datatable.setColumnsConfig(data.columns);																								
						});
					}else{
						this.reportingConfiguration = undefined;
						this.datatable.setColumnsConfig(this.getDefaultColumns());
						this.search();
					}
					
				},
				
				initAdditionalColumns:function(){
					this.additionalColumns=[];
					this.selectedAddColumns=[];
					
					if(lists.get("readsets-addcolumns") && lists.get("readsets-addcolumns").length === 1){
						var formColumns = [];
						var allColumns = angular.copy(lists.get("readsets-addcolumns")[0].columns);
						var nbElementByColumn = Math.ceil(allColumns.length / 5); //5 columns
						for(var i = 0; i  < 5 && allColumns.length > 0 ; i++){
							formColumns.push(allColumns.splice(0, nbElementByColumn));	    								
						}
						//complete to 5 five element to have a great design 
						while(formColumns.length < 5){
							formColumns.push([]);
						}
						this.additionalColumns = formColumns;
					}
				},
				
				refreshAdditionalColumns:function(){
					if(this.additionalColumns.length === 0){
						this.initAdditionalColumns();
					}
					if(this.regexColumn && this.additionalColumns.length>0){
						console.log("Refresh additionnal column");
					}
				},
				
				getAddColumnsToForm : function(){
					if(this.additionalColumns.length === 0){
						this.initAdditionalColumns();
					}
					return this.additionalColumns;									
				},
				
				addColumnsToDatatable:function(){
					//this.reportingConfiguration = undefined;
					//this.reportingConfigurationCode = undefined;
					
					this.selectedAddColumns = [];
					for(var i = 0 ; i < this.additionalColumns.length ; i++){
						for(var j = 0; j < this.additionalColumns[i].length; j++){
							if(this.additionalColumns[i][j].select){
								this.selectedAddColumns.push(this.additionalColumns[i][j]);
							}
						}
					}
					if(this.reportingConfigurationCode){
						this.datatable.setColumnsConfig(this.reportingConfiguration.columns.concat(this.selectedAddColumns));
					}else{
						this.datatable.setColumnsConfig(this.getDefaultColumns().concat(this.selectedAddColumns));						
					}
					this.search();
				},	
				
				resetDatatableColumns:function(){
					this.initAdditionalColumns();
					this.datatable.setColumnsConfig(this.getDefaultColumns());
					this.search();
				},

				initAdditionalFilters:function(){
					this.additionalFilters=[];
					
					if(lists.get("readsets-addfilters") && lists.get("readsets-addfilters").length === 1){
						var formFilters = [];
						var allFilters = angular.copy(lists.get("readsets-addfilters")[0].filters);
						var nbElementByColumn = Math.ceil(allFilters.length / 5); //5 columns
						for(var i = 0; i  < 5 && allFilters.length > 0 ; i++){
							formFilters.push(allFilters.splice(0, nbElementByColumn));	    								
						}
						//complete to 5 five element to have a great design 
						while(formFilters.length < 5){
							formFilters.push([]);
						}
							
						this.additionalFilters = formFilters;
					}
				},
				
				getAddFiltersToForm : function(){
					if(this.additionalFilters.length === 0){
						this.initAdditionalFilters();
					}
					return this.additionalFilters;									
				},	
				/**
				 * initialise the service
				 */
				init : function($routeParams, datatableConfig){
					initListService();
					
					datatableConfig.messages = {
							transformKey: function(key, args) {
		                        return Messages(key, args);
		                    }
					};
					
					//to avoid to lost the previous search
					if(datatableConfig && angular.isUndefined(mainService.getDatatable())){
						searchService.datatable = datatable(datatableConfig);
						mainService.setDatatable(searchService.datatable);
						searchService.datatable.setColumnsConfig(getDefaultColumns());		
					}else if(angular.isDefined(mainService.getDatatable())){
						searchService.datatable = mainService.getDatatable();			
					}	
					
					
					if(angular.isDefined(mainService.getForm())){
						searchService.form = mainService.getForm();
					}else{
						searchService.resetForm();						
					}
					
					if(angular.isDefined($routeParams)){
						this.setRouteParams($routeParams);
					}
				}
		};
		
		return searchService;				
	}
]);
 