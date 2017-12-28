package app.models

import scala.collection.mutable
import scala.concurrent.Future
import com.mohiva.play.silhouette.api.{ Identity, LoginInfo }
import app.models.UserIdentify
import app.models.UserDAOImpl._
import app.models.ldap.UserConnection
import app.utils.cache.PlaySyncCacheLayer
import app.utils.types.UserId

/**
 * The companion object.
 */
object UserDAOImpl {

  /**
   * The list of users.
   * TODO: Use EhCache
   */
  val users: mutable.HashMap[UserId, UserIdentify] = mutable.HashMap()

}

/**
 * Give access to the user object.
 */
class UserDAOImpl extends UserDAO {

  def find(loginInfo: LoginInfo) = {
    PlaySyncCacheLayer.cache.get[UserConnection](loginInfo.providerKey) match {
      case Some(uc) => {
        Future.successful(users.find { case (_, user) => user.loginInfo == loginInfo }.map(_._2))
      }
      case None => Future.successful(None)
    }
  }

  def find(userID: UserId) = Future.successful(users.get(userID))

  def save(user: UserIdentify) = {
    users += (user.userID -> user)
    Future.successful(user)
  }

}

trait UserDAO {

  def find(loginInfo: LoginInfo): Future[Option[UserIdentify]]

  def find(userID: UserId): Future[Option[UserIdentify]]

  def save(user: UserIdentify): Future[UserIdentify]

}

case class UserIdentify(
  userID: UserId,
  loginInfo: LoginInfo,
  isAdmin: Boolean) extends Identity
