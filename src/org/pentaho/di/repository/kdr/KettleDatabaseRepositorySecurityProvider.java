package org.pentaho.di.repository.kdr;

import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleSecurityException;
import org.pentaho.di.repository.BaseRepositorySecurityProvider;
import org.pentaho.di.repository.IRole;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ProfileMeta;
import org.pentaho.di.repository.RepositoryCapabilities;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.repository.RepositorySecurityProvider;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.repository.ProfileMeta.Permission;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryConnectionDelegate;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryPermissionDelegate;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryProfileDelegate;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryUserDelegate;

public class KettleDatabaseRepositorySecurityProvider extends BaseRepositorySecurityProvider implements
    RepositorySecurityProvider, RepositorySecurityManager {

  private RepositoryCapabilities capabilities;

  private KettleDatabaseRepository repository;

  private KettleDatabaseRepositoryUserDelegate userDelegate;

  private KettleDatabaseRepositoryProfileDelegate profileDelegate;

  private KettleDatabaseRepositoryPermissionDelegate permissionDelegate;

  private KettleDatabaseRepositoryConnectionDelegate connectionDelegate;

  /**
   * @param repository 
   * @param userInfo
   */
  public KettleDatabaseRepositorySecurityProvider(KettleDatabaseRepository repository, RepositoryMeta repositoryMeta,
      UserInfo userInfo) {
    super(repositoryMeta, userInfo);
    this.repository = repository;
    this.capabilities = repositoryMeta.getRepositoryCapabilities();

    // This object is initialized last in the KettleDatabaseRepository constructor.
    // As such it's safe to keep references here to the delegates...
    //
    userDelegate = repository.userDelegate;
    profileDelegate = repository.profileDelegate;
    permissionDelegate = repository.permissionDelegate;
    connectionDelegate = repository.connectionDelegate;
  }

  public boolean isReadOnly() {
    return capabilities.isReadOnly() || (userInfo.isReadOnly() && !userInfo.isAdministrator());
  }

  public boolean isLockingPossible() {
    return capabilities.supportsLocking()
        && (userInfo != null && (userInfo.supportsLocking() || userInfo.isAdministrator()));
  }

  public boolean allowsVersionComments() {
    return false;
  }

  public boolean isVersionCommentMandatory() {
    return false;
  }

  public ProfileMeta loadProfileMeta(ObjectId id_profile) throws KettleException {
    return profileDelegate.loadProfileMeta(new ProfileMeta(), id_profile);
  }

  public void saveProfile(ProfileMeta profileMeta) throws KettleException {
    profileDelegate.saveProfileMeta(profileMeta);
  }

  // UserInfo

  public UserInfo loadUserInfo(String login) throws KettleException {
    return userDelegate.loadUserInfo(new UserInfo(), login);
  }

  public void saveUserInfo(UserInfo userInfo) throws KettleException {
    userDelegate.saveUserInfo(userInfo);
  }

  public void validateAction(RepositoryOperation... operations) throws KettleException, KettleSecurityException {

    // If there is no user available, we fail
    //
    if (userInfo == null) {
      throw new KettleException("A valid user is needed to use this repository");
    }

    // No questions asked for an administrator...
    //
    if (userInfo.isAdministrator()) {
      return;
    }

    // If the user is not enabled, not a single operation can take place...
    //
    if (!userInfo.isEnabled()) {
      throw new KettleException("The user is not enabled");
    }

    for (RepositoryOperation operation : operations) {
      switch (operation) {
        case READ_TRANSFORMATION:
          if (!userInfo.useTransformations())
            throw new KettleException(operation + " : user can't use transformations");
          break;
        case MODIFY_TRANSFORMATION:
          if (userInfo.isReadOnly())
            throw new KettleException(operation + " : user is read-only");
          if (capabilities.isReadOnly())
            throw new KettleException(operation + " : repository is read-only");
          if (!userInfo.useTransformations())
            throw new KettleException(operation + " : user can't use transformations");
          break;
        case DELETE_TRANSFORMATION:
          if (userInfo.isReadOnly())
            throw new KettleException(operation + " : user is read-only");
          if (capabilities.isReadOnly())
            throw new KettleException(operation + " : repository is read-only");
          if (!userInfo.useTransformations())
            throw new KettleException(operation + " : user can't use transformations");
          break;
        case EXECUTE_TRANSFORMATION:
          if (!userInfo.useTransformations())
            throw new KettleException(operation + " : user can't use transformations");
          break;

        case READ_JOB:
          if (!userInfo.useJobs())
            throw new KettleException(operation + " : user can't use jobs");
          break;
        case MODIFY_JOB:
          if (userInfo.isReadOnly())
            throw new KettleException(operation + " : user is read-only");
          if (capabilities.isReadOnly())
            throw new KettleException(operation + " : repository is read-only");
          if (!userInfo.useJobs())
            throw new KettleException(operation + " : user can't use jobs");
          break;
        case DELETE_JOB:
          if (userInfo.isReadOnly())
            throw new KettleException(operation + " : user is read-only");
          if (capabilities.isReadOnly())
            throw new KettleException(operation + " : repository is read-only");
          if (!userInfo.useJobs())
            throw new KettleException(operation + " : user can't use jobs");
          break;
        case EXECUTE_JOB:
          if (!userInfo.useJobs())
            throw new KettleException(operation + " : user can't use jobs");
          break;

        case MODIFY_DATABASE:
          if (userInfo.isReadOnly())
            throw new KettleException(operation + " : user is read-only");
          if (capabilities.isReadOnly())
            throw new KettleException(operation + " : repository is read-only");
          if (!userInfo.useDatabases())
            throw new KettleException(operation + " : user can't use databases");
          break;
        case DELETE_DATABASE:
          if (userInfo.isReadOnly())
            throw new KettleException(operation + " : user is read-only");
          if (capabilities.isReadOnly())
            throw new KettleException(operation + " : repository is read-only");
          if (!userInfo.useDatabases())
            throw new KettleException(operation + " : user can't use databases");
          break;
        case EXPLORE_DATABASE:
          if (!userInfo.exploreDatabases())
            throw new KettleException(operation + " : user can't explore databases");
          break;

        case MODIFY_SLAVE_SERVER:
        case MODIFY_CLUSTER_SCHEMA:
        case MODIFY_PARTITION_SCHEMA:
          if (userInfo.isReadOnly())
            throw new KettleException(operation + " : user is read-only");
          if (capabilities.isReadOnly())
            throw new KettleException(operation + " : repository is read-only");
          break;
        case DELETE_SLAVE_SERVER:
        case DELETE_CLUSTER_SCHEMA:
        case DELETE_PARTITION_SCHEMA:
          if (userInfo.isReadOnly())
            throw new KettleException(operation + " : user is read-only");
          if (capabilities.isReadOnly())
            throw new KettleException(operation + " : repository is read-only");
          break;

        default:
          throw new KettleException("Operation [" + operation + "] is unknown to the security handler.");
      }
    }
  }

  // PermissionMeta

  public synchronized void delUser(ObjectId id_user) throws KettleException {
    String sql = "DELETE FROM " + repository.quoteTable(KettleDatabaseRepository.TABLE_R_USER) + " WHERE "
        + repository.quote(KettleDatabaseRepository.FIELD_USER_ID_USER) + " = " + id_user;
    repository.execStatement(sql);
  }

  public synchronized void delProfile(ObjectId id_profile) throws KettleException {
    String sql = "DELETE FROM " + repository.quoteTable(KettleDatabaseRepository.TABLE_R_PROFILE) + " WHERE "
        + repository.quote(KettleDatabaseRepository.FIELD_PROFILE_ID_PROFILE) + " = " + id_profile;
    repository.execStatement(sql);
  }

  public synchronized ObjectId getUserID(String login) throws KettleException {
    return userDelegate.getUserID(login);
  }

  public ObjectId[] getUserIDs() throws KettleException {
    return connectionDelegate.getIDs("SELECT " + repository.quote(KettleDatabaseRepository.FIELD_USER_ID_USER)
        + " FROM " + repository.quoteTable(KettleDatabaseRepository.TABLE_R_USER));
  }

  public synchronized String[] getUserLogins() throws KettleException {
    String loginField = repository.quote(KettleDatabaseRepository.FIELD_USER_LOGIN);
    return connectionDelegate.getStrings("SELECT " + loginField + " FROM "
        + repository.quoteTable(KettleDatabaseRepository.TABLE_R_USER) + " ORDER BY " + loginField);
  }

  public synchronized String[] getProfiles() throws KettleException {
    String nameField = repository.quote(KettleDatabaseRepository.FIELD_PROFILE_NAME);
    return connectionDelegate.getStrings("SELECT " + nameField + " FROM "
        + repository.quoteTable(KettleDatabaseRepository.TABLE_R_PROFILE) + " ORDER BY " + nameField);
  }

  public ObjectId getProfileID(String profilename) throws KettleException {
    return profileDelegate.getProfileID(profilename);
  }

  public synchronized void renameUser(ObjectId id_user, String newname) throws KettleException {
    userDelegate.renameUser(id_user, newname);
  }

  public synchronized void renameProfile(ObjectId id_profile, String newname) throws KettleException {
    profileDelegate.renameProfile(id_profile, newname);
  }

  public void deleteRoles(List<IRole> roles) throws KettleException {
    throw new UnsupportedOperationException();
  }

  public void deleteUsers(List<UserInfo> users) throws KettleException {
    throw new UnsupportedOperationException();
  }

  public List<IRole> getRoles() throws KettleException {
    throw new UnsupportedOperationException();
  }

  public List<UserInfo> getUsers() throws KettleException {
    throw new UnsupportedOperationException();
  }

  public void setRoles(List<IRole> roles) throws KettleException {
    throw new UnsupportedOperationException();
  }

  public void setUsers(List<UserInfo> users) throws KettleException {
    throw new UnsupportedOperationException();
  }

  public void delUser(String name) throws KettleException {
    throw new UnsupportedOperationException();
  }

  public void deleteRole(String name) throws KettleException {
    throw new UnsupportedOperationException();
  }

  public void updateUser(UserInfo role) throws KettleException {
    throw new UnsupportedOperationException();
  }

  public List<String> getAllRoles() throws KettleException {
    throw new UnsupportedOperationException();
  }

  public List<String> getAllUsers() throws KettleException {
    throw new UnsupportedOperationException();
  }


  public IRole constructRole() throws KettleException {
    throw new UnsupportedOperationException();
  }

  public void createRole(IRole role) throws KettleException {
    throw new UnsupportedOperationException();
  }

  public void updateRole(IRole role) throws KettleException {
    throw new UnsupportedOperationException();
  }

  public IRole getRole(String name) throws KettleException {
    throw new UnsupportedOperationException();
  }
}
