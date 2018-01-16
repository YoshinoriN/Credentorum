package app.utils.auth

import javax.inject.Inject

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.cache._
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import app.utils.config.LDAPConfig

class LDAPAuth @Inject() (
  cache: AsyncCacheApi
) extends DelegableAuthInfoDAO[PasswordInfo] {

  def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    cache.set(loginInfo.providerKey, PersistentAuthInfo(loginInfo, authInfo), LDAPConfig.expiryDuration).map(_ => authInfo)
  }

  //TODO: Is this method work well ?
  def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    add(loginInfo, authInfo)
  }

  //TODO: Is this method work well ?
  def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    add(loginInfo, authInfo)
  }

  def remove(loginInfo: LoginInfo): Future[Unit] = {
    Future.successful(cache.remove(loginInfo.providerKey).map(_ => loginInfo))
  }

  //FIXME : Exception occur
  def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
    cache.get(loginInfo.providerKey)
  }

}

case class PersistentAuthInfo(
  loginInfo: LoginInfo,
  passwordInfo: PasswordInfo
)
