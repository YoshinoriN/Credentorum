package app.utils.auth

import scala.concurrent.Future
import play.api.mvc.Request
import com.mohiva.play.silhouette.api.{ Authenticator, Authorization }
import app.models.UserIdentify

/**
 * Grants only access if a user has authenticated with the given provider.
 *
 * @param provider The provider ID the user must authenticated with.
 * @tparam A The type of the authenticator.
 */
case class WithAdmin[A <: Authenticator](provider: String) extends Authorization[UserIdentify, A] {

  /**
   * Indicates if a user is authorized to access an action.
   *
   * @param user The usr object.
   * @param authenticator The authenticator instance.
   * @param request The current request.
   * @tparam B The type of the request body.
   * @return True if the user is authorized, false otherwise.
   */
  override def isAuthorized[B](user: UserIdentify, authenticator: A)(implicit request: Request[B]): Future[Boolean] = {
    Future.successful(user.isAdmin)
  }
}
