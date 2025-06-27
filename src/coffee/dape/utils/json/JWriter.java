package coffee.dape.utils.json;

import java.nio.file.Path;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import coffee.dape.utils.Logg;

/**
 * 
 * @author Laeven
 *
 */
public abstract class JWriter<T>
{	
	// Source name can be custom or the filename
	private String sourceName;
	private JsonObject obj;
	public T object;
	
	public void toJson(T object,Path path)
	{
		this.obj = new JsonObject();
		this.sourceName = object.getClass().getSimpleName() != null ? object.getClass().getSimpleName() : "unknown";
		this.object = object;
		
		try
		{
			writeJson();
			
			JUtils.write(path,obj,true);
		}
		catch(Exception e)
		{
			Logg.fatal("Json Writer '" + this.getClass().getSuperclass().getSimpleName() + "' attempting to save '" + this.sourceName + "' failed.",e);
		}
	}
	
	public JsonObject toJson(T object)
	{
		this.obj = new JsonObject();
		this.sourceName = object.getClass().getSimpleName() != null ? object.getClass().getSimpleName() : "unknown";
		this.object = object;
		
		try
		{
			writeJson();
			
			return obj;
		}
		catch(Exception e)
		{
			Logg.fatal("Json Writer '" + this.getClass().getSuperclass().getSimpleName() + "' attempting to save '" + this.sourceName + "' failed.",e);
		}
		
		return null;
	}
	
	public JsonObject toJson(T object,JsonObject jsonObj)
	{
		this.obj = jsonObj;
		this.sourceName = object.getClass().getSimpleName() != null ? object.getClass().getSimpleName() : "unknown";
		this.object = object;
		
		try
		{
			writeJson();
			
			return obj;
		}
		catch(Exception e)
		{
			Logg.fatal("Json Writer '" + this.getClass().getSuperclass().getSimpleName() + "' attempting to save '" + this.sourceName + "' failed.",e);
		}
		
		return null;
	}
	
	public abstract void writeJson() throws Exception;
	
	public void addProperty(String propertyName,boolean bool)
	{
		this.obj.addProperty(propertyName,bool);
	}
	
	public void addProperty(String propertyName,String string)
	{
		this.obj.addProperty(propertyName,string);
	}
	
	public void addProperty(String propertyName,Number num)
	{
		this.obj.addProperty(propertyName,num);
	}
	
	public void add(String propertyName,JsonElement element)
	{
		this.obj.add(propertyName,element);
	}
	
	public T getObj()
	{
		return object;
	}

	public String getSourceName()
	{
		return sourceName;
	}
}