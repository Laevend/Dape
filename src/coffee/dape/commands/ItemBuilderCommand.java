package coffee.dape.commands;

import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.DecimalFormat;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;

import coffee.dape.cmdparsers.astral.annos.CommandEx;
import coffee.dape.cmdparsers.astral.annos.CmdPath;
import coffee.dape.cmdparsers.astral.annos.VMap;
import coffee.dape.cmdparsers.astral.parser.ArgSet;
import coffee.dape.cmdparsers.astral.parser.AstralExecutor;
import coffee.dape.cmdparsers.astral.parser.CommandParser.CmdSender;
import coffee.dape.cmdparsers.astral.types.ArgTypes;
import coffee.dape.exception.MissingAnnotationException;
import coffee.dape.utils.DelayUtils;
import coffee.dape.utils.ImageUtils;
import coffee.dape.utils.MapUtils;
import coffee.dape.utils.MathUtils;
import coffee.dape.utils.PrintUtils;


/**
 * @author Laeven
 */
@CommandEx(name = "itembuilder",alias = {"ib"},description = "A command for building item stacks")
public final class ItemBuilderCommand extends AstralExecutor
{
	private DecimalFormat scaleFormat = new DecimalFormat("#.###");
	
	public ItemBuilderCommand() throws MissingAnnotationException
	{
		super(ItemBuilderCommand.class);
		
		addPath("setScale",CmdSender.PLAYER,new ArgSet().of("scale").of("set").of("<scale>",ArgTypes.DOUBLE).mapTo("scale"));
		
		addPath("resetScale",CmdSender.PLAYER,new ArgSet().of("scale").of("reset"));
		
		addPath("imageToMap",CmdSender.PLAYER,new ArgSet().of("image-to-map").of("<url>",ArgTypes.STRING).mapTo("url"));
		
		addPath("imageToMapResize",CmdSender.PLAYER,new ArgSet().of("image-to-map").of("<url>",ArgTypes.STRING).mapTo("url").of("resize-to-map"));
		
		addPath("jump",CmdSender.PLAYER,new ArgSet().of("jump"));
		
		addPath("speed",CmdSender.PLAYER,new ArgSet().of("speed").of("<speed>",ArgTypes.INT).mapTo("speed"));
	}
	
	@CmdPath(name = "setScale",description = "Sets the players scale attribute",syntax = "/utils scale set <scale>",usage = "/utils scale set 5.57")
	public void setScale(Player p,@VMap("scale") double scale)
	{
		// According to wiki, GENERIC_SCALE min-max values are as below
		double clampedScale = MathUtils.clamp(0.0625d,16d,scale);
		p.getAttribute(Attribute.SCALE).setBaseValue(scale);
		PrintUtils.info(p,"Scale changed to " + scaleFormat.format(clampedScale));
	}
	
	@CmdPath(name = "resetScale",description = "Resets the players scale",syntax = "/utils scale reset",usage = "/utils scale reset")
	public void resetScale(Player p)
	{
		// According to wiki, GENERIC_SCALE min-max values are as below
		p.getAttribute(Attribute.SCALE).setBaseValue(1d);
		PrintUtils.info(p,"Scale changed to " + scaleFormat.format(1d));
	}
	
	@CmdPath(name = "imageToMap",description = "Applies an image to a map",syntax = "/utils image-to-map <url>",usage = "/utils image-to-map https://www.SomeRandomImageWebsite.com/image.png")
	public void imageToMap(Player p,@VMap("url") String url)
	{
		PrintUtils.info(p,"Requesting image...");
		
		DelayUtils.submitAsync("Getting Map via URL Clock",20,() ->
		{
			try
			{
				URL imageUrl = URI.create(url).toURL();
				PrintUtils.info(p,"Getting Image...");
				BufferedImage image = ImageUtils.getImageFromURL(imageUrl);
				PrintUtils.info(p,"Creating map...");
				ItemStack stack = MapUtils.imageToMap(image);
				
				DelayUtils.executeDelayedTask(() ->
				{
					p.getInventory().addItem(stack);
					PrintUtils.success(p,"Image Loaded");
				});
			}
			catch (MalformedURLException e)
			{
				PrintUtils.error(p,"Error occured fetching image!");
				e.printStackTrace();
			}
		},null);
	}
	
	@CmdPath(name = "imageToMapResize",description = "Applies an image to a map and resizes it to fit",syntax = "/utils image-to-map <url> resize-to-map",usage = "/utils image-to-map https://www.SomeRandomImageWebsite.com/image.png resize-to-map")
	public void imageToMapResize(Player p,@VMap("url") String url)
	{
		PrintUtils.info(p,"Requesting image...");
		
		DelayUtils.submitAsync("Getting Map via URL Clock",20,() ->
		{
			try
			{
				URL imageUrl = URI.create(url).toURL();
				PrintUtils.info(p,"Getting Image...");
				BufferedImage image = ImageUtils.getImageFromURL(imageUrl);
				PrintUtils.info(p,"Resizing...");
				image = ImageUtils.resize(image,128,128);
				PrintUtils.info(p,"Creating map...");
				ItemStack stack = MapUtils.imageToMap(image);
				
				DelayUtils.executeDelayedTask(() ->
				{
					p.getInventory().addItem(stack);
					PrintUtils.success(p,"Image Loaded");
				});
			}
			catch (MalformedURLException e)
			{
				PrintUtils.error(p,"Error occured fetching image!");
				e.printStackTrace();
			}
		},null);
	}
	
	@CmdPath(name = "jump",description = "Jump to the block where you're looking",syntax = "/utils jump",usage = "/utils jump")
	public void jump(Player p)
	{
		Location eyeLoc = p.getEyeLocation();
		RayTraceResult result = p.getLocation().getWorld().rayTraceBlocks(eyeLoc,eyeLoc.getDirection(),300,FluidCollisionMode.NEVER);
		
		if(result == null) { PrintUtils.error(p,"Block is too far away!"); return; }
		if(result.getHitBlockFace() == null) { PrintUtils.error(p,"Block is too far away!"); return; }
		
		Location teleportLoc = result.getHitBlock().getLocation().add(0,1,0);
		teleportLoc.setDirection(eyeLoc.getDirection());
		
		p.teleport(teleportLoc);
	}
	
	@CmdPath(name = "speed",description = "Sets the players speed.",syntax = "/utils speed <speed>",usage = "/utils speed 2")
	public void setSpeed(Player p,@VMap("speed") int speed)
	{
		int newSpeed = MathUtils.clamp(0,20,speed);
		float defaultWalkSpeed = 0.2f;
		float defaultFlySpeed = 0.1f;
		
		if(newSpeed == 1)
		{
			p.setWalkSpeed(defaultWalkSpeed);
			p.setFlySpeed(defaultFlySpeed);
		}
		else
		{
			p.setWalkSpeed(newSpeed * 0.1f);
			p.setFlySpeed(newSpeed * 0.1f);
		}
		
		PrintUtils.info(p,"Speed set to " + newSpeed);
	}
}