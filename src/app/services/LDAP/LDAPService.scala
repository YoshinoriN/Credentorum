package app.services.ldap

import scala.collection.JavaConverters._
import scala.reflect._
import scala.reflect.runtime.universe._
import com.unboundid.ldap.sdk._
import app.models.ldap.{ ActiveDirectoryUser, ActiveDirectoryUserOverView, Computer, Attribute, LDAPObjectOverview, OrganizationUnit }
import app.utils.ClassUtil
import app.utils.types.UserId
import app.utils.config.LDAPConfig

object LDAPService {

  val server = {
    if (LDAPConfig.isActiveDirectory) {
      new ActiveDirectoryService
    } else {
      //TODO: OpenLDAP support
      new ActiveDirectoryService
    }
  }

}

trait LDAPService[T] extends LDAPConnectionProvider {

  /**
   * Check the user is ldap server's administrator or not.
   *
   * @param uid
   * @return Boolean
   */
  def isAdmin(uid: UserId): Boolean = {
    findDN(uid) match {
      case Some(dn) => if (dn == LDAPConfig.administratorDN) true else false
      case None => false
    }
  }

  /**
   * User bind with LDAP server.
   *
   * @param uid The user id for bind LDAP server.
   * @param uid The user password.
   * @return LDAP result code.
   */
  def bind(uid: UserId, password: String): ResultCode = {
    findDN(uid) match {
      case Some(dn) => {
        createConnectionByUser(uid: UserId, dn: String, password: String)
        ResultCode.SUCCESS
      }
      case None => ResultCode.OPERATIONS_ERROR
    }
  }

  /**
   * Search LDAP Object using by current user's connection & filter condition.
   *
   * @param connectionUser The current user id.
   * @param filter Filter condition.
   * @param attributes Attributes to be acquired.
   * @return SearchResultEntries
   */
  protected def search(connectionUser: UserId, filter: com.unboundid.ldap.sdk.Filter, attributes: Seq[String]): Option[Seq[com.unboundid.ldap.sdk.SearchResultEntry]] = {
    findConnectionByUser(connectionUser) match {
      case Some(uc) => {
        val searchResult = {
          val searchRequest = new SearchRequest(LDAPConfig.baseDN, SearchScope.SUB, filter, attributes: _*)
          searchRequest.setSizeLimit(LDAPConfig.maxResults)
          uc.connection.search(searchRequest).getSearchEntries
        }
        searchResult.isEmpty match {
          case false => {
            Some(searchResult.asScala.toSeq)
          }
          case true => None
        }
      }
      case None => None
    }
  }

  /**
   * Map SearchResultEntries to specify LDAP classes.
   *
   * @param SearchResultEntries
   * @return Specify LDAP classes.
   */
  protected def mapSearchResultEntryToLdapClass[T](sr: Seq[com.unboundid.ldap.sdk.SearchResultEntry])(implicit cTag: ClassTag[T]): Seq[T] = {
    val t = for (v <- sr) yield {
      cTag.runtimeClass.getConstructor(classOf[com.unboundid.ldap.sdk.SearchResultEntry]).newInstance(v).asInstanceOf[T]
    }
    t.toSeq
  }

  /**
   * Find dn by uid.
   *
   * @param connectionUser The current user id.
   * @return dn or none.
   */
  def findDN(uid: UserId): Option[String] = {
    val connection = defaultConnection;
    val dn = connection.search(new SearchRequest(
      LDAPConfig.baseDN,
      SearchScope.SUB,
      Filter.createEqualityFilter(LDAPConfig.uidAttributeName, uid.value.toString)
    )).getSearchEntries
    connection.close
    dn.isEmpty match {
      case false => Some(dn.get(0).getDN)
      case true => None
    }
  }

  /**
   * Find specific mapped LDAP objects.
   *
   * @param connectionUser The current user id.
   * @param filter LDAP search filter.
   * @return Specify LDAP object classes or none.
   */
  def find[T](connectionUser: UserId, filter: com.unboundid.ldap.sdk.Filter)(implicit tTag: TypeTag[T], cTag: ClassTag[T]): Option[Seq[T]] = {
    search(connectionUser, filter, ClassUtil.getLDAPAttributeFields[T]) match {
      case Some(sr) => Some(mapSearchResultEntryToLdapClass[T](sr))
      case None => None
    }
  }

  /**
   * Find specific mapped LDAP object that it match distinguishedName.
   *
   * @param connectionUser The current user id.
   * @param dn Target object's distinguishedName.
   * @return Specify LDAP object class or none.
   */
  def findByDN[T](connectionUser: UserId, dn: String)(implicit tTag: TypeTag[T], cTag: ClassTag[T]): Option[T] = {
    search(connectionUser, Filter.createEqualityFilter("distinguishedName", dn), ClassUtil.getLDAPAttributeFields[T]) match {
      case Some(sr) => Some(mapSearchResultEntryToLdapClass[T](sr).head)
      case None => None
    }
  }

  /**
   * Find Domains. (only overview)
   *
   * @param connectionUser The current user id.
   * @return LDAPObjectOverview classes or none.
   */
  def findDomains(connectionUser: UserId): Option[Seq[app.models.ldap.LDAPObjectOverview]] = {
    search(connectionUser, Filter.createEqualityFilter("objectClass", "domain"), ClassUtil.getLDAPAttributeFields[LDAPObjectOverview]) match {
      case Some(sr) => Some(mapSearchResultEntryToLdapClass[LDAPObjectOverview](sr))
      case None => None
    }
  }

  /**
   * Find OrganizationUnit. (only overview)
   *
   * @param connectionUser The current user id.
   * @return LDAPObjectOverview classes or none.
   */
  def findOrganizations(connectionUser: UserId): Option[Seq[app.models.ldap.LDAPObjectOverview]] = {
    search(connectionUser, Filter.createEqualityFilter("objectClass", "organizationalUnit"), ClassUtil.getLDAPAttributeFields[LDAPObjectOverview]) match {
      case Some(sr) => Some(mapSearchResultEntryToLdapClass[LDAPObjectOverview](sr))
      case None => None
    }
  }

  /**
   * Find mapped Computer class.
   *
   * @param connectionUser The current user id.
   * @param targetUid The target uid.
   * @return Computer class or none.
   */
  def findComputer(connectionUser: UserId): Option[Seq[app.models.ldap.Computer]] = {
    search(connectionUser, Filter.createEqualityFilter("objectCategory", "computer"), ClassUtil.getLDAPAttributeFields[Computer]) match {
      case Some(sr) => Some(mapSearchResultEntryToLdapClass[Computer](sr))
      case None => None
    }
  }

  /**
   * Find computers
   *
   * @param connectionUser The current user id.
   * @return LDAPObjectOverview classes or none.
   */
  def findComputers(connectionUser: UserId): Option[Seq[app.models.ldap.LDAPObjectOverview]] = {
    //TODO: objectCategory attributes is only for ActiveDirectory
    search(connectionUser, Filter.createEqualityFilter("objectCategory", "computer"), ClassUtil.getLDAPAttributeFields[LDAPObjectOverview]) match {
      case Some(sr) => Some(mapSearchResultEntryToLdapClass[LDAPObjectOverview](sr))
      case None => None
    }
  }

  /**
   * Find users. (only overview)
   *
   * @param connectionUser The current user id.
   * @return LDAPObjectOverview classes or none.
   */
  def findUsers(connectionUser: UserId): Option[Seq[app.models.ldap.ActiveDirectoryUserOverView]] = {
    search(connectionUser, Filter.createANDFilter(Filter.createEqualityFilter("objectClass", "user"), Filter.createNOTFilter(Filter.create("(objectClass=computer)"))), ClassUtil.getLDAPAttributeFields[LDAPObjectOverview]) match {
      case Some(sr) => Some(mapSearchResultEntryToLdapClass[ActiveDirectoryUserOverView](sr))
      case None => None
    }
  }

  /**
   * Find mapped user class.
   *
   * @param connectionUser The current user id.
   * @param targetUid The target uid.
   * @return T
   */
  def findUser(connectionUser: UserId, targetUid: String): Option[T]

}
