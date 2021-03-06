/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
* This software was developed by Pentaho Corporation and is provided under the terms 
* of the GNU Lesser General Public License, Version 2.1. You may not use 
* this file except in compliance with the license. If you need a copy of the license, 
* please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
* Data Integration.  The Initial Developer is Pentaho Corporation.
*
* Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
* the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.ui.repository.kdr;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.DBCache;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;
import org.pentaho.di.repository.kdr.KettleDatabaseRepositoryMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;
import org.pentaho.di.ui.core.database.dialog.SQLEditor;
import org.pentaho.di.ui.core.dialog.EnterPasswordDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.repository.dialog.RepositoryDialogInterface;
import org.pentaho.di.ui.repository.dialog.UpgradeRepositoryProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * This dialog is used to edit/enter the name and description of a repository.
 * You can also use this dialog to create, upgrade or remove a repository.
 * 
 * @author Matt
 * @since 19-jun-2003
 *
 */

public class KettleDatabaseRepositoryDialog implements RepositoryDialogInterface {
  private static Class<?> PKG = RepositoryDialogInterface.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
  private MODE mode;
  private Label wlConnection;

  private Button wnConnection, weConnection, wdConnection;

  private CCombo wConnection;

  private FormData fdlConnection, fdConnection, fdnConnection, fdeConnection, fddConnection;

  private Label wlId;

  private Text wId;

  private FormData fldId, fdId;

  private Label wlName;

  private Text wName;

  private FormData fdlName, fdName;

  private Button wOK, wCreate, wDrop, wCancel;

  private Listener lsOK, lsCreate, lsDrop, lsCancel;

  private Display display;

  private Shell shell;

  private PropsUI props;

  private KettleDatabaseRepositoryMeta input;

  private RepositoriesMeta repositories;
  
  private RepositoriesMeta masterRepositoriesMeta;

  private String masterRepositoryName;
  
  private DatabaseDialog databaseDialog;
  
  public KettleDatabaseRepositoryDialog(Shell parent, int style, RepositoryMeta repositoryMeta,
      RepositoriesMeta repositoriesMeta) {
    this.display = parent.getDisplay();
    this.props = PropsUI.getInstance();
    this.input = (KettleDatabaseRepositoryMeta) repositoryMeta;
    this.repositories = repositoriesMeta;
    this.masterRepositoriesMeta = repositoriesMeta.clone();
    this.masterRepositoryName = repositoryMeta.getName();

    shell = new Shell(parent, style | SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN | SWT.APPLICATION_MODAL);
    shell.setText(BaseMessages.getString(PKG, "RepositoryDialog.Dialog.Main.Title")); //$NON-NLS-1$

  }

  public KettleDatabaseRepositoryMeta open(final MODE mode) {
    this.mode = mode;
    props.setLook(shell);

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout(formLayout);
    shell.setImage(GUIResource.getInstance().getImageSpoon());
    shell.setText(BaseMessages.getString(PKG, "RepositoryDialog.Dialog.Main.Title2")); //$NON-NLS-1$

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Add the connection buttons :
    wnConnection = new Button(shell, SWT.PUSH);
    wnConnection.setText(BaseMessages.getString(PKG, "System.Button.New")); //$NON-NLS-1$
    weConnection = new Button(shell, SWT.PUSH);
    weConnection.setText(BaseMessages.getString(PKG, "System.Button.Edit")); //$NON-NLS-1$
    wdConnection = new Button(shell, SWT.PUSH);
    wdConnection.setText(BaseMessages.getString(PKG, "System.Button.Delete")); //$NON-NLS-1$

    // Button positions...
    fddConnection = new FormData();
    fddConnection.right = new FormAttachment(100, 0);
    fddConnection.top = new FormAttachment(0, margin);
    wdConnection.setLayoutData(fddConnection);

    fdeConnection = new FormData();
    fdeConnection.right = new FormAttachment(wdConnection, -margin);
    fdeConnection.top = new FormAttachment(0, margin);
    weConnection.setLayoutData(fdeConnection);

    fdnConnection = new FormData();
    fdnConnection.right = new FormAttachment(weConnection, -margin);
    fdnConnection.top = new FormAttachment(0, margin);
    wnConnection.setLayoutData(fdnConnection);

    wConnection = new CCombo(shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
    props.setLook(wConnection);
    fdConnection = new FormData();
    fdConnection.left = new FormAttachment(middle, 0);
    fdConnection.top = new FormAttachment(wnConnection, 0, SWT.CENTER);
    fdConnection.right = new FormAttachment(wnConnection, -margin);
    wConnection.setLayoutData(fdConnection);

    fillConnections();

    // Connection line
    wlConnection = new Label(shell, SWT.RIGHT);
    wlConnection.setText(BaseMessages.getString(PKG, "RepositoryDialog.Label.SelectConnection")); //$NON-NLS-1$
    props.setLook(wlConnection);
    fdlConnection = new FormData();
    fdlConnection.left = new FormAttachment(0, 0);
    fdlConnection.right = new FormAttachment(middle, -margin);
    fdlConnection.top = new FormAttachment(wnConnection, 0, SWT.CENTER);
    wlConnection.setLayoutData(fdlConnection);

    // Add the listeners
    // New connection
    wnConnection.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent arg0) {
        DatabaseMeta databaseMeta = new DatabaseMeta();
        getDatabaseDialog().setDatabaseMeta(databaseMeta);

        if (getDatabaseDialog().open() != null) {
          repositories.addDatabase(getDatabaseDialog().getDatabaseMeta());
          fillConnections();

          int idx = repositories.indexOfDatabase(getDatabaseDialog().getDatabaseMeta());
          wConnection.select(idx);

        }
      }
    });

    // Edit connection
    weConnection.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent arg0) {
        DatabaseMeta databaseMeta = repositories.searchDatabase(wConnection.getText());
        if (databaseMeta != null) {
          getDatabaseDialog().setDatabaseMeta(databaseMeta);
          if (getDatabaseDialog().open() != null) {
            fillConnections();
            int idx = repositories.indexOfDatabase(getDatabaseDialog().getDatabaseMeta());
            wConnection.select(idx);
          }
        }
      }
    });

    // Delete connection
    wdConnection.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent arg0) {
        DatabaseMeta dbinfo = repositories.searchDatabase(wConnection.getText());
        if (dbinfo != null) {
          int idx = repositories.indexOfDatabase(dbinfo);
          repositories.removeDatabase(idx);
          fillConnections();
        }
      }
    });

    // ID line
    wlId = new Label(shell, SWT.RIGHT);
    wlId.setText(BaseMessages.getString(PKG, "RepositoryDialog.Label.ID")); //$NON-NLS-1$
    props.setLook(wlId);
    fldId = new FormData();
    fldId.left = new FormAttachment(0, 0);
    fldId.top = new FormAttachment(wnConnection, margin * 2);
    fldId.right = new FormAttachment(middle, -margin);
    wlId.setLayoutData(fldId);
    wId = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wId);
    fdId = new FormData();
    fdId.left = new FormAttachment(middle, 0);
    fdId.top = new FormAttachment(wnConnection, margin * 2);
    fdId.right = new FormAttachment(100, 0);
    wId.setLayoutData(fdId);

    // Name line
    wlName = new Label(shell, SWT.RIGHT);
    wlName.setText(BaseMessages.getString(PKG, "RepositoryDialog.Label.Name")); //$NON-NLS-1$
    props.setLook(wlName);
    fdlName = new FormData();
    fdlName.left = new FormAttachment(0, 0);
    fdlName.top = new FormAttachment(wId, margin);
    fdlName.right = new FormAttachment(middle, -margin);
    wlName.setLayoutData(fdlName);
    wName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wName);
    fdName = new FormData();
    fdName.left = new FormAttachment(middle, 0);
    fdName.top = new FormAttachment(wId, margin);
    fdName.right = new FormAttachment(100, 0);
    wName.setLayoutData(fdName);

    wOK = new Button(shell, SWT.PUSH);
    wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
    lsOK = new Listener() {
      public void handleEvent(Event e) {
        ok();
      }
    };
    wOK.addListener(SWT.Selection, lsOK);

    wCreate = new Button(shell, SWT.PUSH);
    wCreate.setText(BaseMessages.getString(PKG, "RepositoryDialog.Button.CreateOrUpgrade")); //$NON-NLS-1$
    lsCreate = new Listener() {
      public void handleEvent(Event e) {
        create();
      }
    };
    wCreate.addListener(SWT.Selection, lsCreate);

    wDrop = new Button(shell, SWT.PUSH);
    wDrop.setText(BaseMessages.getString(PKG, "RepositoryDialog.Button.Remove")); //$NON-NLS-1$
    lsDrop = new Listener() {
      public void handleEvent(Event e) {
        drop();
      }
    };
    wDrop.addListener(SWT.Selection, lsDrop);

    wCancel = new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$
    lsCancel = new Listener() {
      public void handleEvent(Event e) {
        cancel();
      }
    };
    wCancel.addListener(SWT.Selection, lsCancel);

    BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCreate, wDrop, wCancel }, margin, wName);
    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener(new ShellAdapter() {
      public void shellClosed(ShellEvent e) {
        cancel();
      }
    });

    getData();

    BaseStepDialog.setSize(shell);

    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }
    return input;
  }

  private DatabaseDialog getDatabaseDialog() {
    if (databaseDialog != null) {
      return databaseDialog;
    }
    databaseDialog = new DatabaseDialog(shell);
    return databaseDialog;
  }

  public void dispose() {
    props.setScreen(new WindowProperty(shell));
    shell.dispose();
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    if (input.getName() != null)
      wId.setText(input.getName());
    if (input.getDescription() != null)
      wName.setText(input.getDescription());
    if (input.getConnection() != null)
      wConnection.setText(input.getConnection().getName());
  }

  private void cancel() {
    input = null;
    dispose();
  }

  private void getInfo(KettleDatabaseRepositoryMeta info) {
    info.setName(wId.getText());
    info.setDescription(wName.getText());

    int idx = wConnection.getSelectionIndex();
    if (idx >= 0) {
      DatabaseMeta dbinfo = repositories.getDatabase(idx);
      info.setConnection(dbinfo);
    } else {
      info.setConnection(null);
    }
  }

  private void ok() {
    getInfo(input);
    if (input.getConnection() != null) {
      if (input.getName() != null && input.getName().length() > 0) {
        if (input.getDescription() != null && input.getDescription().length() > 0) {
          // If MODE is ADD then check if the repository name does not exist in the repository list then close this dialog
          // If MODE is EDIT then check if the repository name is the same as before if not check if the new name does
          // not exist in the repository. Otherwise return true to this method, which will mean that repository already exist
          if(mode == MODE.ADD) {
            if(masterRepositoriesMeta.searchRepository(input.getName()) == null) {
              dispose();            
            } else {
              displayRepositoryAlreadyExistMessage(input.getName());
            }
          } else {
            if(masterRepositoryName.equals(input.getName())) {
              dispose();
            } else if(masterRepositoriesMeta.searchRepository(input.getName()) == null) {
              dispose();
            } else {
              displayRepositoryAlreadyExistMessage(input.getName());
            }
          }
        } else {
          MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
          box.setMessage(BaseMessages.getString(PKG, "RepositoryDialog.Dialog.ErrorNoName.Message")); //$NON-NLS-1$
          box.setText(BaseMessages.getString(PKG, "RepositoryDialog.Dialog.Error.Title")); //$NON-NLS-1$
          box.open();
        }
      } else {
        MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
        box.setMessage(BaseMessages.getString(PKG, "RepositoryDialog.Dialog.ErrorNoId.Message")); //$NON-NLS-1$
        box.setText(BaseMessages.getString(PKG, "RepositoryDialog.Dialog.Error.Title")); //$NON-NLS-1$
        box.open();        
      }
    } else {
      MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
      box.setMessage(BaseMessages.getString(PKG, "RepositoryDialog.Dialog.ErrorNoConnection.Message")); //$NON-NLS-1$
      box.setText(BaseMessages.getString(PKG, "RepositoryDialog.Dialog.Error.Title")); //$NON-NLS-1$
      box.open();      
    }
  }

  private void fillConnections() {
    wConnection.removeAll();

    // Fill in the available connections...
    for (int i = 0; i < repositories.nrDatabases(); i++) {
      wConnection.add(repositories.getDatabase(i).getName());
    }
  }

  private void create() {
    System.out.println("Loading repository info..."); //$NON-NLS-1$

    KettleDatabaseRepositoryMeta repositoryMeta = new KettleDatabaseRepositoryMeta();
    getInfo(repositoryMeta);

    if (repositoryMeta.getConnection() != null) {
      if (repositoryMeta.getConnection().getAccessType() == DatabaseMeta.TYPE_ACCESS_ODBC) {
        // Show a warning: using ODBC is not always the best choice ;-)
        System.out.println("Show ODBC warning..."); //$NON-NLS-1$

        MessageBox qmb = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
        qmb
            .setMessage(BaseMessages
                .getString(PKG, "RepositoryDialog.Dialog.ODBCIsNotSafe.Message", Const.CR, Const.CR)); //$NON-NLS-1$ //$NON-NLS-2$
        qmb.setText(BaseMessages.getString(PKG, "RepositoryDialog.Dialog.ODBCIsNotSafe.Title")); //$NON-NLS-1$
        int answer = qmb.open();
        if (answer != SWT.YES) {
          return; // Don't continue
        }

      }

      try {
        System.out.println("Allocating repository..."); //$NON-NLS-1$

        KettleDatabaseRepository rep = (KettleDatabaseRepository) PluginRegistry.getInstance().loadClass(
            RepositoryPluginType.class, repositoryMeta, Repository.class);
        rep.init(repositoryMeta);

        System.out.println("Connecting to database for repository creation..."); //$NON-NLS-1$
        rep.connectionDelegate.connect(true, true);
        boolean upgrade = false;
        String cu = BaseMessages.getString(PKG, "RepositoryDialog.Dialog.CreateUpgrade.Create"); //$NON-NLS-1$

        try {
          String userTableName = rep.getDatabaseMeta().quoteField(KettleDatabaseRepository.TABLE_R_USER);
          upgrade = rep.getDatabase().checkTableExists(userTableName); //$NON-NLS-1$
          if (upgrade)
            cu = BaseMessages.getString(PKG, "RepositoryDialog.Dialog.CreateUpgrade.Upgrade"); //$NON-NLS-1$
        } catch (KettleDatabaseException dbe) {
          // Roll back the connection: this is required for certain databases like PGSQL
          // Otherwise we can't execute any other DDL statement.
          //
          rep.rollback();

          // Don't show an error anymore, just go ahead and propose to create the repository!
        }

        MessageBox qmb = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
        qmb
            .setMessage(BaseMessages.getString(PKG, "RepositoryDialog.Dialog.CreateUpgrade.Message1") + cu + BaseMessages.getString(PKG, "RepositoryDialog.Dialog.CreateUpgrade.Message2")); //$NON-NLS-1$ //$NON-NLS-2$
        qmb.setText(BaseMessages.getString(PKG, "RepositoryDialog.Dialog.CreateUpgrade.Title")); //$NON-NLS-1$
        int answer = qmb.open();

        if (answer == SWT.YES) {
          boolean goAhead = !upgrade;

          if (!goAhead) {
            EnterPasswordDialog etd = new EnterPasswordDialog(
                shell,
                BaseMessages.getString(PKG, "RepositoryDialog.Dialog.EnterPassword.Title"), BaseMessages.getString(PKG, "RepositoryDialog.Dialog.EnterPassword.Message"), ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            etd.setModal();
            String pwd = etd.open();
            if (pwd != null) {
              try {
                // authenticate as admin before upgrade
                // disconnect before connecting, we connected above already
                // 
                rep.disconnect();
                rep.connect("admin", pwd, true);
                goAhead = true;
              } catch (KettleException e) {
                new ErrorDialog(
                    shell,
                    BaseMessages.getString(PKG, "RepositoryDialog.Dialog.UnableToVerifyUser.Title"), BaseMessages.getString(PKG, "RepositoryDialog.Dialog.UnableToVerifyUser.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
              }
            }
          }

          if (goAhead) {
            System.out
                .println(BaseMessages.getString(PKG, "RepositoryDialog.Dialog.TryingToUpgradeRepository.Message1") + cu + BaseMessages.getString(PKG, "RepositoryDialog.Dialog.TryingToUpgradeRepository.Message2")); //$NON-NLS-1$ //$NON-NLS-2$
            UpgradeRepositoryProgressDialog urpd = new UpgradeRepositoryProgressDialog(shell, rep, upgrade);
            if (urpd.open()) {
              if (urpd.isDryRun()) {
                StringBuffer sql = new StringBuffer();
                sql.append("-- Repository creation/upgrade DDL: ").append(Const.CR);
                sql.append("--").append(Const.CR);
                sql.append("-- Nothing was created nor modified in the target repository database.").append(Const.CR);
                sql.append("-- Hit the OK button to execute the generated SQL or Close to reject the changes.").append(
                    Const.CR);
                sql.append("-- Please note that it is possible to change/edit the generated SQL before execution.")
                    .append(Const.CR);
                sql.append("--").append(Const.CR);
                for (String statement : urpd.getGeneratedStatements()) {
                  if (statement.endsWith(";")) {
                    sql.append(statement).append(Const.CR);
                  } else {
                    sql.append(statement).append(";").append(Const.CR).append(Const.CR);
                  }
                }
                SQLEditor editor = new SQLEditor(shell, SWT.NONE, rep.getDatabaseMeta(), DBCache.getInstance(), sql
                    .toString());
                editor.open();

              } else {
                MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
                mb
                    .setMessage(BaseMessages.getString(PKG, "RepositoryDialog.Dialog.UpgradeFinished.Message1") + cu + BaseMessages.getString(PKG, "RepositoryDialog.Dialog.UpgradeFinished.Message2")); //$NON-NLS-1$ //$NON-NLS-2$
                mb.setText(BaseMessages.getString(PKG, "RepositoryDialog.Dialog.UpgradeFinished.Title")); //$NON-NLS-1$
                mb.open();
              }
            }
          }
        }

        rep.disconnect();
      } catch (KettleException ke) {
        new ErrorDialog(
            shell,
            BaseMessages.getString(PKG, "RepositoryDialog.Dialog.UnableToConnectToUpgrade.Title"), BaseMessages.getString(PKG, "RepositoryDialog.Dialog.UnableToConnectToUpgrade.Message") + Const.CR, ke); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      }
    } else {
      MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
      mb.setMessage(BaseMessages.getString(PKG, "RepositoryDialog.Dialog.FirstCreateAValidConnection.Message")); //$NON-NLS-1$
      mb.setText(BaseMessages.getString(PKG, "RepositoryDialog.Dialog.FirstCreateAValidConnection.Title")); //$NON-NLS-1$
      mb.open();
    }
  }

  private void drop() {
    KettleDatabaseRepositoryMeta repositoryMeta = new KettleDatabaseRepositoryMeta();
    getInfo(repositoryMeta);

    try {
      KettleDatabaseRepository rep = (KettleDatabaseRepository) PluginRegistry.getInstance().loadClass(
          RepositoryPluginType.class, repositoryMeta, Repository.class);
      rep.init(repositoryMeta);

      MessageBox qmb = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
      qmb.setMessage(BaseMessages.getString(PKG, "RepositoryDialog.Dialog.ConfirmRemovalOfRepository.Message")); //$NON-NLS-1$
      qmb.setText(BaseMessages.getString(PKG, "RepositoryDialog.Dialog.ConfirmRemovalOfRepository.Title")); //$NON-NLS-1$
      int answer = qmb.open();

      if (answer == SWT.YES) {
        EnterPasswordDialog etd = new EnterPasswordDialog(
            shell,
            BaseMessages.getString(PKG, "RepositoryDialog.Dialog.AskAdminPassword.Title"), BaseMessages.getString(PKG, "RepositoryDialog.Dialog.AskAdminPassword.Message"), ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String pwd = etd.open();
        if (pwd != null) {
          try {
            // Connect as admin user
            rep.connect("admin", pwd);
            try {
              rep.dropRepositorySchema();

              MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
              mb.setMessage(BaseMessages.getString(PKG, "RepositoryDialog.Dialog.RemovedRepositoryTables.Message")); //$NON-NLS-1$
              mb.setText(BaseMessages.getString(PKG, "RepositoryDialog.Dialog.RemovedRepositoryTables.Title")); //$NON-NLS-1$
              mb.open();
            } catch (KettleDatabaseException dbe) {
              MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
              mb
                  .setMessage(BaseMessages.getString(PKG, "RepositoryDialog.Dialog.UnableToRemoveRepository.Message") + Const.CR + dbe.getMessage()); //$NON-NLS-1$
              mb.setText(BaseMessages.getString(PKG, "RepositoryDialog.Dialog.UnableToRemoveRepository.Title")); //$NON-NLS-1$
              mb.open();
            }
          } catch (KettleException e) {
            new ErrorDialog(
                shell,
                BaseMessages.getString(PKG, "RepositoryDialog.Dialog.UnableToVerifyAdminUser.Title"), BaseMessages.getString(PKG, "RepositoryDialog.Dialog.UnableToVerifyAdminUser.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
          } finally {
            rep.disconnect();
          }
        }
      }
    } catch (KettleException ke) {
      new ErrorDialog(
          shell,
          BaseMessages.getString(PKG, "RepositoryDialog.Dialog.NoRepositoryFoundOnConnection.Title"), BaseMessages.getString(PKG, "RepositoryDialog.Dialog.NoRepositoryFoundOnConnection.Message"), ke); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
  }
  
  private void  displayRepositoryAlreadyExistMessage(String name) {
    MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
    box.setMessage(BaseMessages.getString(PKG, "RepositoryDialog.Dialog.ErrorIdExist.Message", name)); //$NON-NLS-1$
    box.setText(BaseMessages.getString(PKG, "RepositoryDialog.Dialog.Error.Title")); //$NON-NLS-1$
    box.open();                   
  }
}
