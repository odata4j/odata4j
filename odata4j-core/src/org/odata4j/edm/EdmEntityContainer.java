package org.odata4j.edm;

import java.util.List;

import core4j.Enumerable;

public class EdmEntityContainer {

	public final String name;
	public final boolean isDefault;
	public final List<EdmEntitySet> entitySets;
	public final List<EdmAssociationSet> associationSets;
	
	public EdmEntityContainer(String name, boolean isDefault, List<EdmEntitySet> entitySets, List<EdmAssociationSet> associationSets){
		this.name = name;
		this.isDefault = isDefault;
		this.entitySets = entitySets==null?Enumerable.empty(EdmEntitySet.class).toList():entitySets;
		this.associationSets = associationSets==null?Enumerable.empty(EdmAssociationSet.class).toList():associationSets;
	}
}
