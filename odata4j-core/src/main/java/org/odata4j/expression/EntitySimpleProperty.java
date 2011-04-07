package org.odata4j.expression;

/** An expression which identifies an individual property on an entity
 */
public interface EntitySimpleProperty extends MemberExpression {

	/** Recover the name of the property
	 * 
	 * @return the name of the property
	 */
    public abstract String getPropertyName();
}
