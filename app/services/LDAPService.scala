package app.services

import scala.collection.mutable

import com.typesafe.config.ConfigFactory
import com.unboundid.ldap.sdk._

trait LDAPService {

  val configuration = ConfigFactory.load
  val host = configuration.getString("ldap.host")
  val port = configuration.getInt("ldap.port")
  val ldaps = configuration.getBoolean("ldap.ldaps")
  val baseDN = configuration.getString("ldap.baseDN")
  val bindDN = configuration.getString("ldap.bindDN")
  val password = configuration.getString("ldap.password")
  val uidAttributeName = configuration.getString("ldap.uidAttributeName")
  val initialConnextions = configuration.getInt("ldap.initialConnextions")
  val maxConnections = configuration.getInt("ldap.maxConnections")

  //TODO: Create connection options

  case class UserConnection(
    dn: String,
    connection: LDAPConnection
  //TODO: Add user's role.
  )

  /**
   * Create connection using by config user.
   */
  protected val defaultConnection = new LDAPConnection(host, port, bindDN, password)

  /**
   * The connections store by user.
   */
  protected val connections: mutable.HashMap[String, UserConnection] = mutable.HashMap()

  /**
   * Create connection by users and store it.
   */
  def createConnectionByUser(uid: String, dn: String, password: String): Unit = {
    val connection = UserConnection(dn, new LDAPConnection(host, port, dn, password))
    connections += (uid -> connection)
  }

  /**
   * Remove & close connection from connections store by User.
   */
  def removeConnectionByUser(uid: String): Unit = {
    //TODO: Release connection certainly.
    connections.get(uid) match {
      case Some(uc) => {
        uc.connection.close
        connections -= uid
      }
      case None => None
    }
  }

  /**
   * Get Connection by uid.
   */
  def getConnectionByUser(uid: String): Option[UserConnection] = {
    connections.get(uid)
  }

  /*
  val connectionPool: LDAPConnectionPool = {
    if (ldaps) {
      //TODO: Have to change create over TLS connection.
      new LDAPConnectionPool(connection, initialConnextions, maxConnections)
    } else {
      new LDAPConnectionPool(connection, initialConnextions, maxConnections)
    }
  }
  */

}