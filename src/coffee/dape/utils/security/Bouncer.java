package coffee.dape.utils.security;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Objects;

import coffee.dape.exception.IllegalMethodCallException;
import coffee.dape.utils.Logg;
import coffee.dape.utils.MathUtils;

/**
 * @author Laeven
 * Acts as a way to block method calls from classes/objects that shouldn't be calling a method
 */
public final class Bouncer
{
	/**
	 * Halts a method call unless the class & method calling this method is of the arguments passed
	 * @param clazz Class to allow a method call from
	 * @param meth Method to allow a method call from
	 * @throws IllegalMethodCallException Thrown when The class and method calling a method does not match arguments provided
	 */
	public static final void haltAllBut(final Class<?> clazz,final Method meth) throws IllegalMethodCallException
	{
		StackTraceElement ele = Thread.currentThread().getStackTrace()[3];
		if(ele.getClassName().equals(clazz.getName()) && ele.getMethodName().equals(meth.getName())) { return; }
		
		throw new IllegalMethodCallException(ele.getClassName(),ele.getMethodName(),ele.getLineNumber());
	}
	
	/**
	 * Halts a method call unless the class calling this method is one of the arguments passed
	 * @param clazzes Class to allow a method call from
	 * @throws IllegalMethodCallException Thrown when The class calling a method does not match any class arguments provided
	 */
	public static final void haltAllBut(final Class<?>... clazzes) throws IllegalMethodCallException
	{
		StackTraceElement ele = Thread.currentThread().getStackTrace()[3];
		
		for(Class<?> clazz : clazzes)
		{
			if(ele.getClassName().equals(clazz.getName())) { return; }
		}
		
		throw new IllegalMethodCallException(ele.getClassName(),ele.getMethodName(),ele.getLineNumber());
	}
	
	/**
	 * Halts a method call if the class & method calling this method is of the arguments passed
	 * @param clazz Class to block a method call from
	 * @param meth Method to block a method call from
	 * @throws IllegalMethodCallException Thrown when The class and method calling a method matches arguments provided
	 */
	public static final void haltIfCallFrom(final Class<?> clazz,final Method meth) throws IllegalMethodCallException
	{
		StackTraceElement ele = Thread.currentThread().getStackTrace()[3];
		if(!ele.getClassName().equals(clazz.getName()) && !ele.getMethodName().equals(meth.getName())) { return; }
		
		throw new IllegalMethodCallException(ele.getClassName(),ele.getMethodName(),ele.getLineNumber());
	}
	
	/**
	 * Probes the stack trace where {@link Bouncer#probe()} is called showing what where the last call was from
	 */
	public static final void probeLastCall()
	{
		StackTraceElement ele = Thread.currentThread().getStackTrace()[3];
		Logg.info("Bouncer Probe -> \n" + 
				  "    Class: " + ele.getClassName() + "\n" +
				  "    Method: " + ele.getMethodName() + "\n" +
				  "    Line: " + ele.getLineNumber());
	}
	
	
	/**
	 * Probes the stack trace where {@link Bouncer#probe()} is called showing what where the last call was from
	 */
	public static final void probe()
	{
		char arrowLeftUp = '\u2514';
		StringBuilder sb = new StringBuilder();
		StackTraceElement ele = Thread.currentThread().getStackTrace()[3];
		
		sb.append("Bouncer Probe -> \n");
		sb.append("&a" + ele.getClassName() + " &f| &9" + ele.getMethodName() + " &f| &e" + ele.getLineNumber() + "\n");
		ele = Thread.currentThread().getStackTrace()[4];
		sb.append("&f" + arrowLeftUp + " &a" + ele.getClassName() + " &f| &9" + ele.getMethodName() + " &f| &e" + ele.getLineNumber() + "\n");
		
		for(int i = 5; i < Thread.currentThread().getStackTrace().length; i++)
		{
			ele = Thread.currentThread().getStackTrace()[i];
			sb.append("  &a" + ele.getClassName() + " &f| &9" + ele.getMethodName() + " &f| &e" + ele.getLineNumber() + "\n");
		}
		
		Logg.info(sb.toString());
	}
	
	/**
	 * Checks a string is not null, have a length of 0, is not empty or blank
	 * @param string String to check
	 * @param message Message to display should string not meet these conditions
	 */
	public static final void requireNotNullOrEmpty(String string,String message)
	{
		Objects.requireNonNull(string,message);
		if(string.length() == 0 || string.isEmpty() || string.isBlank())
		{
			Logg.throwIllegalArgumentError(message);
			return;
		}
	}
	
	/**
	 * Checks an array is not null, has a length greater than 0, and checks the first array index if a value exists
	 * @param array The array object to check
	 * @param message Message to display should string not meet these conditions
	 */
	public static final void requireNotNullOrEmpty(Object[] array,String message)
	{
		Objects.requireNonNull(array,message);
		if(array.length == 0)
		{
			Logg.throwIllegalArgumentError(message);
			return;
		}
		
		Objects.requireNonNull(array[0],message);
	}
	
	/**
	 * Checks an array is not null, has a length greater than 0, and checks the first array index if a value exists
	 * @param array The array object to check
	 * @param message Message to display should string not meet these conditions
	 */
	public static final void requireNotNullOrEmpty(byte[] array,String message)
	{
		Objects.requireNonNull(array,message);
		if(array.length == 0)
		{
			Logg.throwIllegalArgumentError(message);
			return;
		}
		
		Objects.requireNonNull(array[0],message);
	}
	
	/**
	 * Checks an array is not null, has a length greater than 0, and checks the first array index if a value exists
	 * @param array The array object to check
	 * @param message Message to display should string not meet these conditions
	 */
	public static final void requireNotNullOrEmpty(short[] array,String message)
	{
		Objects.requireNonNull(array,message);
		if(array.length == 0)
		{
			Logg.throwIllegalArgumentError(message);
			return;
		}
		
		Objects.requireNonNull(array[0],message);
	}
	
	/**
	 * Checks an array is not null, has a length greater than 0, and checks the first array index if a value exists
	 * @param array The array object to check
	 * @param message Message to display should string not meet these conditions
	 */
	public static final void requireNotNullOrEmpty(int[] array,String message)
	{
		Objects.requireNonNull(array,message);
		if(array.length == 0)
		{
			Logg.throwIllegalArgumentError(message);
			return;
		}
		
		Objects.requireNonNull(array[0],message);
	}
	
	/**
	 * Checks an array is not null, has a length greater than 0, and checks the first array index if a value exists
	 * @param array The array object to check
	 * @param message Message to display should string not meet these conditions
	 */
	public static final void requireNotNullOrEmpty(long[] array,String message)
	{
		Objects.requireNonNull(array,message);
		if(array.length == 0)
		{
			Logg.throwIllegalArgumentError(message);
			return;
		}
		
		Objects.requireNonNull(array[0],message);
	}
	
	/**
	 * Checks an array is not null, has a length greater than 0, and checks the first array index if a value exists
	 * @param array The array object to check
	 * @param message Message to display should string not meet these conditions
	 */
	public static final void requireNotNullOrEmpty(float[] array,String message)
	{
		Objects.requireNonNull(array,message);
		if(array.length == 0)
		{
			Logg.throwIllegalArgumentError(message);
			return;
		}
		
		Objects.requireNonNull(array[0],message);
	}
	
	/**
	 * Checks an array is not null, has a length greater than 0, and checks the first array index if a value exists
	 * @param array The array object to check
	 * @param message Message to display should string not meet these conditions
	 */
	public static final void requireNotNullOrEmpty(double[] array,String message)
	{
		Objects.requireNonNull(array,message);
		if(array.length == 0)
		{
			Logg.throwIllegalArgumentError(message);
			return;
		}
		
		Objects.requireNonNull(array[0],message);
	}
	
	/**
	 * Checks an array is not null, has a length greater than 0, and checks the first array index if a value exists
	 * @param array The array object to check
	 * @param message Message to display should string not meet these conditions
	 */
	public static final void requireNotNullOrEmpty(boolean[] array,String message)
	{
		Objects.requireNonNull(array,message);
		if(array.length == 0)
		{
			Logg.throwIllegalArgumentError(message);
			return;
		}
		
		Objects.requireNonNull(array[0],message);
	}
	
	/**
	 * Checks an array is not null, has a length greater than 0, and checks the first array index if a value exists
	 * @param array The array object to check
	 * @param message Message to display should string not meet these conditions
	 */
	public static final void requireNotNullOrEmpty(char[] array,String message)
	{
		Objects.requireNonNull(array,message);
		if(array.length == 0)
		{
			Logg.throwIllegalArgumentError(message);
			return;
		}
		
		Objects.requireNonNull(array[0],message);
	}
	
	/**
	 * Checks a collection is not null and is not empty
	 * @param collection The Collection object to check
	 * @param message Message to display should string not meet these conditions
	 */
	public static final void requireNotNullOrEmpty(Collection<?> collection,String message)
	{
		Objects.requireNonNull(collection,message);
		if(collection.isEmpty())
		{
			Logg.throwIllegalArgumentError(message);
			return;
		}
	}
	
	/**
	 * Checks a value is not null and is within a range
	 * @param value The value to check
	 * @param message Message to display should string not meet these conditions
	 */
	public static final void requireNotNullAndInRange(int value,int lowerBound,int upperBound,String message)
	{
		Objects.requireNonNull(value,message);
		if(!MathUtils.inclusiveRange(lowerBound,upperBound,value))
		{
			Logg.throwIllegalArgumentError(message);
			return;
		}
	}
	
	/**
	 * Checks a value is not null and is within a range
	 * @param value The value to check
	 * @param message Message to display should string not meet these conditions
	 */
	public static final void requireNotNullAndInRange(long value,long lowerBound,long upperBound,String message)
	{
		Objects.requireNonNull(value,message);
		if(!MathUtils.inclusiveRange(lowerBound,upperBound,value))
		{
			Logg.throwIllegalArgumentError(message);
			return;
		}
	}
}
