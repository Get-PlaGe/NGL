package views.components.datatable;

import java.util.List;

public class DatatableResponse<T> {
	
	public List<T> data;
	public Integer recordsNumber;
	
	//This default constructor is needed for mapping and converting a JsonNode to a Java object when doing a test.
	public DatatableResponse(){
		
	}
	
	public DatatableResponse(List<T> data) {
		super();
		this.data = data;
		this.recordsNumber = data.size();
	}

	public DatatableResponse(List<T> data, Integer recordsNumber) {
		super();
		this.data = data;
		this.recordsNumber = recordsNumber;
	}

}
