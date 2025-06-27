package coffee.dape.utils.json;

import java.nio.file.Path;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import coffee.dape.utils.FUtils;
import coffee.dape.utils.Logg;

/**
 * 
 * @author Laeven
 * JUtils or JsonUtils for convenient Json reading and writing functions
 *
 */
public class JUtils
{
	private static GsonBuilder builder;
	private static Gson gson;
	private static Gson gsonPretty;
	
	static
	{
		builder = new GsonBuilder();
		builder.excludeFieldsWithoutExposeAnnotation();
		gson = builder.create();
		
		builder.setPrettyPrinting();
		gsonPretty = builder.create();
	}
	
	/**
	 * Writes a JsonObject to disk
	 * @param path Path to save object to a .json file
	 * @param json Json string to save
	 * @param prettyPrint If Gson should use pretty print
	 */
	public static void write(Path path,String json,boolean prettyPrint)
	{
		Objects.requireNonNull(path,"Path cannot be null!");
		Objects.requireNonNull(json,"Json string to save cannot be null!");
		Objects.requireNonNull(prettyPrint,"Pretty print cannot be null!");
		
		JsonElement element;
		
		try
		{
			element = JsonParser.parseString(json);
			
			if(!element.isJsonObject())
			{
				throw new JsonSyntaxException("Element is not of JsonObject type!");
			}
		}
		catch(JsonSyntaxException e)
		{
			Logg.error("Gson could not parse json '" + json + "'",e);
			return;
		}
		
		write(path,element.getAsJsonObject(),prettyPrint);
	}
	
	/**
	 * Parses a Json string into a JsonObject
	 * @param json Json string to save
	 * @return 
	 */
	public static JsonObject toJsonObject(String json)
	{
		Objects.requireNonNull(json,"Json string to save cannot be null!");
		
		JsonElement element;
		
		try
		{
			element = JsonParser.parseString(json);
			
			if(!element.isJsonObject())
			{
				throw new JsonSyntaxException("Element is not of JsonObject type!");
			}
		}
		catch(JsonSyntaxException e)
		{
			Logg.error("Gson could not parse json '" + json + "'",e);
			return null;
		}
		
		return element.getAsJsonObject();
	}
	
	/**
	 * Writes a JsonObject to disk
	 * @param path Path to save object to a .json file
	 * @param obj Object to save
	 * @param prettyPrint If Gson should use pretty print
	 */
	public static void write(Path path,JsonObject obj,boolean prettyPrint)
	{
		Objects.requireNonNull(path,"Path cannot be null!");
		Objects.requireNonNull(obj,"JsonObject to save cannot be null!");
		Objects.requireNonNull(prettyPrint,"Pretty print cannot be null!");
		
		String json;
		
		if(prettyPrint)
		{
			json = gsonPretty.toJson(obj);
		}
		else
		{
			json = gson.toJson(obj);
		}
		
		if(!FUtils.write(path,json.getBytes()))
		{
			Logg.error("Could not save Json to " + path.toAbsolutePath().toString());
		}
	}
	
	/**
	 * Writes a JsonObject to Json string
	 * @param obj Object to write
	 * @param prettyPrint If Gson should use pretty print
	 * @return 
	 */
	public static String toJsonString(JsonObject obj,boolean prettyPrint)
	{
		Objects.requireNonNull(obj,"JsonObject to save cannot be null!");
		Objects.requireNonNull(prettyPrint,"Pretty print cannot be null!");
		
		String json;
		
		if(prettyPrint)
		{
			json = gsonPretty.toJson(obj);
		}
		else
		{
			json = gson.toJson(obj);
		}
		
		return json;
	}
	
	/**
	 * Reads a .json file from disk
	 * @param path Path to json file
	 * @return JsonObject or null if able to read json or parse json
	 */
	public static JsonObject readToObject(Path path)
	{
		Objects.requireNonNull(path,"Path cannot be null!");
		
		String data = readToString(path);
		
		if(data == null) { return null; }
		
		try
		{
			JsonElement element = JsonParser.parseString(data);
			return element.getAsJsonObject();
		}
		catch(JsonSyntaxException e)
		{
			Logg.error("Gson could not parse json at file " + path.toAbsolutePath().toString(),e);
		}
		
		return null;
	}
	
	/**
	 * Reads a .json file from disk
	 * @param path Path to json file
	 * @return Json string or null if able to read json or parse json
	 */
	public static String readToString(Path path)
	{
		Objects.requireNonNull(path,"Path cannot be null!");
		
		byte[] data = FUtils.read(path);
		
		if(data == null)
		{
			Logg.error("Could not read Json from disk!");
			return null;
		}
		
		return new String(data);
	}
}