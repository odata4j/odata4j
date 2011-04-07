package org.odata4j.core;

public enum ODataVersion {
	
	// order of definition is important
	V1("1.0"),
	V2("2.0");
	
	public final String asString;

	private ODataVersion(String asString) {
		this.asString = asString;
	}
	
	public static ODataVersion parse(String str) {
		if (V1.asString.equals(str))
			return V1;
		else if (V2.asString.equals(str)) {
			return V2;
		} else {
			throw new IllegalArgumentException("Unknown ODataVersion " + str);
		}
	}
}
