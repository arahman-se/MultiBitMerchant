package org.multibit.mbm.auth.hmac;

import com.google.common.base.Optional;
import com.xeiam.xchange.utils.CryptoUtils;
import com.yammer.dropwizard.auth.AuthenticationException;
import com.yammer.dropwizard.auth.Authenticator;
import org.multibit.mbm.db.dao.UserDao;
import org.multibit.mbm.db.dto.User;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

/**
 * <p>Authenticator to provide the following to application:</p>
 * <ul>
 * <li>Verifies the provided credentials are valid</li>
 * </ul>
 *
 * @since 0.0.1
 */
public class HmacAuthenticator implements Authenticator<HmacCredentials, User> {

  private UserDao userDao;

  @Override
  public Optional<User> authenticate(HmacCredentials credentials) throws AuthenticationException {

    // Get the User referred to by the API key
    User user = userDao.getUserByUUID(credentials.getApiKey());

    // Locate their secret key
    String secretKey = user.getSecretKey();

    try {
      String computedSignature= CryptoUtils.computeSignature(
        credentials.getAlgorithm(),
        credentials.getContents(),
        secretKey);

      if (computedSignature.equals(credentials.getDigest())) {
        return Optional.of(user);
      }
    } catch (GeneralSecurityException e) {
      return Optional.absent();
    } catch (UnsupportedEncodingException e) {
      return Optional.absent();
    }

    return Optional.absent();

  }

  public void setUserDao(UserDao userDao) {
    this.userDao = userDao;
  }
}
