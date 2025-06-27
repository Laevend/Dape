package coffee.dape.utils.minecraftprofile;

import java.util.Base64;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import coffee.dape.utils.json.JReader;
import coffee.dape.utils.json.JUtils;
import coffee.dape.utils.json.JsonDataType;
import coffee.dape.utils.minecraftprofile.data.MinecraftProfile;

/**
 * @author Laeven
 * @since 1.0.0
 */
public class MinecraftProfileReader extends JReader<MinecraftProfile>
{
	private MinecraftProfile player;
	
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String PROPERTIES = "properties";
	
	public static final String VALUE = "value";
	public static final String SIGNATURE = "signature";
	
	public static final String TIMESTAMP = "timestamp";
	public static final String PROFILE_ID = "profileId";
	public static final String PROFILE_NAME = "profileName";
	public static final String SIGNATURE_REQUIRED = "signatureRequired";
	public static final String TEXTURES = "textures";
	
	public static final String SKIN = "SKIN";
	public static final String URL = "url";
	public static final String METADATA = "metadata";
	public static final String MODEL = "model";
	
	public static final String CAPE = "CAPE";
	
	@Override
	public MinecraftProfile readJson() throws Exception
	{
		assertHas(ID).assertType(ID,JsonDataType.STRING);
		assertHas(NAME).assertType(NAME,JsonDataType.STRING);
		assertHas(PROPERTIES).assertType(PROPERTIES,JsonDataType.JSON_ARRAY);
		
		this.player = new MinecraftProfile();
		
		readId();
		readName();
		readProperties();
		
		return player;
	}
	
	private void readId() throws Exception
	{		
		player.setId(get(ID).getAsString());
	}
	
	private void readName() throws Exception
	{
		player.setName(get(NAME).getAsString());
	}
	
	private void readProperties() throws Exception
	{
		JsonArray arr = get(PROPERTIES).getAsJsonArray();
		JsonObject obj1 = arr.get(0).getAsJsonObject();
		
		assertHas(NAME,obj1).assertType(NAME,obj1,JsonDataType.STRING);
		player.getTexturesProperty().setName(obj1.get(NAME).getAsString());
		
		assertHas(VALUE,obj1).assertType(VALUE,obj1,JsonDataType.STRING);
		
		String valueJson = new String(Base64.getDecoder().decode(obj1.get(VALUE).getAsString()));
		JsonObject base64Obj = JUtils.toJsonObject(valueJson);
		
		if(obj1.has(SIGNATURE))
		{
			assertType(SIGNATURE,obj1,JsonDataType.STRING);
			player.getTexturesProperty().setSignature(obj1.get(SIGNATURE).getAsString());
		}
		
		assertHas(TIMESTAMP,base64Obj).assertType(TIMESTAMP,base64Obj,JsonDataType.STRING);
		assertHas(PROFILE_ID,base64Obj).assertType(PROFILE_ID,base64Obj,JsonDataType.STRING);
		assertHas(PROFILE_NAME,base64Obj).assertType(PROFILE_NAME,base64Obj,JsonDataType.STRING);
		
		player.getTexturesProperty().setTimestamp(base64Obj.get(TIMESTAMP).getAsLong());
		player.getTexturesProperty().setProfileId(base64Obj.get(PROFILE_ID).getAsString());
		player.getTexturesProperty().setProfileName(base64Obj.get(PROFILE_NAME).getAsString());
		
		if(base64Obj.has(SIGNATURE_REQUIRED))
		{
			assertType(SIGNATURE_REQUIRED,base64Obj,JsonDataType.STRING);
			player.getTexturesProperty().setSignatureRequired(base64Obj.get(SIGNATURE_REQUIRED).getAsBoolean());
		}
		
		if(base64Obj.has(TEXTURES))
		{
			JsonObject textures = base64Obj.get(TEXTURES).getAsJsonObject();
			
			if(textures.has(SKIN))
			{
				JsonObject skin = textures.get(SKIN).getAsJsonObject();
				assertHas(URL,skin).assertType(URL,skin,JsonDataType.STRING);
				player.getTexturesProperty().setSkinURL(skin.get(URL).getAsString());
				
				if(skin.has(METADATA))
				{
					JsonObject metadata = skin.get(METADATA).getAsJsonObject();
					
					assertHas(MODEL,metadata).assertType(MODEL,metadata,JsonDataType.STRING);
					player.getTexturesProperty().setSkinSlim(true);
				}
			}
			
			if(textures.has(CAPE))
			{
				JsonObject cape = textures.get(CAPE).getAsJsonObject();
				assertHas(URL,cape).assertType(URL,cape,JsonDataType.STRING);
				player.getTexturesProperty().setCapeURL(cape.get(URL).getAsString());
			}
		}
	}
}
