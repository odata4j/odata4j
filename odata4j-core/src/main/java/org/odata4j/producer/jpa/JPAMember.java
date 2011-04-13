package org.odata4j.producer.jpa;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import javax.persistence.metamodel.Attribute;

import org.core4j.CoreUtils;

public abstract class JPAMember {

	public abstract Class<?> getJavaType();
	public abstract boolean isReadable();
	public abstract boolean isWriteable();
	public abstract <T> T get(Object target);
	public abstract <T> void set(Object target, T value);
	public abstract <T extends Annotation> T getAnnotation(Class<T> annotationClass);

	public static JPAMember create(Attribute<?,?> jpaAttribute){
		Member javaMember = jpaAttribute.getJavaMember();
		if (javaMember instanceof Field)
			return new FieldMember((Field)javaMember);
		if (javaMember instanceof Method)
			return new GetterSetterMember((Method)javaMember,null);
		
		// http://wiki.eclipse.org/EclipseLink/Development/JPA_2.0/metamodel_api#DI_95:_20091017:_Attribute.getJavaMember.28.29_returns_null_for_a_BasicType_on_a_MappedSuperclass_because_of_an_uninitialized_accessor
		JPAMember rt = reverseEngineerJPAMember(jpaAttribute.getDeclaringType().getJavaType(), jpaAttribute.getName());
		
		if (rt==null)
			throw new IllegalArgumentException("Could not find java member for: " + jpaAttribute);
		return rt;
	}
	
	
	

	private static JPAMember reverseEngineerJPAMember(Class<?> type, String name) {
		try {
			Field field = CoreUtils.getField(type, name);
			return new FieldMember(field);
		} catch (Exception ignore) {
		}

		// TODO handle setters, overloads
		String methodName = "get" + Character.toUpperCase(name.charAt(0))+ name.substring(1);
		while (!type.equals(Object.class)) {
			try {
				Method method = type.getDeclaredMethod(methodName);
				return new GetterSetterMember(method,null);
			} catch (Exception ignore) {
			}
			type = type.getSuperclass();
		}
		return null;
	}
	
	
	private static class FieldMember extends JPAMember{

		private final Field field;
		public FieldMember(Field field){
			this.field = field;
			this.field.setAccessible(true);
		}
	

		@Override
		public Class<?> getJavaType() {
			return field.getType();
		}

		@Override
		public boolean isReadable() {
			return true;
		}

		@Override
		public boolean isWriteable() {
			return true;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T get(Object target) {
			try {
				return (T)field.get(target);
			} catch (Exception e){
				throw new RuntimeException(e);
			}
		}

		@Override
		public <T> void set(Object target, T value) {
			try {
				field.set(target,value);
			} catch (Exception e){
				throw new RuntimeException(e);
			}
		}
		
		@Override
		public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
			return field.getAnnotation(annotationClass);
		}
	}
	
	private static class GetterSetterMember extends JPAMember {

		private final Method getter;
		private final Method setter;
		
		public GetterSetterMember( Method getter, Method setter){
			this.getter = getter;
			this.setter = setter;
			
			if (getter!=null)
				getter.setAccessible(true);
			if (setter!=null)
				setter.setAccessible(true);
		}
		

		@Override
		public Class<?> getJavaType() {
			return getter.getReturnType();
		}

		@Override
		public boolean isReadable() {
			return getter!=null;
		}

		@Override
		public boolean isWriteable() {
			return setter!=null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T get(Object target) {
			if (getter==null)
				throw new RuntimeException("Member is not readable");
			try {
				return (T)getter.invoke(target);
			} catch (Exception e){
				throw new RuntimeException(e);
			}
		}

		@Override
		public <T> void set(Object target, T value) {
			if (setter==null)
				throw new RuntimeException("Member is not writeable");
			try {
				setter.invoke(target, value);
			} catch (Exception e){
				throw new RuntimeException(e);
			}
		}
		
		@Override
		public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
			return getter.getAnnotation(annotationClass);
		}
	}
	

}
