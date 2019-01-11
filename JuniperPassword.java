// https://forums.juniper.net/t5/Junos/Password-encryption-algorithm-in-Junos/td-p/96208
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;  

/**
 * Class to encrypt passwords used in Junos configurations.
 * 
 * @author dkasten
 */
public final class JuniperPassword
{
  public static void main(String[] args)
  {
    String plain = args[0];
    String encrypted =  encrypt9(plain);
    System.out.println(encrypted);
  }

  private static final String itoa64 = "./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
  private static Random rnd = new Random();
  
  // Constants for encrypt9 method
  private static final String MAGIC = "$9$";
  private static final String[] FAMILY = {"QzF3n6/9CAtpu0O", "B1IREhcSyrleKvMW8LXx", "7N-dVbwsY2g4oaJZGUDj", "iHkq.mPf5T"};
  private static Map<String,Integer> EXTRA = new HashMap<String,Integer> ();
  private static Pattern VALID;
  private static final String[] NUM_ALPHA;
  private static Map<String,Integer> ALPHA_NUM = new HashMap<String,Integer> ();
  private static final int[][] ENCODING = {
      {1,  4, 32},
      {1, 16, 32},
      {1,  8, 32},
      {1, 64    },
      {1, 32    },
      {1,  4, 16, 128},
      {1, 32, 64}
  };
  
  static {
    // Prepare EXTRA constant for use with encrypt9 method
    for (int fam = 0; fam < FAMILY.length; fam++) {
      for (char c : FAMILY[fam].toCharArray()) {
        EXTRA.put(String.valueOf(c), 3 - fam);
      }
    }
    
    // Prepare VALID RegEx pattern constant for use with decrypt9 method (VALID currently not used as decrypt9 has not been added to class but letters and end variables are used in next section)
    StringBuilder letters = new StringBuilder("");
    for (String item : FAMILY) {
      letters.append(item);
    }
    String end = letters.toString() + "{4,}$";
    end.replace("-", "\\-");
    VALID = Pattern.compile("^\\Q" + MAGIC + "\\E" + end);
    
    // Prepare NUM_ALPHA and ALPHA_NUM constants for use with encrypt9 method
    NUM_ALPHA = new String[letters.length()];
    int x = 0;
    for (char item : letters.toString().toCharArray()) {
      NUM_ALPHA[x] = String.valueOf(item);
      x++;
    }
    for (int num = 0; num < NUM_ALPHA.length; num++) {
      ALPHA_NUM.put(NUM_ALPHA[num], num);
    }
  }
  
  /**
   * Creates a non-reversable $1 password used for user logins on Junos configurations.  Wrapper with try/catch block and random salt creations
   * for the {@link #crypt(String, String)} method.
   * 
   * @param pw Password to encrypt.
   * @return String - Encrypted password.  Can be null if encryption fails.
   */
  public static String encrypt1(String pw)
  {
    try
    {
      return crypt(pw, randomSalt(8));
    }
    catch (NoSuchAlgorithmException e)
    {
      e.printStackTrace();
      return null;
    }
  }
  
  /**
   * Creates a reversable $9 password used for most passwords in Junos configurations.
   * 
   * @param pw Password to encrypt.
   * @return String - Encrypted password.  Can be null if encryption fails.
   */
  public static String encrypt9(String pw)
  {
    String salt = randomSalt(1);
    String rand = randomSalt(EXTRA.get(salt));
    int pos = 0;
    String prev = salt;
    String crypt = MAGIC + salt + rand;
    
    for (char item : pw.toCharArray()) {
      int[] encode = ENCODING[pos % ENCODING.length];
      crypt += gapEncode(item, prev, encode);
      prev = crypt.substring(crypt.length() - 1);
      pos++;
    }
    
    return crypt;
  }
  
  /**
   * Helper function to generate random salt strings.
   * 
   * @param len Length of the random salt to create.
   * @return String - Generated salt string.
   */
  private static String randomSalt(int len)
  {
    StringBuilder sb = new StringBuilder(len);
    for (int i = 0; i < len; i++)
    {
      sb.append(itoa64.charAt(rnd.nextInt(itoa64.length())));
    }
    return sb.toString();
  }

  /**
   * Conversion method that is part of the FreeBSD crypt algorithm.  Not designed to be called except by {@link #crypt(String, String)}.
   */
  private static String to64(long v, int n)
  {
    String output = "";

    while (--n >= 0)
    {
      output += itoa64.charAt((int) (v & 0x3f));
      v >>=6;
    }

    return (output);
  }

  /**
   * FreeBSD crypt algorithm converted from C to Java.
   * 
   * @param pw Password to encrypt.
   * @param salt Salt to use during encryption.  If salt is longer than 8 characters it will be truncated to 8 characters.
   *             Also will only take characters up to a $ character.
   * @return String - FreeBSD encrypted password.
   * @throws java.security.NoSuchAlgorithmException System doesn't support MD5 algorithm.
   */
  private static String crypt(String pw, String salt) throws java.security.NoSuchAlgorithmException
  {
    final String magic = "$1$";
    String passwd, sp;
    byte[] finalVar;
    int pl, i, j;
    MessageDigest ctx, ctx1;
    long l;

    // Refine the Salt first
    sp = salt;
    
    // If it starts with the magic string, then skip that
    if (sp.startsWith(magic))
    {
      sp = sp.substring(magic.length(), sp.length());
    }
    
    // It stops at the first '$', max 8 chars
    if (sp.contains("$"))
    {
      sp = sp.substring(0, sp.indexOf("$"));
    }
    if (sp.length() > 8)
    {
      sp = sp.substring(0,8);
    }

    ctx = MessageDigest.getInstance("MD5");

    // The password first, since that is what is most unknown
    ctx.update(pw.getBytes());
    
    // Then our magic string
    ctx.update(magic.getBytes());
    
    // Then the raw salt
    ctx.update(salt.getBytes());

    ctx1 = MessageDigest.getInstance("MD5");

    // Then just as many characters of the MD5(pw,salt,pw)
    ctx1.update(pw.getBytes());
    ctx1.update(sp.getBytes());
    ctx1.update(pw.getBytes());
    finalVar = ctx1.digest();
    for (pl = pw.length(); pl > 0; pl -= 16)
    {
      ctx.update(finalVar, 0, pl>16 ? 16 : pl);
    }

    // Don't leave anything around in vm they could use.
    for (@SuppressWarnings("unused") byte item : finalVar)
    {
      item = (byte)0;
    }
    
    // Then something really weird...
    for (j = 0, i = pw.length(); i != 0; i >>= 1)
    {
      if ((i & 1) != 0)
      {
        ctx.update(finalVar[j]);
      }
      else
      {
        ctx.update(pw.getBytes()[j]);
      }
    }
    
    // Now make the output string
    passwd = magic;
    passwd += sp;
    passwd += "$";
    
    finalVar = ctx.digest();
    
    // and now, just to make sure things don't run too fast 
    // On a 60 Mhz Pentium this takes 34 msec, so you would
    // need 30 seconds to build a 1000 entry dictionary...
    for (i = 0; i < 1000; i++)
    {
      ctx1.reset();
      if ((i & 1) != 0)
      {
        ctx1.update(pw.getBytes());
      }
      else
      {
        ctx1.update(finalVar);
      }
      
      if ((i % 3) != 0)
      {
        ctx1.update(sp.getBytes());
      }
      if ((i % 7) != 0)
      {
        ctx1.update(pw.getBytes());
      }
      if ((i & 1) != 0)
      {
        ctx1.update(finalVar);
      }
      else
      {
        ctx1.update(pw.getBytes());
      }
      finalVar = ctx1.digest();
    }
    
    l = (finalVar[ 0]<<16) | (finalVar[ 6]<<8) | finalVar[12]; passwd += to64(l,4);
    l = (finalVar[ 1]<<16) | (finalVar[ 7]<<8) | finalVar[13]; passwd += to64(l,4);
    l = (finalVar[ 2]<<16) | (finalVar[ 8]<<8) | finalVar[14]; passwd += to64(l,4);
    l = (finalVar[ 3]<<16) | (finalVar[ 9]<<8) | finalVar[15]; passwd += to64(l,4);
    l = (finalVar[ 4]<<16) | (finalVar[10]<<8) | finalVar[ 5]; passwd += to64(l,4);
    l =                       finalVar[11]                   ; passwd += to64(l,2);
    
    
    // Don't leave anything around in VM they could use.
    for (@SuppressWarnings("unused") byte item : finalVar)
    {
      item = (byte)0;
    }        
    
    return passwd;
  }
  
  /**
   * Helper function for {@link #encrypt9(String)} method.  Generates part of the encrypted string and returns it back to the {@link #encrypt9(String)} method.
   *
   * @param pc Character to encrypt.
   * @param prev Previous series of encrypted characters or salt.
   * @param enc Current encoder values.
   * @return String - Encrypted version of the character (can be 2 to 4 characters).
   */
  private static String gapEncode(char pc, String prev, int[] enc) {
    String crypt = "";
    int ord = (int) pc;
    Deque<Integer> gaps = new LinkedList<Integer>();
    
    for (int x = enc.length - 1; x >= 0; x--) {
      gaps.addFirst(ord / enc[x]);
      ord %= enc[x];
    }
    
    for (Integer item : gaps) {
      item += ALPHA_NUM.get(prev) + 1;
      String c = prev = NUM_ALPHA[item % NUM_ALPHA.length];
      crypt += c;
    }
    
    return crypt;
  }
}
