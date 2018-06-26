package javaapplication2;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Properties;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
/**
 * @author akhandwala
 * Code uses the API to fetch data from Qualtrics survey and is scheduled to run on the 
 * system's internal server which is maintained by DoIt Stony Brook
 */

public class connect {

  static final String propFileName = "IAPS.property";
  static KeyGenerator keygenerator;
  static SecretKey mydesKey;
  static Cipher desCipher; 
 
  //The method generates a cipher which will be used to encrypt the password
  //The method code is reused from mkyong.com
  private static void initCipher()
  {
    try
    {
      keygenerator = KeyGenerator.getInstance("des");
      mydesKey = keygenerator.generateKey();
      desCipher = Cipher.getInstance("des/ecb/pkcs5padding");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  //This method takes in the API token as plain text and generates an encrypted pw using the initCipher() 
  //The method code is reused from mkyong.com
  private static byte[] encrypt(String plaintxt)
  {
    byte[] res = "Default".getBytes();
    try
    {
      desCipher.init(1, mydesKey);
      byte[] plainByte = plaintxt.getBytes();
      byte[] cipherByte = desCipher.doFinal(plainByte);
      res = cipherByte;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    System.out.println("res:" + res.toString());
    return res;
  }
 
  //This method can be used to convert the encrypted pw back to plaintext
  //The method code is reused from mkyong.com
  private static String decrypt(byte[] cipherByte)
  {
    String res = "Default";
    System.out.println("cipherByte" + cipherByte.toString());
    try
    {
      desCipher.init(2, mydesKey);
      byte[] plainByte = desCipher.doFinal(cipherByte);
      res = new String(plainByte);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return res;
  }
  
  
  //The main class uses the Qualtrics API to fetch the data from the form and store in a csv format
  public static void main(String[] args) throws Exception {
	// TODO Auto-generated method stub
	
	HttpURLConnection connection = null;
	String outputFileName = "";
    String currentFile = "GlobalITPToday.csv";
    String serverStr = "./";
    String fileFormat = "./test_%s.csv";
    String pwd = "PT:&Token=#############";
    
    //link from Qualtrics API to get form data 
    String urlStr = "https://stonybrook.co1.qualtrics.com/WRAPI/ControlPanel/api.php?Request=getLegacyResponseData&User=Qualtrics%23stonybrookuniversity$pwd$&Format=CSV&Version=2.0&SurveyID=SV_9Ytfvazp1YmG2SV&Labels=1";
    
    
    Properties prop = new Properties();
    //first search if there is already a file as IAPS.property in the current path, and load the parameters from the file
    try
    {
      prop.load(new FileInputStream("IAPS.property"));
      
      urlStr = prop.getProperty("PathtoQualtrics");
      serverStr = prop.getProperty("PathToServer");
      fileFormat = prop.getProperty("FileFormat");
      currentFile = prop.getProperty("FileNameMarkAsToday");
      pwd = prop.getProperty("pwd");
    }
    //if no file found then set the default parameters. A new file will be generated with default parameters 
    catch (FileNotFoundException e)
    {
      prop.setProperty("PathtoQualtrics", urlStr);
      prop.setProperty("PathToServer", serverStr);
      prop.setProperty("FileFormat", fileFormat);
      prop.setProperty("FileNameMarkAsToday", currentFile);
      prop.setProperty("pwd", pwd);
    }
    catch (IOException e2)
    {
      e2.printStackTrace();
    }
   
    Date today = new Date();
    String timeStamp = String.format("%tF@%tr", new Object[] { today, today });
    outputFileName = String.format(fileFormat, new Object[] { timeStamp });
    
    try
    {
      initCipher();
      if (pwd.substring(0, 2).compareTo("PT") == 0)
      {
        urlStr = urlStr.replace("$pwd$", pwd.substring(3));
        prop.setProperty("pwd", "CT:" + encrypt(pwd.substring(3)).toString());
      }
      else
      {
        System.exit(0);
      }
      URL url = new URL(urlStr);
      connection = (HttpURLConnection)url.openConnection();
      connection.setRequestMethod("GET");
      connection.setRequestProperty("Content-Type", "application/json");
      
      outputFileName = outputFileName.replaceFirst(":", "'");
      outputFileName = outputFileName.replaceFirst(":", "''");
      File file = new File(outputFileName);
      BufferedWriter bw = new BufferedWriter(new FileWriter(file));
      BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      
      File currFile = new File(currentFile);
      BufferedWriter bw2 = new BufferedWriter(new FileWriter(currFile));
      
      String line = "";
      while ((line = rd.readLine()) != null)
      {
        bw.write(line + "\n");
        bw2.write(line + "\n");
      }
      bw.close();
      bw2.close();
      
      rd.close();
      
      prop.store(new FileOutputStream("IAPS.property"), null);
		    }
		    catch (Exception e)
		    {
		      e.printStackTrace();
		    }
	}

}
