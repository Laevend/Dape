package coffee.dape.utils.json;

import com.google.gson.JsonObject;

import coffee.dape.exception.DeserialiseException;
import coffee.dape.exception.SerialiseException;

public interface PersistJson
{
	public JsonObject serialise() throws SerialiseException;
	
	public void deserialise(JsonObject obj) throws DeserialiseException;
}
