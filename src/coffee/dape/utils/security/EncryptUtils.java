package coffee.dape.utils.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import coffee.dape.Dape;
import coffee.dape.utils.FUtils;
import coffee.dape.utils.Logg;

public final class EncryptUtils
{
	private static KeyStore keyStore;
	private static final String hash = "$2y$13$12bXq0B33OAlwZow3BiNve/bfGS.8uMw/aL50GhYzZWyfzJCKywe.";
	private static Path keyStoreFile = Dape.internalFilePath("keys" + File.separator + "store");
	private static boolean loaded = false;
	
	static
	{
		try
		{
			keyStore = KeyStore.getInstance("pkcs12");
		}
		catch (KeyStoreException e)
		{
			Logg.error("Could not initialise key store!",e);
		}
	}
	
	/**
	 * Load the existing key store
	 * @param password Password to access the key store
	 */
	public static void loadKeystore(String password)
	{
		// Check if a keystore already exists
		if(!Files.exists(keyStoreFile))
		{
			// No keystore exists, create a new one
			try
			{
				keyStore.load(null,password.toCharArray());
				FUtils.createDirectoriesForFile(keyStoreFile);
				loaded = true;
			}
			catch (Exception e)
			{
				Logg.error("Error occured attempting to create a blank keystore!",e);
				e.printStackTrace();
			}
			
			return;
		}
		
		try(FileInputStream fis = new FileInputStream(keyStoreFile.toFile()))
		{
			keyStore.load(fis,password.toCharArray());
			loaded = true;
		}
		catch(Exception e)
		{
			Logg.error("Could not load keystore!",e);
		}
	}
	
	/**
	 * Set a key value entry in the key store
	 * @param key Key used to access this entry later
	 * @param entry Value to store in the key store
	 * @throws KeyStoreException
	 */
	public static void setKeyStoreEntry(String key,String value) throws KeyStoreException
	{
		KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(new SecretKeySpec(value.getBytes(),"DSA"));
		keyStore.setEntry(key,secretKeyEntry,new KeyStore.PasswordProtection(hash.toCharArray()));
	}
	
	/**
	 * 
	 * @param key Key used to access this entry later
	 * @return Value of the key store entry
	 * @throws Exception
	 */
	public static String getKeyStoreEntry(String key) throws Exception
	{
		KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(key,new KeyStore.PasswordProtection(hash.toCharArray()));
		return new String(secretKeyEntry.getSecretKey().getEncoded(),StandardCharsets.UTF_8);
	}
	
	/**
	 * Saves the key store
	 * @param password Password to access the key store the next time it is loaded
	 */
	public static void saveKeyStore(String password)
	{
		Path f = Dape.internalFilePath("keys" + File.separator + "store");
		FUtils.createDirectories(f);
		
		try(FileOutputStream fos = new FileOutputStream(keyStoreFile.toFile()))
		{
			keyStore.store(fos,password.toCharArray());
		}
		catch(Exception e)
		{
			Logg.error("Could not save keystore!",e);
		}
	}
	
	/**
	 * Encrypts an RSA message
	 * @param message String data to encrypt
	 * @param key Public key
	 * @return encrypted byte data
	 * @throws Exception
	 */
	public static byte[] encrypt(String message,PublicKey key) throws Exception
	{
		Cipher encryptCipher = Cipher.getInstance("RSA");
		encryptCipher.init(Cipher.ENCRYPT_MODE,key);
		
		byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
		return encryptCipher.doFinal(messageBytes);
	}
	
	/**
	 * Decrypts an RSA message
	 * @param encryptedMessage encrypted byte data
	 * @param key Private key
	 * @return Decrypted data
	 * @throws Exception
	 */
	public static String decrypt(byte[] encryptedMessage,PrivateKey key) throws Exception
	{
		Cipher decryptCipher = Cipher.getInstance("RSA");
		decryptCipher.init(Cipher.DECRYPT_MODE,key);
		
		byte[] decryptedMessageBytes = decryptCipher.doFinal(encryptedMessage);
		return new String(decryptedMessageBytes,StandardCharsets.UTF_8);
	}
	
	/**
	 * Generate a new Public and Private key pair
	 * @return Private and Public pair
	 * @throws NoSuchAlgorithmException
	 */
	public static KeyPair getNewKeyPair() throws NoSuchAlgorithmException
	{
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		return generator.generateKeyPair();
	}
	
	/**
	 * Read a private key from a file
	 * @param file File location of private key
	 * @return Private key
	 * @throws Exception
	 */
	public static PrivateKey readPrivateKey(File file) throws Exception
	{
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Files.readAllBytes(file.toPath()));
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePrivate(keySpec);
	}
	
	/**
	 * Read a public key from a file
	 * @param file File location of public key
	 * @return Public key
	 * @throws Exception
	 */
	public static PublicKey readPublicKey(File file) throws Exception
	{
		X509EncodedKeySpec spec = new X509EncodedKeySpec(Files.readAllBytes(file.toPath()));
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePublic(spec);
	}
	
	/**
	 * Write a private key to file
	 * @param keyName Name of the key file
	 * @param key Private Key
	 */
	public static void writePrivateKey(String keyName,PrivateKey key)
	{
		try (FileOutputStream fos = new FileOutputStream(keyName))
		{
		    fos.write(key.getEncoded());
		}
		catch(Exception e)
		{
			Logg.error("Could not write private key " + keyName + "!",e);
		}
	}
	
	/**
	 * Write a public key to file
	 * @param keyName Name of the key file
	 * @param key Public key
	 */
	public static void writePublicKey(String keyName,PublicKey key)
	{
		try (FileOutputStream fos = new FileOutputStream(keyName))
		{
		    fos.write(key.getEncoded());
		}
		catch(Exception e)
		{
			Logg.error("Could not write public key " + keyName + "!",e);
		}
	}
	
	/**
	 * Encodes byte data into a string
	 * @param byteData byte array
	 * @return String representing encoded byte data
	 */
	public static String toBase64(byte[] byteData)
	{
		return Base64.getEncoder().encodeToString(byteData);
	}
	
	/**
	 * Decodes string back into byte data
	 * @param base64EncodedBytes String representing encoded byte data
	 * @return byte data
	 */
	public static byte[] fromBase64(String base64EncodedBytes)
	{
		return Base64.getDecoder().decode(base64EncodedBytes);
	}

	public static final boolean isLoaded()
	{
		return loaded;
	}
	
	public class Common
	{
		
	}
}
