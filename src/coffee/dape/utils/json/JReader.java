package coffee.dape.utils.json;

import java.io.File;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import coffee.dape.exception.InvalidJsonAttributeDataTypeException;
import coffee.dape.exception.MissingJsonAttributeException;

/**
 * 
 * @author Laeven
 *
 */
public abstract class JReader<T>
{
	// Source name can be custom or the filename
	private String sourceName;
	public JsonObject obj;
	
	public T fromJson(File file)
	{
		this.obj = JUtils.readToObject(file.toPath());
		this.sourceName = file.getName() != null ? file.getName() : "unknown";
		
		try
		{
			return readJson();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public T fromJson(String sourceName,String json)
	{
		this.obj = JUtils.toJsonObject(json);
		this.sourceName = sourceName;
		
		try
		{
			return readJson();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public T fromJson(String sourceName,JsonObject obj)
	{
		this.obj = obj;
		this.sourceName = sourceName;
		
		try
		{
			return readJson();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public abstract T readJson() throws Exception;
	
	@SuppressWarnings("unchecked")
	public T fromJson(File file,Object... args)
	{
		this.obj = JUtils.readToObject(file.toPath());
		this.sourceName = file.getName() != null ? file.getName() : "unknown";
		
		try
		{
			if(!(this instanceof JReaderWithArgs)) { throw new UnsupportedOperationException("Cannot cast to JReaderWithArgs as the Reader used has no implementation for this!"); }
			
			return ((JReaderWithArgs<T>) this).readJson(args);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public T fromJson(String sourceName,String json,Object... args)
	{
		this.obj = JUtils.toJsonObject(json);
		this.sourceName = sourceName;
		
		try
		{
			if(!(this instanceof JReaderWithArgs)) { throw new UnsupportedOperationException("Cannot cast to JReaderWithArgs as the Reader used has no implementation for this!"); }
			
			return ((JReaderWithArgs<T>) this).readJson(args);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public T fromJson(String sourceName,JsonObject obj,Object... args)
	{
		this.obj = obj;
		this.sourceName = sourceName;
		
		try
		{
			if(!(this instanceof JReaderWithArgs)) { throw new UnsupportedOperationException("Cannot cast to JReaderWithArgs as the Reader used has no implementation for this!"); }
			
			return ((JReaderWithArgs<T>) this).readJson(args);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public JReader<T> assertHas(String propertyName) throws Exception
	{
		return this.assertHas(propertyName,this.obj);
	}
	
	public JReader<T> assertHas(String propertyName,JsonObject obj) throws Exception
	{
		if(!obj.has(propertyName)) { throw new MissingJsonAttributeException(this.getClass().getSuperclass().getSimpleName(),this.sourceName,propertyName); }
		return this;
	}
	
	public void assertType(String propertyName,JsonDataType type) throws Exception
	{
		this.assertType(propertyName,this.obj,type);
	}
	
	public void assertType(String propertyName,JsonObject obj,JsonDataType type) throws Exception
	{		
		switch(type)
		{
			case BOOLEAN:
			{
				try { Boolean.parseBoolean(obj.get(propertyName).getAsString()); } catch(Exception e) { throw new InvalidJsonAttributeDataTypeException(this.getClass().getSuperclass().getSimpleName(),this.sourceName,propertyName,type); }
				break;
			}
			case BYTE:
			{
				try { obj.get(propertyName).getAsByte(); } catch(Exception e) { throw new InvalidJsonAttributeDataTypeException(this.getClass().getSuperclass().getSimpleName(),this.sourceName,propertyName,type); }
				break;
			}
			case DOUBLE:
			{
				try { obj.get(propertyName).getAsDouble(); } catch(Exception e) { throw new InvalidJsonAttributeDataTypeException(this.getClass().getSuperclass().getSimpleName(),this.sourceName,propertyName,type); }
				break;
			}
			case FLOAT:
			{
				try { obj.get(propertyName).getAsFloat(); } catch(Exception e) { throw new InvalidJsonAttributeDataTypeException(this.getClass().getSuperclass().getSimpleName(),this.sourceName,propertyName,type); }
				break;
			}
			case INTEGER:
			{
				try
				{
					if(!obj.get(propertyName).getAsString().contains("."))
					{
						obj.get(propertyName).getAsInt();
					}
					else
					{
						throw new NumberFormatException();
					}					
				} catch(Exception e) { throw new InvalidJsonAttributeDataTypeException(this.getClass().getSuperclass().getSimpleName(),this.sourceName,propertyName,type); }
				break;
			}
			case LONG:
			{
				try
				{
					if(!obj.get(propertyName).getAsString().contains("."))
					{
						obj.get(propertyName).getAsLong();
					}
					else
					{
						throw new NumberFormatException();
					}					
				} catch(Exception e) { throw new InvalidJsonAttributeDataTypeException(this.getClass().getSuperclass().getSimpleName(),this.sourceName,propertyName,type); }
				break;
			}
			case SHORT:
			{
				try
				{
					if(!obj.get(propertyName).getAsString().contains("."))
					{
						obj.get(propertyName).getAsShort();
					}
					else
					{
						throw new NumberFormatException();
					}					
				} catch(Exception e) { throw new InvalidJsonAttributeDataTypeException(this.getClass().getSuperclass().getSimpleName(),this.sourceName,propertyName,type); }
				break;
			}
			case STRING:
			{
				try { obj.get(propertyName).getAsString(); } catch(Exception e) { throw new InvalidJsonAttributeDataTypeException(this.getClass().getSuperclass().getSimpleName(),this.sourceName,propertyName,type); }
				break;
			}
			case JSON_ARRAY:
			{
				try { obj.get(propertyName).getAsJsonArray(); } catch(Exception e) { throw new InvalidJsonAttributeDataTypeException(this.getClass().getSuperclass().getSimpleName(),this.sourceName,propertyName,type); }
				break;
			}
			case JSON_OBJECT:
			{
				try { obj.get(propertyName).getAsJsonObject(); } catch(Exception e) { throw new InvalidJsonAttributeDataTypeException(this.getClass().getSuperclass().getSimpleName(),this.sourceName,propertyName,type); }
				break;
			}
			default:
			{
				throw new InvalidJsonAttributeDataTypeException(this.getClass().getSuperclass().getSimpleName(),this.sourceName,propertyName,type);
			}
		}
	}
	
	public void assertEnum(String propertyName,Class<?> enumClass) throws Exception
	{
		this.assertEnum(propertyName,this.obj,enumClass);
	}
	
	public void assertEnum(String propertyName,JsonObject obj,Class<?> enumClass) throws Exception
	{
		try { obj.get(propertyName).getAsString(); } catch(Exception e) { throw new InvalidJsonAttributeDataTypeException(this.getClass().getSuperclass().getSimpleName(),this.sourceName,propertyName,enumClass); }
		String enumString = obj.get(propertyName).getAsString();
		
		for(Object o : enumClass.getEnumConstants())
		{
			if(o.toString().equals(enumString)) { return; }
		}
		
		throw new InvalidJsonAttributeDataTypeException(this.getClass().getSuperclass().getSimpleName(),this.sourceName,propertyName,enumClass);
	}
	
	public boolean has(String propertyName)
	{
		return obj.has(propertyName);
	}
	
	public boolean isArray(String propertyName)
	{
		if(!this.has(propertyName)) { return false; }
		return this.obj.get(propertyName).isJsonArray();
	}
	
	public boolean isObject(String propertyName)
	{
		if(!this.has(propertyName)) { return false; }
		return this.obj.get(propertyName).isJsonObject();
	}
	
	public boolean isPrimitive(String propertyName)
	{
		if(!this.has(propertyName)) { return false; }
		return this.obj.get(propertyName).isJsonPrimitive();
	}
	
	public JsonElement get(String propertyName)
	{
		return this.obj.get(propertyName);
	}

	public String getSourceName()
	{
		return sourceName;
	}
}