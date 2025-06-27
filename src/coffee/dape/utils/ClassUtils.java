package coffee.dape.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ClassUtils
{
	public static Set<Class<?>> getAllInterfacesImplemented(Class<?> clazz)
	{
		Set<Class<?>> res = new HashSet<Class<?>>();
		Class<?>[] interfaces = clazz.getInterfaces();
		
		if(interfaces.length == 0) { return res; }
		
		res.addAll(Arrays.asList(interfaces));
		
		for(Class<?> interfacee : interfaces)
		{
			res.addAll(getAllInterfacesImplemented(interfacee));
		}
		
		return res;
	}
	
	public static boolean hasInterface(Class<?> clazz,Class<?> interfacee)
	{
		return getAllInterfacesImplemented(clazz).contains(interfacee);
	}
}
