package org.odata4j.edm;

public class EdmAssociation {

	public final String namespace;
	public final String name;
	public final EdmAssociationEnd end1;
	public final EdmAssociationEnd end2;
	
	public EdmAssociation(String namespace, String name, EdmAssociationEnd end1, EdmAssociationEnd end2){
		this.namespace = namespace;
		this.name = name;
		this.end1 = end1;
		this.end2 = end2;
	}

	public String getFQName() {
		return namespace + "." + name;
	}
}
