package org.odata4j.producer.jpa;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import javax.persistence.metamodel.Attribute;

import org.core4j.CoreUtils;

public class JPAMember {
	
	private final Member member;
	private JPAMember(Member member){
		if (member==null)
			throw new IllegalArgumentException("member cannot be null");
		this.member = member;
	}
	
	public static JPAMember create(Attribute<?,?> jpaAttribute){
		Member member = jpaAttribute.getJavaMember();
		
		if (member == null) { // http://wiki.eclipse.org/EclipseLink/Development/JPA_2.0/metamodel_api#DI_95:_20091017:_Attribute.getJavaMember.28.29_returns_null_for_a_BasicType_on_a_MappedSuperclass_because_of_an_uninitialized_accessor
			member = getJavaMember(jpaAttribute.getDeclaringType().getJavaType(), jpaAttribute.getName());
		}
		
		return new JPAMember(member);
	}
	
	
	public Class<?> getType(){
		Field field = asField();
		return field.getType();
	}
	
	public void set(Object target, Object value){
		try {
			if (member instanceof Field) {
	    		Field field = (Field) member;
	    		field.setAccessible(true);
	    		field.set(target, value);
			} else if (member instanceof Method) {
				throw new UnsupportedOperationException("Implement member"
						+ member + " as field");
			} else {
				throw new UnsupportedOperationException("Implement member" + member);
			}
		} catch (Exception e){
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(Object target){
		try {
			if (member instanceof Field) {
	    		Field field = (Field) member;
	    		field.setAccessible(true);
	    		return (T) field.get(target);
			} else if (member instanceof Method) {
				Method method = (Method) member;
				method.setAccessible(true);
				return (T) method.invoke(target);
			} else {
				throw new UnsupportedOperationException("Implement member" + member);
			}
		} catch (Exception e){
			throw new RuntimeException(e);
		}
	}
	
	private Field asField(){
		if (!(member instanceof Field)) 
			throw new UnsupportedOperationException("Implement member" + member);
	
		return (Field) member;
	}
	
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		if (member instanceof Method) {
			Method method = (Method) member;
			return method.getAnnotation(annotationClass);
		} else if (member instanceof Field) {
			Field field = (Field) member;
			return field.getAnnotation(annotationClass);
		} else
			throw new IllegalArgumentException("only methods and fields are allowed");
	}

	private static Member getJavaMember(Class<?> type, String name) {
		try {
			Field field = CoreUtils.getField(type, name);
			field.setAccessible(true);
			return field;
		} catch (Exception ignore) {
		}

		String methodName = "get" + Character.toUpperCase(name.charAt(0))
				+ name.substring(1);
		while (!type.equals(Object.class)) {
			try {
				Method method = type.getDeclaredMethod(methodName);
				method.setAccessible(true);
				return method;
			} catch (Exception ignore) {
			}
			type = type.getSuperclass();
		}
		return null;
	}

}
