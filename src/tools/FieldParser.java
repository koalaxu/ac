package ac.tools;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FieldParser {
	public static class FieldInfo {
		public String[] field_names;
		public int[] indices;
	}

	public static FieldInfo Parse(String field_expression) {
		Matcher matcher = pattern.matcher(field_expression);
		if (!matcher.find()) return null;
		MatchResult result = matcher.toMatchResult();
		if (result.groupCount() != 6) return null;
		FieldInfo info = new FieldInfo();
		String field_names_string = result.group(1);
		if (field_names_string.isEmpty()) return null;
		String[] field_names = field_names_string.split("\\.");
		info.field_names = new String[field_names.length];
		for (int i = 0; i < field_names.length; ++i) {
			info.field_names[i] = field_names[i];
		}
		String indices_string = result.group(5);
		if (!indices_string.isEmpty()) {
			indices_string = indices_string.substring(1, indices_string.length() - 1).replaceAll("\\]\\[", ",");
			String[] indices = indices_string.split(",");
			info.indices = new int[indices.length];
			for (int i = 0; i < indices.length; ++i) {
				info.indices[i] = Integer.valueOf(indices[i]);
			}
		}
		return info;
	}
	
	public static class FieldLocator {
		public Object parant;
		public Field field;
		public Class<?> output_type;
		
		// Non-field;
		public Method getter;
		public Method setter;
		public Class<?> container_type;
		// Map
		public Object key;
		
		public boolean is_array;
		// public Type type;
	}
	
	public static FieldLocator LocateField(Object obj, FieldInfo info) throws
		NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
		FieldLocator locator = new FieldLocator();
		locator.output_type = obj.getClass();
		for (int i = 0; i < info.field_names.length; ++i) {
			locator.parant = obj;
			locator.field = locator.output_type.getField(info.field_names[i]);
			locator.output_type = locator.field.getType();
			obj = locator.field.get(obj);
		}
		if (info.indices != null) {
			Object value = null;
			String type_name = null;
			for (int i = 0; i < info.indices.length; ++i) {
				if (value != null) {
					obj = value;
					value = null;
					locator.field = null;
				}
				locator.parant = obj;
				if (locator.output_type == TreeMap.class) {
					ParameterizedType ptype = (ParameterizedType)locator.field.getGenericType();
					Type key_type = ptype.getActualTypeArguments()[0];
					Class<?> key_class = Class.forName(key_type.getTypeName());
					locator.key = key_class.getEnumConstants()[info.indices[i]];
					locator.getter = locator.output_type.getDeclaredMethod("get", Object.class);		
					locator.setter = locator.output_type.getDeclaredMethod("put", Object.class, Object.class);
					value = locator.getter.invoke(obj, locator.key);
					Type type = ptype.getActualTypeArguments()[1];
					Matcher matcher = class_pattern.matcher(type.getTypeName());
					if (matcher.find()) {
						MatchResult result = matcher.toMatchResult();
						String class_name = result.group(1);
						locator.output_type = Class.forName(class_name);
						if (result.groupCount() == 3) {
							type_name = result.group(3);
						}
						continue;
					}
				} else if (locator.output_type == LinkedList.class || locator.output_type == ArrayList.class) {
					if (locator.field != null) {
						ParameterizedType ptype = (ParameterizedType)locator.field.getGenericType();
						type_name = ptype.getActualTypeArguments()[0].getTypeName();
					}
					locator.key = info.indices[i];
					locator.getter = locator.output_type.getDeclaredMethod("get", int.class);		
					locator.setter = locator.output_type.getDeclaredMethod("set", int.class, Object.class);
					if (i + 1 < info.indices.length) {
						value = locator.getter.invoke(obj, info.indices[i]);
					}
					locator.container_type = locator.output_type;
					Matcher matcher = class_pattern.matcher(type_name);
					if (matcher.find()) {
						MatchResult result = matcher.toMatchResult();
						String class_name = result.group(1);
						locator.output_type = Class.forName(class_name);
						if (result.groupCount() == 3) {
							type_name = result.group(3);
						}
						continue;
					}
				} else if (locator.output_type == int[].class) {
					locator.key = info.indices[i];
					locator.getter = Array.class.getDeclaredMethod("getInt", Object.class, int.class);
					locator.setter = Array.class.getDeclaredMethod("setInt", Object.class, int.class, int.class);
					locator.output_type = int.class;
					locator.is_array = true;
				} else if (locator.output_type == long[][].class) {
					locator.key = info.indices[i];
					locator.output_type = long[].class;
					value = Array.get(obj, (int) locator.key);
				} else if (locator.output_type == long[].class) {
					locator.key = info.indices[i];
					locator.getter = Array.class.getDeclaredMethod("getLong", Object.class, int.class);
					locator.setter = Array.class.getDeclaredMethod("setLong", Object.class, int.class, long.class);
					locator.output_type = long.class;
					locator.is_array = true;
				} else if (locator.output_type.isArray()) {
					locator.key = info.indices[i];
					locator.getter = Array.class.getDeclaredMethod("get", Object.class, int.class);		
					locator.setter = Array.class.getDeclaredMethod("set", Object.class, int.class, Object.class);
					locator.output_type = locator.output_type.getComponentType();
					locator.is_array = true;
				} else {
					System.err.println("Unsupported locator.output_type: " + locator.output_type);
					System.exit(0);
				}
			}
		}
		return locator;
	}
	
	private static Pattern pattern = Pattern.compile("(([a-z_]+)((.[a-z_]+)*))((\\[\\d+\\])*)");
	private static Pattern class_pattern = Pattern.compile("([A-Za-z_\\.\\$]+)(\\<([A-Za-z_\\\\.]+)\\>)?");
}
