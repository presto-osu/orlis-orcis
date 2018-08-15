package com.twofours.surespot;

import java.security.SecureRandom;
import java.util.Random;
import java.math.BigInteger;
import java.lang.StringBuilder;

public class PassString
{

  private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz,.-;:$?!%&=+*#{[]}<>";
  private static Random rnd = new Random();
  private static SecureRandom random = new SecureRandom();
  
  public static String randomString(int len)
  {
     StringBuilder sb = new StringBuilder(len);
     for( int i = 0; i < len; i++ )
     {
        sb.append(AB.charAt(random.nextInt(AB.length())));
      }
     return sb.toString();
  }

  public static String nextSessionId()
  {
    return new BigInteger(130, random).toString(32);
  }
}
