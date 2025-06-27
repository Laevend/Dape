package coffee.dape.utils.json;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import coffee.dape.utils.FUtils;
import coffee.dape.utils.Logg;

/**
 * 
 * @author Laeven
 *
 */
public class JsonUtils2
{
	private static GsonBuilder builder;
	private static Gson gson;
	
	static
	{
		builder = new GsonBuilder();
		builder.excludeFieldsWithoutExposeAnnotation();
		builder.setPrettyPrinting();
		builder.registerTypeAdapterFactory(new PostProcessingEnabler());
		gson = builder.create();
	}
	
	/**
	 * Writes an object to Json
	 * @param obj Instanced Object
	 * @return Gson String
	 */
	public static String toJsonString(Object obj)
	{		
		return gson.toJson(obj);
	}
	
	/**
	 * Converts an object to Json which is then written to disk
	 * @param path the directory to save the Json file
	 * @param obj Instanced Object
	 */
	public static void toJsonFile(Path path,Object obj)
	{
        if (obj instanceof PreSerialise)
        {
            ((PreSerialise)obj).preSerialise();
        }
		
		String gsonObjString = gson.toJson(obj);
		
		Logg.verb("path " + path.toString(),Logg.VerbGroup.JSON_UTILS);
		Logg.verb("parent file " + path.getParent().toString(),Logg.VerbGroup.JSON_UTILS);
		
		// Check the directory exists
		FUtils.createDirectoriesForFile(path);
		
		try
		{
    		// Create a new buffered writer
			BufferedWriter bw = new BufferedWriter(new FileWriter(path.toFile()));
			
			// Write the object to the file
			bw.write(gsonObjString);
			bw.flush();
			bw.close();
		} 
		catch (Exception e)
		{
			Logg.error("JsonUtility could not write Json to disk!",e);
		}
	}
	
	/**
	 * Writes a JsonObject to disk
	 * @param path the directory to save the Json file
	 * @param obj Instanced JsonObject
	 * @return true if data was saved correctly
	 */
	public static boolean toJsonFile(Path path,JsonObject obj)
	{
		Logg.verb("path " + path.toString(),Logg.VerbGroup.JSON_UTILS);
		Logg.verb("parent file " + path.getParent().toString(),Logg.VerbGroup.JSON_UTILS);
		
		// Check the directory exists
		FUtils.createDirectoriesForFile(path);

		try
		{
    		// Create a new buffered writer
			BufferedWriter bw = new BufferedWriter(new FileWriter(path.toFile()));
			
			// Write the object to the file
			bw.write(gson.toJson(obj));
			bw.flush();
			bw.close();
			
			return true;
		} 
		catch (IOException e)
		{
			Logg.error("JsonUtility could not write JsonObject to disk!",e);
			return false;
		}
	}
	
	/**
	 * Creates an object from a Json string
	 * @param <T> Object type
	 * @param stringObj Json string
	 * @param class_ The class used to create the Json string
	 * @return Instanced Object
	 */
	public static <T> Object fromJson(String stringObj,Class<T> class_)
	{
		return gson.fromJson(stringObj,class_);
	}
	
	/**
	 * Creates an object from a Json string from disk
	 * @param <T> Object type
	 * @param path the directory where the Json file is saved
	 * @param class_ The class used to create the Json string
	 * @return Instanced Object
	 */
	public static <T> Object fromJsonFile(String path,Class<T> class_)
	{
		File file = new File(path);
		
		// Check the directory exists
		if(!file.exists())
	    {
			Logg.error("JsonUtility could not read Json from disk because the file doesn't exist!");
			return null;
	    }
		
		try
		{
			FileReader fr = new FileReader(file);
			Object obj = gson.fromJson(fr,class_);
			
			fr.close();
			
			return obj;
		} 
		catch (Exception e)
		{
			Logg.error("JsonUtility could not read Json from disk!",e);
		}
		
		return null;
	}
	
	/**
	 * Creates a JsonObject from a Json string from disk
	 * @param path the directory where the Json file is saved
	 * @return Instanced JsonObject
	 */
	public static JsonObject fromJsonFile(String path)
	{
		File file = new File(path);
		JsonObject jObj = new JsonObject();
		
		// Check the directory exists
		if(!file.exists())
	    {
			return new JsonObject();
	    }
		
		try(FileReader fr = new FileReader(file))
		{
            jObj = JsonParser.parseReader(fr).getAsJsonObject();			
			return jObj;
		} 
		catch (Exception e)
		{
			Logg.error("JsonUtility could not read Json from disk!",e);
		}
		
		return null;
	}
	
	/**
	 * Creates a JsonObject from a Json string
	 * @param jsonString String of Json
	 * @return Instanced JsonObject
	 */
	public static JsonObject fromJsonString(String jsonString)
	{
		JsonObject jObj = new JsonObject();
		
		try
		{
            JsonElement jsonElement = JsonParser.parseString(jsonString);
            jObj = jsonElement.getAsJsonObject();			
			return jObj;
		} 
		catch (Exception e)
		{
			Logg.error("JsonUtility could not parse Json string!",e);
		}
		
		return null;
	}
}