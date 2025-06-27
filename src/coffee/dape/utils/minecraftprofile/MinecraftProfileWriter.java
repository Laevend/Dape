package coffee.dape.utils.minecraftprofile;

import java.util.Base64;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import coffee.dape.utils.json.JUtils;
import coffee.dape.utils.json.JWriter;
import coffee.dape.utils.minecraftprofile.data.MinecraftProfile;

/**
 * @author Laeven
 */
public class MinecraftProfileWriter extends JWriter<MinecraftProfile>
{	
	@Override
	public void writeJson() throws Exception
	{
		writeId();
		writeName();
		writeProperties();
	}
	
	private void writeId()
	{
		addProperty(MinecraftProfileReader.ID,this.object.getId());
	}
	
	private void writeName()
	{
		addProperty(MinecraftProfileReader.NAME,this.object.getName());
	}
	
	private void writeProperties()
	{
		JsonArray properties = new JsonArray();
		JsonObject textures = new JsonObject();
		
		textures.addProperty(MinecraftProfileReader.NAME,"textures");
		
		JsonObject base64Textures = new JsonObject();
		
		base64Textures.addProperty(MinecraftProfileReader.TIMESTAMP,this.object.getTexturesProperty().getTimestamp());
		base64Textures.addProperty(MinecraftProfileReader.PROFILE_ID,this.object.getTexturesProperty().getProfileId());
		base64Textures.addProperty(MinecraftProfileReader.PROFILE_NAME,this.object.getTexturesProperty().getProfileName());
		
		if(this.object.getTexturesProperty().isSignatureRequired())
		{
			base64Textures.addProperty(MinecraftProfileReader.SIGNATURE_REQUIRED,true);
		}
		
		JsonObject tex = new JsonObject();
		JsonObject skin;
		JsonObject cape;
		
		if(this.object.getTexturesProperty().getSkinURL() != null)
		{
			skin = new JsonObject();
			skin.addProperty(MinecraftProfileReader.URL,this.object.getTexturesProperty().getSkinURL());
			
			if(this.object.getTexturesProperty().isSkinSlim())
			{
				JsonObject isSlim = new JsonObject();
				isSlim.addProperty(MinecraftProfileReader.MODEL,"slim");
				skin.add(MinecraftProfileReader.METADATA,isSlim);
			}
			
			tex.add(MinecraftProfileReader.SKIN,skin);
		}
		
		if(this.object.getTexturesProperty().getCapeURL() != null)
		{
			cape = new JsonObject();
			cape.addProperty(MinecraftProfileReader.URL,this.object.getTexturesProperty().getCapeURL());			
			tex.add(MinecraftProfileReader.CAPE,cape);
		}
		
		base64Textures.add(MinecraftProfileReader.TEXTURES,tex);
		
		String json = JUtils.toJsonString(base64Textures,false);
		String base64Json = Base64.getEncoder().encodeToString(json.getBytes());
		
		textures.addProperty(MinecraftProfileReader.VALUE,base64Json);
		
		if(this.object.getTexturesProperty().getSignature() != null)
		{
			addProperty(MinecraftProfileReader.SIGNATURE,this.object.getTexturesProperty().getSignature());
		}
		
		properties.add(textures);
		add(MinecraftProfileReader.PROPERTIES,properties);
	}
}