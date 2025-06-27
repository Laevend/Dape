package coffee.dape.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import coffee.dape.Dape;
import coffee.dape.cmdparsers.astral.annos.CommandEx;
import coffee.dape.cmdparsers.astral.annos.CmdPath;
import coffee.dape.cmdparsers.astral.annos.VMap;
import coffee.dape.cmdparsers.astral.parser.ArgSet;
import coffee.dape.cmdparsers.astral.parser.AstralExecutor;
import coffee.dape.cmdparsers.astral.parser.CommandParser.CmdSender;
import coffee.dape.cmdparsers.astral.suggestions.Suggestions;
import coffee.dape.cmdparsers.astral.types.ArgTypes;
import coffee.dape.exception.MissingAnnotationException;
import coffee.dape.utils.Logg;
import coffee.dape.utils.PrintUtils;
import coffee.dape.utils.structs.Namespace;


/**
 * @author Laeven
 */
@CommandEx(name = "logger",description = "A command for configuring the logger")
public class LoggerCommand extends AstralExecutor
{
	public LoggerCommand() throws MissingAnnotationException
	{
		super(LoggerCommand.class);
		
		addPath("toggle all verbose messages",CmdSender.ANY,new ArgSet().of("toggle").of("verbose").of("all"));
		
		addPath("toggle verbose messages by group",CmdSender.ANY,new ArgSet().of("toggle").of("verbose").of("by-group").of("<group>",ArgTypes.NAMESPACE,Suggestions.loggingVerboseGroups()).mapTo("namespace"));
		
		addPath("toggle all warning messages",CmdSender.ANY,new ArgSet().of("toggle").of("warnings"));
		
		addPath("toggle all error messages",CmdSender.ANY,new ArgSet().of("toggle").of("errors"));
		
		addPath("toggle all fatal messages",CmdSender.ANY,new ArgSet().of("toggle").of("fatals"));
		
		addPath("toggle all exception messages",CmdSender.ANY,new ArgSet().of("toggle").of("exceptions"));
		
		addPath("toggle writing exceptions to disk",CmdSender.ANY,new ArgSet().of("toggle").of("write-exceptions"));
	}
	
	@CmdPath(name = "toggle all verbose messages",description = "Toggles all verbose messages on/off",syntax = "/logger toggle verbose all",usage = "/logger toggle verbose all")
	public void toggleAllVerbose(CommandSender sender)
	{
		if(Logg.isHideVerbose())
		{
			Dape.getConfigFile().set("logger.hide_verbose",false);
			Logg.setHideErrors(false);
			PrintUtils.info(sender,"Error messages are now shown.");
		}
		else
		{
			Dape.getConfigFile().set("logger.hide_verbose",true);
			Logg.setHideErrors(true);
			PrintUtils.info(sender,"Error messages are now hidden.");
		}
		
		Dape.getConfigFile().saveConfig();
	}
	
	@CmdPath(name = "toggle verbose messages by group",description = "Toggles groups of verbose messages on/off",syntax = "/logger toggle ",usage = "/help hop mellow")
	public void toggleVerboseByGroup(CommandSender sender,@VMap("namespace") Namespace namespace)
	{
		List<String> enabledVerboseGroups = (List<String>) Dape.getConfigFile().getStringList("logger.verbose.enabled_groups");
		if(enabledVerboseGroups == null) { return; }
		
		if(enabledVerboseGroups.contains(namespace.toSimpleString()))
		{
			enabledVerboseGroups.remove(namespace.toSimpleString());
			Dape.getConfigFile().set("logger.verbose.enabled_groups",false);
			Logg.setVerboseGroupEnabled(namespace,false);
			PrintUtils.info(sender,"Verbose messages for group " + namespace.toSimpleString() + " are now hidden.");
		}
		else
		{
			enabledVerboseGroups.add(namespace.toSimpleString());
			Dape.getConfigFile().set("logger.verbose.enabled_groups",true);
			Logg.setVerboseGroupEnabled(namespace,true);
			PrintUtils.info(sender,"Verbose messages for group " + namespace.toSimpleString() + " are now shown.");
		}
	}
	
	@CmdPath(name = "toggle all warning messages",description = "Toggles warning messages on/off",syntax = "/logger toggle warnings",usage = "/logger toggle warnings")
	public void toggleWarnings(CommandSender sender)
	{
		if(Logg.isHideErrors())
		{
			Dape.getConfigFile().set("logger.hide_warnings",false);
			Logg.setHideWarnings(false);
			PrintUtils.info(sender,"Error messages are now shown.");
		}
		else
		{
			Dape.getConfigFile().set("logger.hide_warnings",true);
			Logg.setHideWarnings(true);
			PrintUtils.info(sender,"Error messages are now hidden.");
		}
		
		Dape.getConfigFile().saveConfig();
	}
	
	@CmdPath(name = "toggle all error messages",description = "Toggles error messages on/off",syntax = "/logger toggle errors",usage = "/logger toggle errors")
	public void toggleErrors(CommandSender sender)
	{
		if(Logg.isHideErrors())
		{
			Dape.getConfigFile().set("logger.hide_errors",false);
			Logg.setHideErrors(false);
			PrintUtils.info(sender,"Error messages are now shown.");
		}
		else
		{
			Dape.getConfigFile().set("logger.hide_errors",true);
			Logg.setHideErrors(true);
			PrintUtils.info(sender,"Error messages are now hidden.");
		}
		
		Dape.getConfigFile().saveConfig();
	}
	
	@CmdPath(name = "toggle all fatal messages",description = "Toggles warning messages on/off",syntax = "/logger toggle fatals",usage = "/logger toggle fatals")
	public void toggleFatals(CommandSender sender)
	{
		if(Logg.isHideErrors())
		{
			Dape.getConfigFile().set("logger.hide_fatals",false);
			Logg.setHideFatals(false);
			PrintUtils.info(sender,"Error messages are now shown.");
		}
		else
		{
			Dape.getConfigFile().set("logger.hide_fatals",true);
			Logg.setHideFatals(true);
			PrintUtils.info(sender,"Error messages are now hidden.");
		}
		
		Dape.getConfigFile().saveConfig();
	}
	
	@CmdPath(name = "toggle all exception messages",description = "Toggles warning messages on/off",syntax = "/logger toggle exceptions",usage = "/logger toggle exceptions")
	public void toggleExceptions(CommandSender sender)
	{
		if(Logg.isHideErrors())
		{
			Dape.getConfigFile().set("logger.hide_exceptions",false);
			Logg.setSilenceExceptions(false);
			PrintUtils.info(sender,"Error messages are now shown.");
		}
		else
		{
			Dape.getConfigFile().set("logger.hide_exceptions",true);
			Logg.setSilenceExceptions(true);
			PrintUtils.info(sender,"Error messages are now hidden.");
		}
		
		Dape.getConfigFile().saveConfig();
	}
	
	@CmdPath(name = "toggle writing exceptions to disk",description = "Toggles writing exceptions to disk on/off",syntax = "/logger toggle write-exceptions",usage = "/logger toggle write-exceptions")
	public void toggleWritingExceptions(CommandSender sender)
	{
		if(Logg.isHideErrors())
		{
			Dape.getConfigFile().set("logger.write_warnings",false);
			Logg.setWriteExceptions(false);
			PrintUtils.info(sender,"Error messages are now shown.");
		}
		else
		{
			Dape.getConfigFile().set("logger.write_warnings",true);
			Logg.setWriteExceptions(true);
			PrintUtils.info(sender,"Error messages are now hidden.");
		}
		
		Dape.getConfigFile().saveConfig();
	}
}