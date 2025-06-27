package coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.email;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.bukkit.entity.Player;

import com.google.gson.JsonObject;

import coffee.dape.Dape;
import coffee.dape.cmdparsers.astral.elevatedaccount.ElevatedAccountCtrl;
import coffee.dape.cmdparsers.astral.elevatedaccount.ElevatedAccountCtrl.AuthMethod;
import coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.AuthenticationMethod;
import coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.totp.TimedOTPAuthMethod;
import coffee.dape.event.ChatInputEvent;
import coffee.dape.exception.DeserialiseException;
import coffee.dape.exception.IllegalMethodCallException;
import coffee.dape.utils.ChatUtils;
import coffee.dape.utils.ChatUtils.InputHandler;
import coffee.dape.utils.Logg;
import coffee.dape.utils.PrintUtils;
import coffee.dape.utils.chat.InputListener;
import coffee.dape.utils.structs.Namespace;

public final class EmailSetup implements InputListener
{
	private EmailSetupPhase setupState = EmailSetupPhase.NONE;
	private EmailOTPAuthMethod otpMethod;
	private static Map<UUID,EmailSetup> setupInstance = new HashMap<>();
	
	public static EmailSetup getInstance(Player p)
	{
		if(setupInstance.containsKey(p.getUniqueId())) { return setupInstance.get(p.getUniqueId()); }
		setupInstance.put(p.getUniqueId(),new EmailSetup());
		return setupInstance.get(p.getUniqueId());
	}
	
	public final void startSetup(Player p)
	{
		if(this.setupState != EmailSetupPhase.NONE) { return; }
		
		this.setupState = EmailSetupPhase.ENTER_EMAIL;
		onSetup(new ChatInputEvent(p,null,null,null));
	}
	
	@InputHandler(ChatUtils.HandlerNames.ELEVATED_ACCOUNTS_AUTH_METHOD_SETUP_EMAIL_OTP)
	public static final void onChatInputStaticPinSetup(ChatInputEvent e)
	{
		getInstance(e.getPlayer()).onSetup(e);
	}
	
	/**
	 * Gets a HTML document that is sent to a user with their OTP
	 * 
	 * <p>This method is used to get a HTML document for standard OTP requesting.
	 * @param otp One time pass code to inject into the document
	 * @return HTML document with injected OTP
	 */
	private static String getOTPEmailContent(String otp)
	{
		String htmlEmailContent = "<!DOCTYPE html><html lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\"><head><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width\"><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"><meta name=\"x-apple-disable-message-reformatting\"><title>Vertex ID Login [" + otp + "]</title><link href=\"https://fonts.googleapis.com/css?family=Roboto:300,500\" rel=\"stylesheet\"><style>html,body{margin:0 auto !important;padding:0 !important;height:100% !important;width:100% !important}*{-ms-text-size-adjust:100%;-webkit-text-size-adjust:100%}div[style*=\"margin: 16px 0\"]{margin:0 !important}table,td{mso-table-lspace:0pt !important;mso-table-rspace:0pt !important}table{border-spacing:0 !important;border-collapse:collapse !important;table-layout:fixed !important;margin:0 auto !important}table table table{table-layout:auto}img{-ms-interpolation-mode:bicubic}*[x-apple-data-detectors], .x-gmail-data-detectors, .x-gmail-data-detectors *,.aBn{border-bottom:0 !important;cursor:default !important;color:inherit !important;text-decoration:none !important;font-size:inherit !important;font-family:inherit !important;font-weight:inherit !important;line-height:inherit !important}.a6S{display:none !important;opacity:0.01 !important}img.g-img+div{display:none !important}.button-link{text-decoration:none !important}@media only screen and (min-device-width: 375px) and (max-device-width: 413px){.email-container{min-width:375px !important}}</style><style>.button-td,.button-a{transition:all 100ms ease-in}.button-td:hover,.button-a:hover{background:#555 !important;border-color:#555 !important}@media screen and (max-width: 480px){.fluid{width:100% !important;max-width:100% !important;height:auto !important;margin-left:auto !important;margin-right:auto !important}.stack-column,.stack-column-center{display:block !important;width:100% !important;max-width:100% !important;direction:ltr !important}.stack-column-center{text-align:center !important}.center-on-narrow{text-align:center !important;display:block !important;margin-left:auto !important;margin-right:auto !important;float:none !important}table.center-on-narrow{display:inline-block !important}.email-container p{font-size:19px !important;line-height:27px !important}p.disclaimer{font-size:12px !important;line-height:18px !important}}</style></head><body width=\"100%\" bgcolor=\"#F1F1F1\" style=\"margin: 0; mso-line-height-rule: exactly;\"><center style=\"width: 100%; background: #F1F1F1; text-align: left;\"><div style=\"display:none;font-size:1px;line-height:1px;max-height:0px;max-width:0px;opacity:0;overflow:hidden;mso-hide:all;font-family: 'Roboto', sans-serif;\"></div><div style=\"max-width: 600px; margin: auto;\" class=\"email-container\"><table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" align=\"center\" width=\"100%\" style=\"max-width: 600px;\" class=\"email-container\"><tr><td bgcolor=\"#051036\" align=\"center\" valign=\"top\" style=\"text-align: center; background-position: center center !important; background-size: cover !important;\"><div><table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" width=\"100%\" style=\"max-width:600px; margin: auto;\"><tr><td align=\"right\" valign=\"middle\"><table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" width=\"100%\" style=\"max-width:600px; margin: auto;\"><tr><td valign=\"middle\" style=\"text-align: center; padding: 0;\"><img src=\"https://vertexmc.org/images/EmailBanner.png\" width=\"100%\" alt=\"Vertex ID\" border=\"0\" style=\"display: block; height: auto; font-family: 'Roboto', sans-serif; font-size: 15px; line-height: 20px; color: #FFF;\"></td></tr></table></td></tr></table></div></td></tr><tr><td bgcolor=\"#36393f\"><table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"100%\" align=\"center\" bgcolor=\"#36393f\"><tr><td style=\"padding: 20px 40px 20px 40px; text-align: center;\"><h1 style=\"margin: 0; font-family: 'Roboto', 'Arial', sans-serif; font-size: 32px; line-height: 40px; color: #d1d2d4; font-weight: bold; letter-spacing: 0px;\">Welcome back!</h1></td></tr><tr><td style=\"padding: 0px 40px 20px 40px; font-family: 'Roboto', sans-serif; font-size: 17px; line-height: 20px; color: #d1d2d4; text-align: center; font-weight:300;\"><p style=\"margin: 0 0 5px 0;\">Use the verification code below to log in.</p></tr><tr><td style=\"padding: 20px 40px 40px 40px; text-align: center;\" align=\"center\"><table role=\"presentation\" align=\"center\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" class=\"center-on-narrow\"><tr><td style=\"border-radius: 8px; background: #202225; text-align: center;\"><div style=\"background: #292b2f; border: 2px solid #202225; font-family: 'Roboto', sans-serif; font-size: 30px; line-height: 1.1; text-align: center; text-decoration: none; display: block; border-radius: 8px; font-weight: bold; padding: 10px 40px;\"> <span style=\"color:#d1d2d4;letter-spacing: 5px;\">" + otp + "</span></div></td></tr></table></td></tr></table></td></tr><tr><td bgcolor=\"#36393f\"><table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"100%\" bgcolor=\"#36393f\" style=\"border-top: 1px solid #292b2f;\"><tr><td style=\"padding: 30px 30px; text-align: center;font-family: 'Roboto', sans-serif; font-size: 15px; line-height: 20px;\"><table align=\"center\" style=\"text-align: center;\"><tr><td style=\"font-family: 'Roboto', sans-serif; font-size: 12px; line-height: 20px; color: #d1d2d4; text-align: center; font-weight:300;\"><p class=\"disclaimer\" style=\"margin-bottom: 5px;\">You've received this email because you requested to log in to your Vertex ID. If you didn&#x27;t request to log in, you can safely ignore this email.</p></td></tr></table></td></tr></table></td></tr><tr><td bgcolor=\"#202225\"><table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"100%\"><tr><td align=\"left\" style=\"padding: 40px 10px 40px 40px; font-family: 'Roboto', sans-serif; font-size: 14px; line-height: 20px; color: #FFFFFF; text-align: left;\"><p style=\"margin: 0 0 10px 0;\"><a href=\"http://vertexmc.org/TOS.html\" class=\"links__link\" style=\"color: #ffffff; font-size: 12px; text-decoration: none;\">Terms of Service</a></p><p style=\"margin: 0;\"><a href=\"http://vertexmc.org/privacypolicy.html\" class=\"links__link\" style=\"color: #ffffff; font-size: 12px; text-decoration: none;\">Privacy Policy</a></p></td><td align=\"right\" style=\"padding: 40px 40px 40px 10px; text-align: right;\"> <img src=\"https://vertexmc.org/images/vertexlogo.png\" alt=\"Vertex\" width=\"80\" height=\"70\" /></td></tr></table></td></tr></table></div></center></body></html>";
		return htmlEmailContent;
	}
	
	/**
	 * Gets a HTML document that is sent to a user with their email verification OTP
	 * 
	 * <p>This method is used to get a HTML document for verifying a users email.
	 * @param otp One time pass code to inject into the document
	 * @return HTML document with injected OTP
	 */
	private static String getOTPVerifyEmailContent(String otp)
	{
		String htmlEmailContent = "<!DOCTYPE html><html lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\"><head><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width\"><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"><meta name=\"x-apple-disable-message-reformatting\"><title>Vertex ID Login [" + otp + "]</title><link href=\"https://fonts.googleapis.com/css?family=Roboto:300,500\" rel=\"stylesheet\"><style>html,body{margin:0 auto !important;padding:0 !important;height:100% !important;width:100% !important}*{-ms-text-size-adjust:100%;-webkit-text-size-adjust:100%}div[style*=\"margin: 16px 0\"]{margin:0 !important}table,td{mso-table-lspace:0pt !important;mso-table-rspace:0pt !important}table{border-spacing:0 !important;border-collapse:collapse !important;table-layout:fixed !important;margin:0 auto !important}table table table{table-layout:auto}img{-ms-interpolation-mode:bicubic}*[x-apple-data-detectors], .x-gmail-data-detectors, .x-gmail-data-detectors *,.aBn{border-bottom:0 !important;cursor:default !important;color:inherit !important;text-decoration:none !important;font-size:inherit !important;font-family:inherit !important;font-weight:inherit !important;line-height:inherit !important}.a6S{display:none !important;opacity:0.01 !important}img.g-img+div{display:none !important}.button-link{text-decoration:none !important}@media only screen and (min-device-width: 375px) and (max-device-width: 413px){.email-container{min-width:375px !important}}</style><style>.button-td,.button-a{transition:all 100ms ease-in}.button-td:hover,.button-a:hover{background:#555 !important;border-color:#555 !important}@media screen and (max-width: 480px){.fluid{width:100% !important;max-width:100% !important;height:auto !important;margin-left:auto !important;margin-right:auto !important}.stack-column,.stack-column-center{display:block !important;width:100% !important;max-width:100% !important;direction:ltr !important}.stack-column-center{text-align:center !important}.center-on-narrow{text-align:center !important;display:block !important;margin-left:auto !important;margin-right:auto !important;float:none !important}table.center-on-narrow{display:inline-block !important}.email-container p{font-size:19px !important;line-height:27px !important}p.disclaimer{font-size:12px !important;line-height:18px !important}}</style></head><body width=\"100%\" bgcolor=\"#F1F1F1\" style=\"margin: 0; mso-line-height-rule: exactly;\"><center style=\"width: 100%; background: #F1F1F1; text-align: left;\"><div style=\"display:none;font-size:1px;line-height:1px;max-height:0px;max-width:0px;opacity:0;overflow:hidden;mso-hide:all;font-family: 'Roboto', sans-serif;\"></div><div style=\"max-width: 600px; margin: auto;\" class=\"email-container\"><table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" align=\"center\" width=\"100%\" style=\"max-width: 600px;\" class=\"email-container\"><tr><td bgcolor=\"#051036\" align=\"center\" valign=\"top\" style=\"text-align: center; background-position: center center !important; background-size: cover !important;\"><div><table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" width=\"100%\" style=\"max-width:600px; margin: auto;\"><tr><td align=\"right\" valign=\"middle\"><table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" width=\"100%\" style=\"max-width:600px; margin: auto;\"><tr><td valign=\"middle\" style=\"text-align: center; padding: 0;\"><img src=\"https://vertexmc.org/images/EmailBanner.png\" width=\"100%\" alt=\"Vertex ID\" border=\"0\" style=\"display: block; height: auto; font-family: 'Roboto', sans-serif; font-size: 15px; line-height: 20px; color: #FFF;\"></td></tr></table></td></tr></table></div></td></tr><tr><td bgcolor=\"#36393f\"><table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"100%\" align=\"center\" bgcolor=\"#36393f\"><tr><td style=\"padding: 20px 40px 20px 40px; text-align: center;\"><h1 style=\"margin: 0; font-family: 'Roboto', 'Arial', sans-serif; font-size: 32px; line-height: 40px; color: #d1d2d4; font-weight: bold; letter-spacing: 0px;\">Email Verification</h1></td></tr><tr><td style=\"padding: 0px 40px 20px 40px; font-family: 'Roboto', sans-serif; font-size: 17px; line-height: 20px; color: #d1d2d4; text-align: center; font-weight:300;\"><p style=\"margin: 0 0 5px 0;\">Use the verification code below to verify your email.</p></tr><tr><td style=\"padding: 20px 40px 40px 40px; text-align: center;\" align=\"center\"><table role=\"presentation\" align=\"center\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" class=\"center-on-narrow\"><tr><td style=\"border-radius: 8px; background: #202225; text-align: center;\"><div style=\"background: #292b2f; border: 2px solid #202225; font-family: 'Roboto', sans-serif; font-size: 30px; line-height: 1.1; text-align: center; text-decoration: none; display: block; border-radius: 8px; font-weight: bold; padding: 10px 40px;\"> <span style=\"color:#d1d2d4;letter-spacing: 5px;\">" + otp + "</span></div></td></tr></table></td></tr></table></td></tr><tr><td bgcolor=\"#36393f\"><table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"100%\" bgcolor=\"#36393f\" style=\"border-top: 1px solid #292b2f;\"><tr><td style=\"padding: 30px 30px; text-align: center;font-family: 'Roboto', sans-serif; font-size: 15px; line-height: 20px;\"><table align=\"center\" style=\"text-align: center;\"><tr><td style=\"font-family: 'Roboto', sans-serif; font-size: 12px; line-height: 20px; color: #d1d2d4; text-align: center; font-weight:300;\"><p class=\"disclaimer\" style=\"margin-bottom: 5px;\">You've received this email because you requested verification of your email. If you didn&#x27;t request a verification, you can safely ignore this email.</p></td></tr></table></td></tr></table></td></tr><tr><td bgcolor=\"#202225\"><table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"100%\"><tr><td align=\"left\" style=\"padding: 40px 10px 40px 40px; font-family: 'Roboto', sans-serif; font-size: 14px; line-height: 20px; color: #FFFFFF; text-align: left;\"><p style=\"margin: 0 0 10px 0;\"><a href=\"http://vertexmc.org/TOS.html\" class=\"links__link\" style=\"color: #ffffff; font-size: 12px; text-decoration: none;\">Terms of Service</a></p><p style=\"margin: 0;\"><a href=\"http://vertexmc.org/privacypolicy.html\" class=\"links__link\" style=\"color: #ffffff; font-size: 12px; text-decoration: none;\">Privacy Policy</a></p></td><td align=\"right\" style=\"padding: 40px 40px 40px 10px; text-align: right;\"> <img src=\"https://vertexmc.org/images/vertexlogo.png\" alt=\"Vertex\" width=\"80\" height=\"70\" /></td></tr></table></td></tr></table></div></center></body></html>";
		return htmlEmailContent;
	}
		
	public final void onSetup(ChatInputEvent e)
	{
		Player p = e.getPlayer();
		
		if(!ElevatedAccountCtrl.hasElevatedAccount(p)) { PrintUtils.error(e.getPlayer(),"Error! You don't have an elevated account!"); return; }
		if(setupState == null) { PrintUtils.error(e.getPlayer(),"Error! Incorrect OTP setup state!"); return; }
		
		// Gathering input from player
		switch(setupState)
		{
			case NONE ->
			{
				PrintUtils.error(e.getPlayer(),"Error! Incorrect OTP setup state!");
				return;
			}
			case ENTER_EMAIL ->
			{
				setupState = EmailSetupPhase.SEND_EMAIL_OTP;
				ChatUtils.requestInput(p,"Enter your email address to send a one-time-passcode to.",Namespace.of(Dape.getNamespaceName(),ChatUtils.HandlerNames.ELEVATED_ACCOUNTS_AUTH_METHOD_SETUP_EMAIL_OTP));
				return;
			}
			case SEND_EMAIL_OTP ->
			{
				setupState = EmailSetupPhase.TEST_OTP;

				String email = e.getInput();
				otpMethod = new EmailOTPAuthMethod(email);
				
				if(!EmailOTPAuthMethod.EMAIL_PATTERN.matcher(email).matches())
				{
					setupState = EmailSetupPhase.NONE;
					PrintUtils.error(p,"'" + email + "' is not a valid email address!");
					return;
				}
				
				Properties properties = System.getProperties();
				properties.setProperty(EmailOTPAuthMethod.getMailServer().asString(),EmailOTPAuthMethod.getHost().asString());
				
				Session session = Session.getDefaultInstance(properties);
				
				try
				{
					// MimeMessage object
					MimeMessage message = new MimeMessage(session);
					
					message.setFrom(new InternetAddress(EmailOTPAuthMethod.getSender().asString()));
					message.addRecipient(Message.RecipientType.TO,new InternetAddress(email));
					message.setContent(getOTPEmailContent("123456"),"text/html");
					
					Transport.send(message);
					
					PrintUtils.info(p,"A one-time-passcode has been sent to " + email);
					ChatUtils.requestInput(p,"Enter your one-time-passcode that you recieved.",Namespace.of(Dape.getNamespaceName(),ChatUtils.HandlerNames.ELEVATED_ACCOUNTS_AUTH_METHOD_SETUP_EMAIL_OTP));
				}
				catch(Exception ex)
				{
					setupState = EmailSetupPhase.NONE;
					PrintUtils.error(p,"An error occured attempting to send a one-time-passcode to " + email);
					Logg.error("Error occured attempting to send email to " + email,ex);
				}
				
				return;
			}
			case TEST_OTP ->
			{
				setupState = EmailSetupPhase.NONE;
			}
			default ->
			{
				PrintUtils.error(e.getPlayer(),"Error! Incorrect OTP setup state! (" + setupState + ")");
				return;
			}
		}
		
		if(!otpMethod.verifyMethod(e.getInput(),p)) { return; }
		
		JsonObject emailObj = new JsonObject();
		emailObj.addProperty(EmailOTPAuthMethod.EMAIL,otpMethod.getEmailAddress().asString());
		
		boolean hasExistingAuthMethod = false;
		
		// If elevated account owner already has an existing static pin method we can overwrite it
		for(AuthenticationMethod meth : ElevatedAccountCtrl.getAccount(e.getPlayer()).getAuthMethods())
		{
			if(meth.getAuthType() != AuthMethod.EMAIL_OTP) { continue; }
			hasExistingAuthMethod = true;
			
			EmailOTPAuthMethod existingOTPMethod = (EmailOTPAuthMethod) meth;
			
			try
			{
				existingOTPMethod.deserialise(emailObj);
			}
			catch(Exception e1)
			{
				Logg.error("Error occured deserialising new TOTP data to existing TOTP authentication method!",e1);
				PrintUtils.error(p,"An error occured updating your timed one-time-passcode!");
				return;
			}
			
			ElevatedAccountCtrl.save(e.getPlayer().getUniqueId());
			PrintUtils.success(p,"TOTP updated!");
			break;
		}
		
		// If elevated account owner does not have an existing static pin method we need to create a new one
		if(!hasExistingAuthMethod)
		{
			EmailOTPAuthMethod newOTPMethod;
			
			try
			{
				newOTPMethod = new EmailOTPAuthMethod(emailObj);
			}
			catch (DeserialiseException e1)
			{
				Logg.error("Error occured deserialising new OTP data to new OTP authentication method!",e1);
				PrintUtils.error(p,"An error occured creating your OTP!");
				return;
			}
			
			// If authentication methods are locked, we can't add new ones. Set as pending until server shutdown
			if(ElevatedAccountCtrl.Config.LOCKED_AUTH_METHODS.get())
			{
				try
				{
					ElevatedAccountCtrl.setPendingAuthChange(e.getPlayer().getUniqueId(),newOTPMethod);
					PrintUtils.success(p,"OTP pending for creation!");
					PrintUtils.info(p,"For OTP change to take effect, you or someone must restart the server!");
				}
				catch (IllegalMethodCallException e1)
				{
					Logg.error("Error occured setting new OTP authentication method as a pending change!",e1);
					PrintUtils.error(p,"An error occured attempting to set your OTP change as pending!");
					return;
				}
			}
			// Authentication methods are not locked so we can just add a new one
			else
			{
				ElevatedAccountCtrl.getAccount(e.getPlayer()).getAuthMethods().add(newOTPMethod);
				ElevatedAccountCtrl.save(e.getPlayer().getUniqueId());
				PrintUtils.success(p,"OTP created!");
			}
		}
		
		// overwrite memory
		otpMethod = null;
		
		// Remove temp pin method if exists
		ElevatedAccountCtrl.getAccount(e.getPlayer()).markTempPinForRemoval();
		
		PrintUtils.success(p,"OTP setup complete!");
	}
	
	private enum EmailSetupPhase
	{
		NONE,
		ENTER_EMAIL,
		SEND_EMAIL_OTP,
		TEST_OTP
	}
}
