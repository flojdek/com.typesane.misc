package com.typesane
 
import java.security.MessageDigest

object HashUtil {

  /**
   * Generate SHA1 hash string for given `x`.
   */
  def sha1(x: String): String = {
    MessageDigest.getInstance("SHA-1").digest(x.getBytes).map("%02x".format(_))
      .mkString
  }
}
