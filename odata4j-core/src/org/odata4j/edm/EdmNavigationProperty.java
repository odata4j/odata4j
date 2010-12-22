package org.odata4j.edm;

public class EdmNavigationProperty {

	public final String name;
	public final EdmAssociation relationship;
	public final EdmAssociationEnd fromRole;
	public final EdmAssociationEnd toRole;

	public boolean selected = true;

	public EdmNavigationProperty(
			String name, 
			EdmAssociation relationship,
			EdmAssociationEnd fromRole, 
			EdmAssociationEnd toRole) {
		this.name = name;
		this.relationship = relationship;
		this.fromRole = fromRole;
		this.toRole = toRole;
	}
}
