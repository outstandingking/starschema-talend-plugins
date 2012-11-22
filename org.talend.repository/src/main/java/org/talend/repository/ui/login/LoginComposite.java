// ============================================================================
//
// Copyright (C) 2006-2012 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.ui.login;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.internal.dialogs.EventLoopProgressMonitor;
import org.eclipse.ui.internal.wizards.datatransfer.TarException;
import org.osgi.framework.Bundle;
import org.osgi.service.prefs.BackingStoreException;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.exception.WarningException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.dialogs.ProgressDialog;
import org.talend.commons.utils.PasswordHelper;
import org.talend.commons.utils.system.EnvironmentUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.general.ConnectionBean;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.User;
import org.talend.core.model.repository.SVNConstant;
import org.talend.core.prefs.PreferenceManipulator;
import org.talend.core.repository.ILoginConnectionService;
import org.talend.core.repository.LoginConnectionManager;
import org.talend.core.repository.model.IRepositoryFactory;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.model.RepositoryFactoryProvider;
import org.talend.core.tis.ICoreTisService;
import org.talend.core.ui.ISVNProviderService;
import org.talend.core.ui.TalendBrowserLaunchHelper;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.core.updatesite.IUpdateSiteBean;
import org.talend.json.JSONException;
import org.talend.json.JSONObject;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.ui.ERepositoryImages;
import org.talend.repository.ui.actions.importproject.DeleteProjectsAsAction;
import org.talend.repository.ui.actions.importproject.DemoProjectBean;
import org.talend.repository.ui.actions.importproject.EDemoProjectFileType;
import org.talend.repository.ui.actions.importproject.ImportDemoProjectAction;
import org.talend.repository.ui.actions.importproject.ImportProjectAsAction;
import org.talend.repository.ui.actions.importproject.ImportProjectsUtilities;
import org.talend.repository.ui.login.connections.ConnectionUserPerReader;
import org.talend.repository.ui.login.connections.ConnectionsDialog;
import org.talend.repository.ui.login.sandboxProject.CreateSandboxProjectDialog;
import org.talend.repository.ui.wizards.newproject.NewProjectWizard;

/**
 * labe Composite login.<br/>
 * 
 * $Id: /talend/tos/trunk/org.talend.repository/src/main/java/org/talend/repository/ui/login/LoginComposite.java 24167
 * 2009-04-28T09:55:53.574018Z wchen $
 * 
 */
public class LoginComposite extends Composite {

    private static final String DEFAULT_PROJECT_NAME = "ProjectName"; //$NON-NLS-1$

    private static final String LOCAL = "local"; //$NON-NLS-1$

    public static final String FONT_ARIAL = "Arial"; //$NON-NLS-1$

    private static final int VERTICAL_SPACE = 0;

    private static final int HORIZONTAL_SPACE = 5;

    private static final int HORIZONTAL_TWO_SPACE = 10;

    private static final int HORIZONTAL_THREE_SPACE = 15;

    private static final int HORIZONTAL_FOUR_SPACE = 20;

    private static final int LEFTSPACE = 80;

    /**
     * Colors used for Remote Object background when enabled.
     */
    public static final Color WHITE_COLOR = new Color(null, 255, 255, 255);

    public static final Color GREY_COLOR = new Color(null, 215, 215, 215);

    public static final Color YELLOW_GREEN_COLOR = new Color(null, 159, 181, 38);// 143, 163, 35

    public static final Color YELLOW_COLOR = new Color(null, 255, 173, 37);// 254, 182, 84

    private static final Color RED_COLOR = new Color(null, new RGB(240, 0, 0));// 255

    private static final Image LOGIN_CRITICAL_IMAGE = ImageProvider.getImage(ERepositoryImages.LOGIN_CRITICAL_ICON);

    private static final Image LOGIN_WARNING_IMAGE = ImageProvider.getImage(ERepositoryImages.LOGIN_WARNING_ICON);

    private static final Image LOGIN_CORRECT_IMAGE = ImageProvider.getImage(ERepositoryImages.LOGIN_CORRECT_ICON);

    private FormToolkit toolkit;

    private ComboViewer connectionsViewer;

    private LoginDialog dialog;

    private Text user;

    private Text passwordText;

    private ComboViewer projectViewer;

    private ComboViewer branchesViewer;

    private Label svnBranchLabel;

    private Button fillProjectsBtn;

    private Button createProjectBtn;

    private Button openProjectBtn;

    private Button advanced;

    private Button manageConnectionsButton;

    private Button manageProjectsButton;

    private Button manageProjectsButtonTemp;

    private Label statusLabel;

    private ComboViewer manageViewer;

    private Text importText;

    private ComboViewer importCombo;

    private Text projectText;

    private Label manageProjectLabel1;

    private String lastWarnings;

    private Button restartBut;

    private Button updateBtn;

    public List<ConnectionBean> storedConnections = null;

    private String lastConnection = null;

    public static boolean isRestart = false;

    private boolean inuse = false;

    private Label iconLabel = null;

    private Label onIconLabel = null;

    private ConnectionUserPerReader perReader = null;

    private Composite repositoryComposite = null;

    private Composite userEmailComposite = null;

    private Composite separatorComposite = null;

    private Composite tosActionComposite = null;

    private Composite tosProjectComposite = null;

    private Composite tosWelcomeComposite = null;

    private Composite tisRepositoryComposite = null;

    private Composite colorComposite = null;

    private Composite formBody = null;

    private Label passwordLabel = null;

    private Composite passwordComposite = null;

    private Composite tisBlankCompoiste = null;

    private Hyperlink repositoryHyperlink = null;

    IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(IBrandingService.class);

    private ConnectionBean beforeConnBean;

    private ConnectionBean firstConnBean;

    private List<IUpdateSiteBean> updateSiteToInstall = new ArrayList<IUpdateSiteBean>();

    // only for test
    // private static final String ARCHIVA_URL = "http://192.168.0.58:8080";

    private static final String ARCHIVA_SERVICES_SEGMENT = "/restServices/archivaServices/"; //$NON-NLS-N$ //$NON-NLS-1$ //$NON-NLS-1$

    private static final String ARCHIVA_SERVICES_URL_KEY = "archivaUrl"; //$NON-NLS-N$ //$NON-NLS-1$ //$NON-NLS-1$

    private static final String ARCHIVA_REPOSITORY_KEY = "repository"; //$NON-NLS-N$ //$NON-NLS-1$ //$NON-NLS-1$

    private static final String ARCHIVA_USER = "username"; //$NON-NLS-N$ //$NON-NLS-1$ //$NON-NLS-1$

    private static final String ARCHIVA_USER_PWD = "password"; //$NON-NLS-N$ //$NON-NLS-1$ //$NON-NLS-1$

    private boolean afterUpdate = false;

    private TOSLoginComposite tosLoginComposite;

    private StackLayout stackLayout;

    private Composite parent;

    private String oldPath;

    private static Logger log = Logger.getLogger(LoginComposite.class);

    /**
     * Constructs a new LoginComposite.
     * 
     * @param parent Parent component.
     * @param style Style bits.
     */
    public LoginComposite(Composite parent, int style, LoginDialog dialog, boolean inuse, TOSLoginComposite tosLoginComposite,
            StackLayout stackLayout) {
        super(parent, style);
        this.parent = parent;
        this.dialog = dialog;
        this.inuse = inuse;
        this.tosLoginComposite = tosLoginComposite;
        this.stackLayout = stackLayout;

        perReader = ConnectionUserPerReader.getInstance();
        try {
            Browser browser = new Browser(parent, SWT.BORDER);
            System.setProperty("USE_BROWSER", "yes"); //$NON-NLS-1$ //$NON-NLS-2$
            browser.dispose();
        } catch (Throwable t) {
            System.setProperty("USE_BROWSER", "no"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        setLayout(layout);
        toolkit = new FormToolkit(this.getDisplay());
        Form form = toolkit.createForm(this);
        formBody = form.getBody();
        formBody.setBackgroundMode(SWT.INHERIT_DEFAULT);
        GridData formBodyGd = new GridData(GridData.FILL_BOTH);
        form.setLayoutData(formBodyGd);
        GridLayout createLayout = createLayout(1);
        createLayout.verticalSpacing = 0;
        createLayout.horizontalSpacing = 0;
        createLayout.marginHeight = 0;
        createLayout.marginWidth = 0;
        formBody.setLayout(createLayout);

        if (!PluginChecker.isSVNProviderPluginLoaded()) {
            createTosRepositoryArea(formBody);
            // createSeparator(formBody);
            createTosActionArea(formBody);
            createTosProjectArea(formBody);
        } else {
            createTisRepositoryArea(formBody);
            createSeparator(formBody);
            createTisActionArea(formBody);
            createTisProjectArea(formBody);
            createTisBlankArea(formBody);
        }
        createTosWelcomArea(formBody);
        // createRestartArea(formBody);

        readConnectionData();
        recordFirstConnection();
        fillContents();
        addListeners();
        if (inuse) {
            manageViewer.getControl().setEnabled(false);
            manageProjectsButton.setEnabled(false);
            openProjectBtn.setEnabled(false);
            // warningLabel.setVisible(true);
            restartBut.setVisible(false);
        }
        if (PluginChecker.isSVNProviderPluginLoaded()) {
            manageViewer.getControl().setEnabled(true);
            manageProjectsButton.setEnabled(true);
        }
        try {
            setStatusArea();
            log.info("validate updatesite..."); //$NON-NLS-N$ //$NON-NLS-1$ //$NON-NLS-1$
            validateUpdate();
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            log.error(e);
        } catch (JSONException e) {
            ExceptionHandler.process(e);
            log.error(e);
        }
        displayPasswordComposite();
        if (!PluginChecker.isSVNProviderPluginLoaded()) {
            initConnection();
        }
    }

    private void initConnection() {
        if (storedConnections == null || storedConnections.size() == 0) {
            getConnection();
        }
    }

    private void displayPasswordComposite() {
        if (PluginChecker.isSVNProviderPluginLoaded()) {
            if (getConnection() != null) {
                boolean local = RepositoryConstants.REPOSITORY_LOCAL_ID.equals(getConnection().getRepositoryId());
                if (local) {
                    GridData layoutData = (GridData) passwordComposite.getLayoutData();
                    passwordComposite.setVisible(false);
                    layoutData.exclude = true;

                    GridData layoutData2 = (GridData) tisBlankCompoiste.getLayoutData();
                    tisBlankCompoiste.setVisible(true);
                    layoutData2.exclude = false;
                    formBody.layout();
                } else {
                    GridData layoutData = (GridData) passwordComposite.getLayoutData();
                    passwordComposite.setVisible(true);
                    layoutData.exclude = false;

                    GridData layoutData2 = (GridData) tisBlankCompoiste.getLayoutData();
                    tisBlankCompoiste.setVisible(false);
                    layoutData2.exclude = true;
                    formBody.layout();
                }
            } else {
                GridData layoutData = (GridData) passwordComposite.getLayoutData();
                passwordComposite.setVisible(true);
                layoutData.exclude = false;

                GridData layoutData2 = (GridData) tisBlankCompoiste.getLayoutData();
                tisBlankCompoiste.setVisible(false);
                layoutData2.exclude = true;
                formBody.layout();
            }
            getShell().pack();
        }
    }

    private void setManageViewer() {
        PreferenceManipulator prefManipulator = new PreferenceManipulator(CorePlugin.getDefault().getPreferenceStore());
        if (prefManipulator.getBoolean(ImportDemoProjectAction.DEMO_ALREADY_IMPORTED)) {
            manageViewer.setSelection(new StructuredSelection(new Object[] { manageViewer.getElementAt(1) }));
        } else {
            manageViewer.setSelection(new StructuredSelection(new Object[] { manageViewer.getElementAt(0) }));
        }
    }

    private ManageItem[] getManageElements() {
        boolean tis = PluginChecker.isSVNProviderPluginLoaded();
        boolean isSVNProviderPluginLoadedRemote = isSVNProviderPluginLoadedRemote();
        boolean hasConnection = (getConnection() != null);
        List<ManageItem> toReturn = new ArrayList<ManageItem>();

        IBrandingService service = (IBrandingService) GlobalServiceRegister.getDefault().getService(IBrandingService.class);
        boolean usesDemoProjects = service.getBrandingConfiguration().isUseDemoProjects();

        if (hasConnection && (!tis || !isSVNProviderPluginLoadedRemote)) { // demo for Tos and Tis_Local
            if (usesDemoProjects) {
                toReturn.add(new ManageItem(ImportDemoProjectAction.getInstance().getToolTipText()) {

                    @Override
                    public void run() {
                        importDemoProject();
                    }
                });
            }
            toReturn.add(new ManageItem(Messages.getString("LoginComposite.buttons.newProject.desc")) { //$NON-NLS-1$

                @Override
                public void run() {
                    createNewProject();
                }

            });

            toReturn.add(new ManageItem(Messages.getString("LoginComposite.buttons.importProject.desc")) { //$NON-NLS-1$

                @Override
                public void run() {
                    importProjects();
                }

            });
        }
        // delete project for Tos and Tis_Remote
        toReturn.add(new ManageItem(Messages.getString("LoginComposite.buttons.deleteProject.desc")) { //$NON-NLS-1$

            @Override
            public void run() {
                deleteProject();
            }

        });

        if (tis) { // Sendbox for Tis_Remote
            toReturn.add(new ManageItem(Messages.getString("LoginComposite.buttons.createSandboxProject.desc")) { //$NON-NLS-1$

                @Override
                public void run() {
                    createSendboxProject();
                }

            });
        }
        return toReturn.toArray(new ManageItem[] {});
    }

    // create for feature 15083
    private void createTosRepositoryArea(Composite parent) {
        // local repository

        repositoryComposite = toolkit.createComposite(parent);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 30;
        repositoryComposite.setLayoutData(gd);
        repositoryComposite.setLayout(new FormLayout());
        repositoryComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
        repositoryComposite.setBackground(parent.getBackground());
        // repositoryComposite.setBackground(GREY_COLOR);

        Label welcomeLabel = toolkit.createLabel(repositoryComposite, Messages.getString("LoginComposite.welcomeTitle")); //$NON-NLS-1$
        welcomeLabel.setBackground(repositoryComposite.getBackground());
        GC gc = new GC(welcomeLabel);
        Point labelSize = gc.stringExtent(Messages.getString("LoginComposite.welcomeTitle")); //$NON-NLS-1$
        gc.dispose();

        FormData welcomeLabelFormData = new FormData();
        welcomeLabelFormData.top = new FormAttachment(0, 7);
        welcomeLabelFormData.left = new FormAttachment(0, HORIZONTAL_TWO_SPACE);
        welcomeLabelFormData.right = new FormAttachment(0, HORIZONTAL_TWO_SPACE + labelSize.x);
        welcomeLabelFormData.bottom = new FormAttachment(0, 7 + labelSize.y);
        welcomeLabel.setLayoutData(welcomeLabelFormData);

        passwordText = toolkit.createText(repositoryComposite, null, SWT.PASSWORD | SWT.BORDER);
        passwordText.setVisible(false);

        // repository
        Label repositoryLabel = toolkit.createLabel(repositoryComposite, Messages.getString("LoginComposite.connections")); //$NON-NLS-1$
        repositoryLabel.setBackground(repositoryComposite.getBackground());
        FormData formData2 = new FormData();
        formData2.top = new FormAttachment(0, 17);
        formData2.left = new FormAttachment(0, HORIZONTAL_TWO_SPACE);
        formData2.right = new FormAttachment(0, LEFTSPACE);
        formData2.bottom = new FormAttachment(100, -HORIZONTAL_SPACE);
        repositoryLabel.setLayoutData(formData2);
        repositoryLabel.setVisible(false);

        connectionsViewer = new ComboViewer(repositoryComposite, SWT.BORDER | SWT.READ_ONLY);
        connectionsViewer.setContentProvider(new ArrayContentProvider());
        connectionsViewer.setLabelProvider(new ConnectionLabelProvider());
        formData2 = new FormData();
        formData2.top = new FormAttachment(0, HORIZONTAL_THREE_SPACE);
        formData2.left = new FormAttachment(repositoryLabel, HORIZONTAL_SPACE);
        formData2.right = new FormAttachment(50, 0);
        connectionsViewer.getControl().setLayoutData(formData2);
        connectionsViewer.getCombo().setVisible(false);

        repositoryHyperlink = toolkit.createHyperlink(repositoryComposite,
                Messages.getString("LoginComposite.sharedRepositoryMessage"), SWT.NONE); //$NON-NLS-1$
        repositoryHyperlink.setBackground(repositoryComposite.getBackground());
        formData2 = new FormData();
        formData2.top = new FormAttachment(0, HORIZONTAL_THREE_SPACE);
        formData2.left = new FormAttachment(connectionsViewer.getControl(), HORIZONTAL_FOUR_SPACE);
        formData2.right = new FormAttachment(100, -HORIZONTAL_TWO_SPACE);
        repositoryHyperlink.setLayoutData(formData2);
        repositoryHyperlink.addHyperlinkListener(new HyperlinkAdapter() {

            @Override
            public void linkActivated(HyperlinkEvent e) {

                String url = "http://www.talend.com/products-data-integration/sharedRepository.php"; //$NON-NLS-1$
                TalendBrowserLaunchHelper.openURL(url);
            }
        });
        repositoryHyperlink.setVisible(false);

        // user E_mail
        userEmailComposite = toolkit.createComposite(parent);
        userEmailComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
        // userEmailComposite.setBackground(GREY_COLOR);
        userEmailComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        userEmailComposite.setLayout(new FormLayout());

        Label detailLabel = toolkit.createLabel(userEmailComposite, Messages.getString("LoginComposite.detailMessage")); //$NON-NLS-1$
        detailLabel.setBackground(userEmailComposite.getBackground());
        FormData detailLabelFormData = new FormData();
        detailLabelFormData.top = new FormAttachment(0, 0);
        detailLabelFormData.left = new FormAttachment(0, HORIZONTAL_TWO_SPACE);
        if (Platform.getOS().equals(Platform.OS_WIN32)) {
            detailLabelFormData.right = new FormAttachment(0, 440);
        } else if (Platform.getOS().equals(Platform.OS_LINUX)) {
            detailLabelFormData.right = new FormAttachment(0, 500);
        } else {
            detailLabelFormData.right = new FormAttachment(0, 500);
        }
        detailLabelFormData.bottom = new FormAttachment(100, 0);
        detailLabel.setLayoutData(detailLabelFormData);

        Label userLabel = toolkit.createLabel(userEmailComposite, Messages.getString("LoginComposite.emailTitle")); //$NON-NLS-1$
        userLabel.setBackground(userEmailComposite.getBackground());
        FormData formData = new FormData();
        formData.top = new FormAttachment(0, HORIZONTAL_TWO_SPACE);
        formData.left = new FormAttachment(0, HORIZONTAL_TWO_SPACE);
        formData.right = new FormAttachment(0, LEFTSPACE);
        formData.bottom = new FormAttachment(100, -HORIZONTAL_TWO_SPACE);
        userLabel.setLayoutData(formData);
        userLabel.setVisible(false);

        user = toolkit.createText(userEmailComposite, "", SWT.BORDER); //$NON-NLS-1$
        user.setEditable(false);
        user.setEnabled(false);
        formData = new FormData();
        formData.top = new FormAttachment(0, HORIZONTAL_TWO_SPACE);
        formData.left = new FormAttachment(userLabel, HORIZONTAL_SPACE);
        formData.right = new FormAttachment(90, -HORIZONTAL_TWO_SPACE);
        user.setLayoutData(formData);
        user.setVisible(false);

        manageConnectionsButton = toolkit.createButton(userEmailComposite, null, SWT.PUSH);
        manageConnectionsButton.setBackground(userEmailComposite.getBackground());
        manageConnectionsButton.setToolTipText(Messages.getString("LoginComposite.manageConnectionsToolTipHint")); //$NON-NLS-1$
        manageConnectionsButton.setImage(ImageProvider.getImage(EImage.THREE_DOTS_ICON));
        formData = new FormData();
        formData.top = new FormAttachment(0, 7);
        formData.left = new FormAttachment(user, HORIZONTAL_SPACE);
        formData.right = new FormAttachment(100, -HORIZONTAL_TWO_SPACE);
        manageConnectionsButton.setLayoutData(formData);
        manageConnectionsButton.setVisible(false);

    }

    private void createSeparator(Composite parent) {
        separatorComposite = toolkit.createComposite(parent);
        separatorComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
        separatorComposite.setBackground(GREY_COLOR);
        separatorComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout();
        layout.marginTop = 0; // HORIZONTAL_TWO_SPACE;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.marginBottom = 0;
        separatorComposite.setLayout(layout);
        Label separatorLabel = toolkit.createSeparator(separatorComposite, SWT.HORIZONTAL | SWT.COLOR_DARK_GRAY);
        separatorLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        separatorLabel.setBackground(GREY_COLOR);
    }

    private void createTisActionArea(Composite parent) {
        tosActionComposite = toolkit.createComposite(parent);
        tosActionComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        tosActionComposite.setLayout(new FormLayout());
        tosActionComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
        tosActionComposite.setBackground(GREY_COLOR);

        FormData data;

        // go
        manageProjectsButton = toolkit.createButton(tosActionComposite, null, SWT.PUSH);
        manageProjectsButton.setBackground(tosActionComposite.getBackground());
        manageProjectsButton.setText(Messages.getString("LoginComposite.manageProjectsButton")); //$NON-NLS-1$

        manageViewer = new ComboViewer(tosActionComposite, SWT.BORDER | SWT.READ_ONLY);
        manageViewer.setContentProvider(new ArrayContentProvider());
        manageViewer.setInput(getManageElements());

        data = new FormData();
        data.top = new FormAttachment(0, HORIZONTAL_THREE_SPACE);
        data.right = new FormAttachment(100, -HORIZONTAL_TWO_SPACE);
        data.bottom = new FormAttachment(100, -HORIZONTAL_FOUR_SPACE);
        manageProjectsButton.setLayoutData(data);

        manageProjectLabel1 = toolkit.createLabel(tosActionComposite, Messages.getString("LoginComposite.actionTitle")); //$NON-NLS-1$
        manageProjectLabel1.setBackground(tosActionComposite.getBackground());
        data = new FormData();
        data.left = new FormAttachment(0, HORIZONTAL_TWO_SPACE);
        data.right = new FormAttachment(0, LEFTSPACE);
        data.bottom = new FormAttachment(manageProjectsButton, HORIZONTAL_FOUR_SPACE, SWT.CENTER);
        manageProjectLabel1.setLayoutData(data);

        // data for managerViewer
        data = new FormData();
        data.left = new FormAttachment(manageProjectLabel1, HORIZONTAL_SPACE);
        data.bottom = new FormAttachment(manageProjectLabel1, HORIZONTAL_FOUR_SPACE, SWT.CENTER);
        Point pbtnPoint = manageProjectsButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        data.right = new FormAttachment(100, -HORIZONTAL_THREE_SPACE - pbtnPoint.x);
        manageViewer.getControl().setLayoutData(data);
    }

    private void createTosActionArea(Composite parent) {
        tosActionComposite = toolkit.createComposite(parent);
        tosActionComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        tosActionComposite.setLayout(new FormLayout());
        tosActionComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
        repositoryComposite.setBackground(parent.getBackground());
        // tosActionComposite.setBackground(GREY_COLOR);

        FormData data;

        // go
        manageProjectsButton = toolkit.createButton(tosActionComposite, null, SWT.PUSH);
        manageProjectsButton.setBackground(tosActionComposite.getBackground());
        manageProjectsButton.setText(Messages.getString("LoginComposite.manageProjectsButton")); //$NON-NLS-1$
        manageProjectsButton.setVisible(false);

        manageViewer = new ComboViewer(tosActionComposite, SWT.BORDER | SWT.READ_ONLY);
        manageViewer.setContentProvider(new ArrayContentProvider());
        manageViewer.setInput(getManageElements());
        manageViewer.getCombo().setVisible(false);

        data = new FormData();
        data.top = new FormAttachment(0, HORIZONTAL_THREE_SPACE);
        data.right = new FormAttachment(100, -HORIZONTAL_TWO_SPACE);
        data.bottom = new FormAttachment(100, -HORIZONTAL_FOUR_SPACE);
        manageProjectsButton.setLayoutData(data);

        manageProjectLabel1 = toolkit.createLabel(tosActionComposite, Messages.getString("LoginComposite.actionTitle")); //$NON-NLS-1$
        manageProjectLabel1.setBackground(tosActionComposite.getBackground());
        data = new FormData();
        data.left = new FormAttachment(0, HORIZONTAL_TWO_SPACE);
        data.right = new FormAttachment(0, LEFTSPACE);
        data.bottom = new FormAttachment(manageProjectsButton, HORIZONTAL_FOUR_SPACE, SWT.CENTER);
        manageProjectLabel1.setLayoutData(data);
        manageProjectLabel1.setVisible(false);

        // data for managerViewer
        data = new FormData();
        data.left = new FormAttachment(manageProjectLabel1, HORIZONTAL_SPACE);
        data.bottom = new FormAttachment(manageProjectLabel1, HORIZONTAL_FOUR_SPACE, SWT.CENTER);
        Point pbtnPoint = manageProjectsButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        data.right = new FormAttachment(100, -HORIZONTAL_THREE_SPACE - pbtnPoint.x);
        manageViewer.getControl().setLayoutData(data);
        //
        manageProjectsButtonTemp = toolkit.createButton(tosActionComposite, null, SWT.PUSH);
        manageProjectsButtonTemp.setBackground(tosActionComposite.getBackground());
        manageProjectsButtonTemp.setText(Messages.getString("LoginComposite.NewImport")); //$NON-NLS-1$
        data = new FormData();
        data.top = new FormAttachment(0, HORIZONTAL_THREE_SPACE);
        data.right = new FormAttachment(90, -HORIZONTAL_TWO_SPACE);
        data.bottom = new FormAttachment(100, -HORIZONTAL_FOUR_SPACE);
        manageProjectsButtonTemp.setLayoutData(data);

        manageProjectLabel1 = toolkit.createLabel(tosActionComposite, Messages.getString("LoginComposite.selectADemoProject")); //$NON-NLS-1$
        manageProjectLabel1.setBackground(tosActionComposite.getBackground());
        GC gc = new GC(manageProjectLabel1);
        Point labelSize = gc.stringExtent(Messages.getString("LoginComposite.selectADemoProject")); //$NON-NLS-1$
        gc.dispose();
        data = new FormData();
        data.left = new FormAttachment(10, HORIZONTAL_SPACE);
        data.right = new FormAttachment(10, HORIZONTAL_TWO_SPACE + labelSize.x);
        data.bottom = new FormAttachment(manageProjectsButtonTemp, HORIZONTAL_FOUR_SPACE, SWT.CENTER);
        manageProjectLabel1.setLayoutData(data);

        // importText = toolkit.createText(tosActionComposite, "", SWT.BORDER | SWT.READ_ONLY);
        // data = new FormData();
        // data.left = new FormAttachment(manageProjectLabel1, 5, SWT.RIGHT);
        // data.bottom = new FormAttachment(manageProjectLabel1, HORIZONTAL_FOUR_SPACE, SWT.CENTER);
        // Point btPoint = manageProjectsButtonTemp.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        // data.right = new FormAttachment(100, -HORIZONTAL_THREE_SPACE - btPoint.x);
        // importText.setLayoutData(data);

        importCombo = new ComboViewer(tosActionComposite, SWT.BORDER | SWT.READ_ONLY);
        data = new FormData();
        data.left = new FormAttachment(manageProjectLabel1, 10, SWT.RIGHT);
        data.bottom = new FormAttachment(manageProjectLabel1, HORIZONTAL_FOUR_SPACE, SWT.CENTER);
        Point btPoint = manageProjectsButtonTemp.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        data.right = new FormAttachment(100, -HORIZONTAL_THREE_SPACE - btPoint.x - 50);
        importCombo.getCombo().setLayoutData(data);
        importCombo.setContentProvider(new ArrayContentProvider());
        List<DemoProjectBean> demoProjectList = ImportProjectsUtilities.getAllDemoProjects();
        for (int i = 0; i < demoProjectList.size(); i++) {
            DemoProjectBean bean = (DemoProjectBean) demoProjectList.get(i);
            importCombo.add(bean.getProjectName());
        }
        importCombo.setSelection(new StructuredSelection(new Object[] { importCombo.getElementAt(0) }));

        manageProjectsButtonTemp.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                // ImportDemoProjectAction.getInstance().setShell(getShell());
                // ImportDemoProjectAction.getInstance().run();
                // populateProjectList();
                // String newProject = ImportDemoProjectAction.getInstance().getProjectName();
                // if (newProject != null) {
                // importText.setText(newProject);
                // }

                NewImportProjectWizard newPrjWiz = new NewImportProjectWizard();
                WizardDialog newProjectDialog = new WizardDialog(getShell(), newPrjWiz);
                newProjectDialog.setTitle(Messages.getString("NewImportProjectWizard.windowTitle")); //$NON-NLS-1$
                if (newProjectDialog.open() == Window.OK) {
                    final String newName = newPrjWiz.getName().trim().replace(' ', '_');
                    final String technicalName = newPrjWiz.getTechnicalName();
                    final String demoProjName = importCombo.getCombo().getItem(importCombo.getCombo().getSelectionIndex());

                    //
                    ProgressDialog progressDialog = new ProgressDialog(getShell()) {

                        private IProgressMonitor monitorWrap;

                        @Override
                        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                            monitorWrap = new EventLoopProgressMonitor(monitor);

                            try {
                                final List<DemoProjectBean> demoProjectList = ImportProjectsUtilities.getAllDemoProjects();
                                DemoProjectBean demoProjectBean = null;
                                for (DemoProjectBean bean : demoProjectList) {
                                    if (bean.getProjectName().equals(demoProjName)) {
                                        demoProjectBean = bean;
                                        break;
                                    }
                                }
                                if (null == demoProjectBean) {
                                    throw new IOException("cannot find selected demo project"); //$NON-NLS-1$
                                }
                                String techName = demoProjectBean.getProjectName();

                                String demoFilePath = demoProjectBean.getDemoProjectFilePath();
                                EDemoProjectFileType demoProjectFileType = demoProjectBean.getDemoProjectFileType();
                                String pluginID = org.talend.resources.ResourcesPlugin.PLUGIN_ID;
                                if (techName.equals("TALENDDEMOSPERL")) { //$NON-NLS-1$
                                    pluginID = "org.talend.resources.perl"; //$NON-NLS-1$
                                } else if (techName.equals("TDQEEDEMOJAVA")) { //$NON-NLS-1$
                                    pluginID = "org.talend.datacleansing.core.ui"; //$NON-NLS-1$
                                }
                                if (demoProjectBean.getPluginId() != null) {
                                    pluginID = demoProjectBean.getPluginId();
                                }
                                Bundle bundle = Platform.getBundle(pluginID);

                                URL url = FileLocator.resolve(bundle.getEntry(demoFilePath));

                                String filePath = new Path(url.getFile()).toOSString();

                                if (demoProjectFileType.getName().equalsIgnoreCase("folder")) { //$NON-NLS-1$
                                    ImportProjectsUtilities.importProjectAs(getShell(), newName, technicalName, filePath,
                                            monitorWrap);
                                } else {// type.equalsIgnoreCase("archive")
                                    ImportProjectsUtilities.importArchiveProjectAs(getShell(), newName, technicalName, filePath,
                                            monitorWrap);
                                }

                            } catch (IOException e) {
                                throw new InvocationTargetException(e);
                            } catch (TarException e) {
                                throw new InvocationTargetException(e);
                            }

                            monitorWrap.done();
                        }
                    };

                    try {
                        progressDialog.executeProcess();
                    } catch (InvocationTargetException e1) {
                        MessageBoxExceptionHandler.process(e1.getTargetException(), getShell());
                    } catch (InterruptedException e1) {
                        // Nothing to do
                    }
                    dialog.advanced();
                }
            }

        });
    }

    private void createBasicTisProjectArea(Composite parent) {
        tosProjectComposite = toolkit.createComposite(parent);
        tosProjectComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        tosProjectComposite.setLayout(new FormLayout());
        tosProjectComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
        tosProjectComposite.setBackground(parent.getBackground());

        svnBranchLabel = toolkit.createLabel(tosProjectComposite, null);
        branchesViewer = new ComboViewer(tosProjectComposite, SWT.BORDER | SWT.READ_ONLY);
        branchesViewer.setContentProvider(new ArrayContentProvider());
        branchesViewer.setLabelProvider(new LabelProvider());
        branchesViewer.getControl().setVisible(false);

        Label tosProjectLabel = toolkit.createLabel(tosProjectComposite, Messages.getString("LoginComposite.projectTitle")); //$NON-NLS-1$

        openProjectBtn = toolkit.createButton(tosProjectComposite, null, SWT.PUSH);
        openProjectBtn.setText(Messages.getString("LoginComposite.buttons.open")); //$NON-NLS-1$
        openProjectBtn.setToolTipText(Messages.getString("LoginComposite.buttons.open.desc")); //$NON-NLS-1$
        Image image = ImageProvider.getImage(ERepositoryImages.OPEN_PROJECT_ICON);
        openProjectBtn.setImage(image);

        projectViewer = new ComboViewer(tosProjectComposite, SWT.BORDER | SWT.READ_ONLY);
        projectViewer.setContentProvider(new ArrayContentProvider());
        projectViewer.setLabelProvider(new ProjectLabelProvider());

        FormData data = new FormData();
        if (PluginChecker.isSVNProviderPluginLoaded()) {
            data.top = new FormAttachment(0, 12);
        } else {
            data.top = new FormAttachment(0, 10);
        }
        data.left = new FormAttachment(0, HORIZONTAL_TWO_SPACE);
        data.right = new FormAttachment(0, LEFTSPACE);
        tosProjectLabel.setLayoutData(data);

        // project
        data = new FormData();
        data.top = new FormAttachment(tosProjectLabel, 0, SWT.TOP);
        data.left = new FormAttachment(tosProjectLabel, HORIZONTAL_SPACE);
        data.right = new FormAttachment(90, -HORIZONTAL_TWO_SPACE);
        projectViewer.getControl().setLayoutData(data);

        // refresh button
        fillProjectsBtn = toolkit.createButton(tosProjectComposite, null, SWT.PUSH);
        fillProjectsBtn.setToolTipText(Messages.getString("LoginComposite.buttons.fill.desc")); //$NON-NLS-1$
        fillProjectsBtn.setImage(ImageProvider.getImage(EImage.REFRESH_ICON));
        data = new FormData();
        data.top = new FormAttachment(projectViewer.getControl(), 0, SWT.CENTER);
        data.right = new FormAttachment(100, -HORIZONTAL_TWO_SPACE);
        fillProjectsBtn.setLayoutData(data);

        // open button
        data = new FormData();
        if (PluginChecker.isSVNProviderPluginLoaded()) {
            data.top = new FormAttachment(projectViewer.getControl(), HORIZONTAL_SPACE);

        } else {
            data.top = new FormAttachment(projectViewer.getControl(), HORIZONTAL_TWO_SPACE);
        }
        data.right = new FormAttachment(90, -HORIZONTAL_TWO_SPACE);
        // data.bottom = new FormAttachment(projectViewer.getControl(), HORIZONTAL_SPACE + 50);
        openProjectBtn.setLayoutData(data);
    }

    private void createTosProjectArea(final Composite parent) {
        tosProjectComposite = toolkit.createComposite(parent);
        tosProjectComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        tosProjectComposite.setLayout(new FormLayout());
        tosProjectComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
        tosProjectComposite.setBackground(parent.getBackground());
        // tosProjectComposite.setBackground(RED_COLOR);

        openProjectBtn = toolkit.createButton(tosProjectComposite, null, SWT.PUSH);
        openProjectBtn.setText(Messages.getString("LoginComposite.buttons.open")); //$NON-NLS-1$
        openProjectBtn.setToolTipText(Messages.getString("LoginComposite.buttons.open.desc")); //$NON-NLS-1$
        Image image = ImageProvider.getImage(ERepositoryImages.OPEN_PROJECT_ICON);
        openProjectBtn.setImage(image);
        openProjectBtn.setVisible(false);

        Label createProjectLabel = toolkit
                .createLabel(tosProjectComposite, Messages.getString("LoginComposite.projectTitleTemp")); //$NON-NLS-1$

        //
        createProjectBtn = toolkit.createButton(tosProjectComposite, null, SWT.PUSH);
        createProjectBtn.setBackground(tosProjectComposite.getBackground());
        createProjectBtn.setText(Messages.getString("LoginComposite.NewCreate")); //$NON-NLS-1$
        FormData data = new FormData();
        data.top = new FormAttachment(createProjectLabel, -HORIZONTAL_SPACE, SWT.CENTER);
        data.right = new FormAttachment(90, -HORIZONTAL_TWO_SPACE);
        data.bottom = new FormAttachment(0, 28 + HORIZONTAL_SPACE);
        createProjectBtn.setLayoutData(data);
        createProjectBtn.setEnabled(true);

        GC gc = new GC(createProjectLabel);
        Point labelSize = gc.stringExtent(Messages.getString("LoginComposite.projectTitleTemp")); //$NON-NLS-1$
        gc.dispose();
        data = new FormData();
        data.top = new FormAttachment(0, 10);
        data.left = new FormAttachment(10, HORIZONTAL_SPACE);
        data.right = new FormAttachment(10, HORIZONTAL_THREE_SPACE + labelSize.x + 1);
        data.bottom = new FormAttachment(createProjectBtn, -1, SWT.BOTTOM);
        createProjectLabel.setLayoutData(data);

        projectText = toolkit.createText(tosProjectComposite, "", SWT.BORDER); //$NON-NLS-1$
        data = new FormData();
        data.top = new FormAttachment(createProjectLabel, 0, SWT.TOP);
        data.left = new FormAttachment(createProjectLabel, 10, SWT.RIGHT);
        data.bottom = new FormAttachment(createProjectLabel, 0, SWT.BOTTOM);
        Point btPoint = createProjectBtn.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        data.right = new FormAttachment(100, -HORIZONTAL_THREE_SPACE - btPoint.x - 50);
        projectText.setLayoutData(data);
        projectText.setText(DEFAULT_PROJECT_NAME);

        // add for bug TDI-19614
        projectText.setBackground(GREY_COLOR);
        projectText.addFocusListener(new FocusAdapter() {

            public void focusGained(FocusEvent e) {
                if (projectText.getText().equals(DEFAULT_PROJECT_NAME)) {
                    projectText.setBackground(null);
                    projectText.setText(""); //$NON-NLS-1$
                }
            }

            public void focusLost(FocusEvent e) {
                if (projectText.getText() == "") { //$NON-NLS-1$
                    projectText.setText(DEFAULT_PROJECT_NAME);
                    projectText.setBackground(GREY_COLOR);
                }
            }
        });

        advanced = toolkit.createButton(tosProjectComposite, null, SWT.PUSH);
        advanced.setText(Messages.getString("LoginComposite.buttons.advanced")); //$NON-NLS-1$
        advanced.setBackground(tosProjectComposite.getBackground());
        data = new FormData();
        data.top = new FormAttachment(createProjectLabel, HORIZONTAL_TWO_SPACE);
        data.left = new FormAttachment(createProjectLabel, HORIZONTAL_TWO_SPACE - 1, SWT.RIGHT);
        // data.right = new FormAttachment(90, -HORIZONTAL_THREE_SPACE);
        data.bottom = new FormAttachment(createProjectLabel, HORIZONTAL_TWO_SPACE + 50);
        advanced.setLayoutData(data);

        createProjectBtn.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                Project project = null;
                NewProjectWizard newPrjWiz = new NewProjectWizard(new Project[] {});
                newPrjWiz.setDefaultProjectName(projectText.getText());
                WizardDialog newProjectDialog = new WizardDialog(getShell(), newPrjWiz);
                newProjectDialog.setTitle(Messages.getString("LoginDialog.newProjectTitle")); //$NON-NLS-1$
                if (newProjectDialog.open() == Window.OK) {
                    project = newPrjWiz.getProject();
                    populateProjectList();
                    projectText.setText(project.getLabel());
                    dialog.advanced();
                }
            }
        });

        advanced.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                dialog.advanced();
            }
        });

    }

    private void createTosWelcomArea(Composite parent) {
        tosWelcomeComposite = toolkit.createComposite(parent);
        tosWelcomeComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        tosWelcomeComposite.setLayout(new FormLayout());
        tosWelcomeComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
        // tosWelcomeComposite.setBackground(RED_COLOR);

        FormData formData2 = null;

        colorComposite = toolkit.createComposite(tosWelcomeComposite);

        // display the icon on the colorComposite
        // iconLabel and onIiconLabel both for the display of icon.
        onIconLabel = toolkit.createLabel(colorComposite, null);
        formData2 = new FormData();
        formData2.height = 46;
        formData2.top = new FormAttachment(0, -11);
        formData2.left = new FormAttachment(0, 8);
        formData2.right = new FormAttachment(0, 54);
        onIconLabel.setLayoutData(formData2);

        colorComposite.setLayout(new FormLayout());
        formData2 = new FormData();
        formData2.top = new FormAttachment(0, 11);
        formData2.height = 24;
        formData2.left = new FormAttachment(0);
        formData2.right = new FormAttachment(100);
        colorComposite.setLayoutData(formData2);

        // display the icon under the colorComposite
        iconLabel = toolkit.createLabel(tosWelcomeComposite, null);
        formData2 = new FormData();
        formData2.height = 46;
        formData2.top = new FormAttachment(0, 0);
        formData2.left = new FormAttachment(0, 8);
        formData2.right = new FormAttachment(0, 54);
        iconLabel.setLayoutData(formData2);

        statusLabel = toolkit.createLabel(colorComposite, null);
        formData2 = new FormData();
        formData2.top = new FormAttachment(colorComposite, 4, SWT.CENTER);
        formData2.height = 24;
        formData2.left = new FormAttachment(0, 60);
        formData2.right = new FormAttachment(100, -5);
        statusLabel.setLayoutData(formData2);
        restartBut = toolkit.createButton(tosWelcomeComposite, Messages.getString("LoginComposite.RESTART"), SWT.PUSH); //$NON-NLS-1$
        restartBut.setVisible(false);
        FormData formData = new FormData();
        formData.top = new FormAttachment(colorComposite, 0);// 5, 315
        // formData.left = new FormAttachment(0, 310);
        formData.right = new FormAttachment(100, -5);
        formData.bottom = new FormAttachment(100, 0);
        restartBut.setLayoutData(formData);// new GridData(GridData.FILL_HORIZONTAL)
        updateBtn = toolkit.createButton(tosWelcomeComposite, "update", SWT.PUSH); //$NON-NLS-1$
        updateBtn.setVisible(false);
        // updateBtn.setEnabled(needUpdate(ARCHIVA_SERVICES_URL, "internal"));
        FormData updateBtnformData = new FormData();
        updateBtnformData.top = new FormAttachment(colorComposite, 0);// 5, 315
        // formData.left = new FormAttachment(restartBut, 100);
        updateBtnformData.right = new FormAttachment(restartBut, -5);
        updateBtnformData.bottom = new FormAttachment(100, 0);
        updateBtn.setLayoutData(updateBtnformData);// new GridData(GridData.FILL_HORIZONTAL)

    }

    private void createTisRepositoryArea(Composite parent) {
        tisRepositoryComposite = toolkit.createComposite(parent);
        tisRepositoryComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        tisRepositoryComposite.setLayout(new FormLayout());
        tisRepositoryComposite.setBackground(GREY_COLOR);

        FormData formData;
        Composite group = new Composite(tisRepositoryComposite, SWT.None);

        // tis repository button
        manageConnectionsButton = toolkit.createButton(tisRepositoryComposite, null, SWT.PUSH);
        manageConnectionsButton.setBackground(GREY_COLOR);
        manageConnectionsButton.setImage(ImageProvider.getImage(EImage.THREE_DOTS_ICON));
        formData = new FormData();
        formData.top = new FormAttachment(group, HORIZONTAL_SPACE, SWT.CENTER);
        formData.right = new FormAttachment(100, -HORIZONTAL_TWO_SPACE);
        manageConnectionsButton.setLayoutData(formData);

        group.setLayout(new FormLayout());
        formData = new FormData();
        formData.top = new FormAttachment(0, 0);
        formData.left = new FormAttachment(0, -HORIZONTAL_SPACE);
        formData.right = new FormAttachment(manageConnectionsButton, -HORIZONTAL_SPACE);
        formData.bottom = new FormAttachment(100, -2);
        group.setLayoutData(formData);

        // tis connection
        Label connectionLabel = toolkit.createLabel(group, null);
        connectionLabel.setBackground(tisRepositoryComposite.getBackground());
        connectionLabel.setText("Connection"); //$NON-NLS-1$
        formData = new FormData();
        formData.top = new FormAttachment(0, HORIZONTAL_TWO_SPACE);
        formData.left = new FormAttachment(0, HORIZONTAL_TWO_SPACE);
        formData.right = new FormAttachment(0, LEFTSPACE);
        connectionLabel.setLayoutData(formData);

        connectionsViewer = new ComboViewer(group, SWT.BORDER | SWT.READ_ONLY);
        connectionsViewer.setContentProvider(new ArrayContentProvider());
        connectionsViewer.setLabelProvider(new ConnectionLabelProvider());
        formData = new FormData();
        formData.top = new FormAttachment(connectionLabel, 0, SWT.CENTER);
        formData.left = new FormAttachment(connectionLabel, HORIZONTAL_SPACE);
        formData.right = new FormAttachment(95, 12);
        connectionsViewer.getControl().setLayoutData(formData);

        // tis e-mail
        Label emailLabel = toolkit.createLabel(group, null);
        emailLabel.setBackground(tisRepositoryComposite.getBackground());//
        emailLabel.setText(Messages.getString("LoginComposite.emailTitle")); //$NON-NLS-1$
        formData = new FormData();
        formData.top = new FormAttachment(connectionLabel, HORIZONTAL_TWO_SPACE);
        formData.left = new FormAttachment(0, HORIZONTAL_TWO_SPACE);
        formData.right = new FormAttachment(0, LEFTSPACE);
        emailLabel.setLayoutData(formData);

        user = toolkit.createText(group, null, SWT.BORDER);
        user.setEditable(false);
        user.setEnabled(false);
        formData = new FormData();
        formData.top = new FormAttachment(emailLabel, 0, SWT.CENTER);
        formData.left = new FormAttachment(emailLabel, HORIZONTAL_SPACE);
        formData.right = new FormAttachment(95, 12);
        user.setLayoutData(formData);

        // tis password
        passwordComposite = toolkit.createComposite(parent);
        GridData gd = new GridData(GridData.FILL_BOTH);
        passwordComposite.setLayoutData(gd);
        passwordComposite.setLayout(new FormLayout());
        passwordComposite.setBackground(GREY_COLOR);

        passwordLabel = toolkit.createLabel(passwordComposite, null);
        passwordLabel.setBackground(passwordComposite.getBackground());
        passwordLabel.setText(Messages.getString("LoginComposite.passwordTitle")); //$NON-NLS-1$
        formData = new FormData();
        formData.top = new FormAttachment(passwordComposite, 3, SWT.TOP);
        formData.left = new FormAttachment(0, HORIZONTAL_TWO_SPACE - 5);//
        formData.right = new FormAttachment(0, LEFTSPACE - 5);// - 5
        // formData.bottom = new FormAttachment(100, -1);
        passwordLabel.setLayoutData(formData);

        passwordText = toolkit.createText(passwordComposite, null, SWT.PASSWORD | SWT.BORDER);
        passwordText.setEditable(false);
        passwordText.setEnabled(false);
        formData = new FormData();
        formData.top = new FormAttachment(passwordComposite, 1, SWT.TOP);
        formData.left = new FormAttachment(passwordLabel, HORIZONTAL_SPACE);
        formData.right = new FormAttachment(100, -45);
        // formData.bottom = new FormAttachment(100, -1);
        passwordText.setLayoutData(formData);

    }

    private void createTisProjectArea(Composite parent) {
        createBasicTisProjectArea(parent); // tosProjectComposite

        FormData data;

        // branch svn

        svnBranchLabel.setText("SVN Branch"); //$NON-NLS-1$
        data = new FormData();
        data.top = new FormAttachment(fillProjectsBtn, HORIZONTAL_SPACE, SWT.BOTTOM);
        data.left = new FormAttachment(0, HORIZONTAL_TWO_SPACE);
        data.right = new FormAttachment(0, LEFTSPACE);
        svnBranchLabel.setLayoutData(data);

        // if (branchesViewer != null && !branchesViewer.getControl().isVisible()) {
        // branchesViewer.getControl().setVisible(true);
        // }
        // need for providers
        data = new FormData();
        data.top = new FormAttachment(svnBranchLabel, 0, SWT.CENTER);
        data.left = new FormAttachment(svnBranchLabel, HORIZONTAL_SPACE);
        data.right = new FormAttachment(50, 0);
        branchesViewer.getControl().setLayoutData(data);
    }

    private void createTisBlankArea(Composite parent) {
        tisBlankCompoiste = toolkit.createComposite(parent);
        // tisBlankCompoiste.setBackground(RED_COLOR);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 20;
        tisBlankCompoiste.setLayoutData(gd);
        tisBlankCompoiste.setVisible(true);
        tisBlankCompoiste.setBackgroundMode(SWT.INHERIT_DEFAULT);
        tisBlankCompoiste.setBackground(parent.getBackground());
    }

    private GridLayout createLayout(int numColumns) {
        GridLayout layout = new GridLayout(numColumns, false);
        if (!EnvironmentUtils.isWindowsSystem()) {
            layout.marginHeight = 0;
            layout.verticalSpacing = 0;
        }
        return layout;
    }

    public boolean needRestartForLocal() {
        final ConnectionBean curConnection = getConnection();
        if (curConnection != null && this.firstConnBean != null) {
            // only switch from other connection to local.
            if (!this.firstConnBean.getRepositoryId().equals(LoginComposite.LOCAL)
                    && curConnection.getRepositoryId().equals(LoginComposite.LOCAL)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Class use to fill manage projects dialog box.
     */
    private abstract class ManageItem {

        private String label;

        public ManageItem(String label) {
            super();
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return getLabel();
        }

        public abstract void run();
    }

    /**
     * @see org.eclipse.swt.widgets.Widget#dispose()
     */
    @Override
    public void dispose() {
        toolkit.dispose();
        super.dispose();
    }

    private boolean isSVNProviderPluginLoadedRemote() {
        boolean isRemote = false;
        if (PluginChecker.isSVNProviderPluginLoaded()) {
            StructuredSelection selection = (StructuredSelection) connectionsViewer.getSelection();
            Object firstElement = selection.getFirstElement();
            if (firstElement instanceof ConnectionBean) {
                ConnectionBean bean = (ConnectionBean) firstElement;
                isRemote = RepositoryConstants.REPOSITORY_REMOTE_ID.equals(bean.getRepositoryId());
            }
        }
        return isRemote;
    }

    public void setStatusArea() throws PersistenceException {
        String productName = brandingService.getFullProductName();
        if (productName != null) {
            String[] split = productName.split(" "); //$NON-NLS-1$
            if (split != null && split.length > 3) {
                productName = brandingService.getShortProductName();
            }
        }

        if (getConnection() != null) {
            final boolean localConn = getConnection().getRepositoryId() == null
                    || getConnection().getRepositoryId().equals(RepositoryConstants.REPOSITORY_LOCAL_ID);
            boolean visible = PluginChecker.isSVNProviderPluginLoaded() && !localConn;
            if (passwordLabel != null) {
                passwordLabel.setVisible(visible);
            }
            if (passwordText != null) {
                passwordText.setVisible(visible);
            }
            if (svnBranchLabel != null) {
                svnBranchLabel.setVisible(visible);
            }
            if (branchesViewer != null) {
                branchesViewer.getControl().setVisible(visible);
            }

            manageViewer.setInput(getManageElements());
            setManageViewer();
            if (!isWorkSpaceSame()) {
                iconLabel.setImage(LOGIN_CRITICAL_IMAGE);
                onIconLabel.setImage(LOGIN_CRITICAL_IMAGE);
                colorComposite.setBackground(RED_COLOR);
                onIconLabel.setBackground(colorComposite.getBackground());
                statusLabel.setText(Messages.getString("LoginComposite.DIFFERENT_WORKSPACES")); //$NON-NLS-1$
                statusLabel.setBackground(RED_COLOR);
                statusLabel.setForeground(WHITE_COLOR);
                Font font = new Font(null, LoginComposite.FONT_ARIAL, 9, SWT.BOLD);// Arial courier
                statusLabel.setFont(font);
            } else if (inuse) {
                iconLabel.setImage(LOGIN_CRITICAL_IMAGE);
                onIconLabel.setImage(LOGIN_CRITICAL_IMAGE);
                colorComposite.setBackground(RED_COLOR);
                onIconLabel.setBackground(colorComposite.getBackground());
                statusLabel.setText(Messages.getString("LoginComposite.Workspace_inuse")); //$NON-NLS-1$
                statusLabel.setBackground(RED_COLOR);
                statusLabel.setForeground(WHITE_COLOR);
                Font font = new Font(null, LoginComposite.FONT_ARIAL, 9, SWT.BOLD);// Arial courier
                statusLabel.setFont(font);
            } else if (projectViewer == null || projectViewer.getCombo().getItemCount() > 0) {
                iconLabel.setImage(LOGIN_CORRECT_IMAGE);
                onIconLabel.setImage(LOGIN_CORRECT_IMAGE);
                colorComposite.setBackground(YELLOW_GREEN_COLOR);
                onIconLabel.setBackground(colorComposite.getBackground());
                statusLabel.setText(Messages.getString("LoginComposite.TisWorkspace_welcome", productName)); //$NON-NLS-1$
                int size = calStatusLabelFont(11, statusLabel.getText());
                statusLabel.setBackground(YELLOW_GREEN_COLOR);
                statusLabel.setForeground(WHITE_COLOR);
                Font font = new Font(null, LoginComposite.FONT_ARIAL, size, SWT.BOLD);// Arial courier
                statusLabel.setFont(font);
                if (fillProjectsBtn != null) {
                    fillProjectsBtn.setEnabled(true);
                }
            } else {
                iconLabel.setImage(LOGIN_CRITICAL_IMAGE);
                onIconLabel.setImage(LOGIN_CRITICAL_IMAGE);
                colorComposite.setBackground(RED_COLOR);
                onIconLabel.setBackground(colorComposite.getBackground());
                statusLabel.setText(Messages.getString("LoginComposite.PROJECT_NEED")); //$NON-NLS-1$
                statusLabel.setBackground(RED_COLOR);
                statusLabel.setForeground(WHITE_COLOR);
                Font font = new Font(null, LoginComposite.FONT_ARIAL, 9, SWT.BOLD);// Arial courier
                statusLabel.setFont(font);
            }
        } else {
            iconLabel.setImage(LOGIN_CRITICAL_IMAGE);
            onIconLabel.setImage(LOGIN_CRITICAL_IMAGE);
            colorComposite.setBackground(RED_COLOR);
            onIconLabel.setBackground(colorComposite.getBackground());
            statusLabel.setText(Messages.getString("LoginComposite.connectionEmpty")); //$NON-NLS-1$
            statusLabel.setBackground(RED_COLOR);
            statusLabel.setForeground(WHITE_COLOR);
            Font font = new Font(null, LoginComposite.FONT_ARIAL, 9, SWT.BOLD);// Arial courier
            statusLabel.setFont(font);
        }
        // this.tosWelcomeComposite.redraw();
        updateVisible();
    }

    private void readConnectionData() {
        if (perReader.isHaveUserPer()) {
            storedConnections = perReader.readConnections();
            lastConnection = perReader.readLastConncetion();
        } else {
            PreferenceManipulator prefManipulator = new PreferenceManipulator(CorePlugin.getDefault().getPreferenceStore());
            storedConnections = prefManipulator.readConnections();
            lastConnection = prefManipulator.getLastConnection();
        }
    }

    private void recordFirstConnection() {
        if (storedConnections != null) {
            for (ConnectionBean bean : storedConnections) {
                if (bean.getName().equals(lastConnection)) {
                    firstConnBean = bean;
                    break;
                }
            }
        }
    }

    private void fillContents() {
        // PreferenceManipulator prefManipulator = new
        // PreferenceManipulator(CorePlugin.getDefault().getPreferenceStore());
        //
        // List<ConnectionBean> storedConnections = prefManipulator.readConnections();
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        boolean isOnlyRemoteConnection = brandingService.getBrandingConfiguration().isOnlyRemoteConnection();
        for (ConnectionBean bean : storedConnections) {
            String user2 = bean.getUser();
            String repositoryId2 = bean.getRepositoryId();
            String workSpace = bean.getWorkSpace();
            String name = bean.getName();
            if (user2 != null && !"".equals(user2) && repositoryId2 != null && !"".equals(repositoryId2) && workSpace != null //$NON-NLS-1$ //$NON-NLS-2$
                    && !"".equals(workSpace) && name != null && !"".equals(name)) { //$NON-NLS-1$ //$NON-NLS-2$
                boolean valid = Pattern.matches(RepositoryConstants.MAIL_PATTERN, user2);
                if (valid && RepositoryConstants.REPOSITORY_REMOTE_ID.equals(repositoryId2)) {
                    String url = bean.getDynamicFields().get(RepositoryConstants.REPOSITORY_URL);
                    valid = url != null || !"".equals(url); //$NON-NLS-1$
                }
                bean.setComplete(valid);
            }
        }
        if (!isOnlyRemoteConnection) {
            connectionsViewer.setInput(storedConnections);
        } else {
            // feature 8,hide error remote connection for Uniserv after their login validate
            List<ILoginConnectionService> loginConnectionServices = LoginConnectionManager.getRemoteConnectionService();
            List<ConnectionBean> lastRemoteConnections = new ArrayList<ConnectionBean>();
            if (loginConnectionServices.size() > 0) {
                for (ILoginConnectionService loginConncetion : loginConnectionServices) {
                    for (ConnectionBean bean : storedConnections) {
                        String errorMsg = loginConncetion.checkConnectionValidation(bean.getName(), bean.getDescription(),
                                bean.getUser(), bean.getPassword(), bean.getWorkSpace(),
                                bean.getDynamicFields().get(RepositoryConstants.REPOSITORY_URL));
                        if (StringUtils.isEmpty(errorMsg) && bean.isComplete()) {
                            lastRemoteConnections.add(bean);
                        }
                    }
                }
            }
            if (lastRemoteConnections.size() > 0) {
                storedConnections = lastRemoteConnections;
            }
            connectionsViewer.setInput(storedConnections);
        }

        // Check number of connection available.
        if (storedConnections.size() == 0) {
            //
        } else if (storedConnections.size() == 1) {
            connectionsViewer.setSelection(new StructuredSelection(new Object[] { storedConnections.get(0) }));
        } else {

            // select last connection used
            boolean selected = false;
            for (ConnectionBean curent : storedConnections) {
                String stringValue = ((LabelProvider) connectionsViewer.getLabelProvider()).getText(curent);
                if (stringValue.equals(lastConnection)) {
                    selectLast(stringValue, connectionsViewer.getCombo());
                    selected = true;
                }
            }
            if (!selected) {
                connectionsViewer.setSelection(new StructuredSelection(new Object[] { storedConnections.get(0) }));
            }
        }
        // projectViewer.getControl().setEnabled(false);
        // branchesViewer.getControl().setEnabled(false);
        if (getConnection() != null) {
            user.setText(getConnection().getUser());
            passwordText.setText(getConnection().getPassword());

            if (!isAuthenticationNeeded()) {
                // unpopulateRemoteLoginElements();
            }
            setRepositoryContextInContext();
        }
        boolean tisRemote = isSVNProviderPluginLoadedRemote();
        if (svnBranchLabel != null) {
            svnBranchLabel.setVisible(tisRemote);
        }
        if (branchesViewer != null) {
            branchesViewer.getControl().setVisible(tisRemote);
        }
        // updateButtons();

        // Validate data
        if (validateFields()) {
            populateProjectList();
            validateProject();
        } else {
            if (storedConnections.size() > 0) {
                user.setText(storedConnections.get(0).getUser());
            }
            setRepositoryContextInContext();
            populateProjectList();
        }
        manageViewer.setInput(getManageElements());
        setManageViewer();
    }

    /**
     * If setted, Select last ? used in PreferenceStore.
     * 
     * @param prefManipulator
     */
    private void selectLast(String lastObjectSelected, Combo comboToSelect) {
        if (lastObjectSelected != null) {
            int userIndex = -1;
            String[] items = comboToSelect.getItems();
            for (int i = 0; userIndex == -1 && i < items.length; i++) {
                if (lastObjectSelected.equals(items[i])) {
                    userIndex = i;
                }
            }
            if (userIndex != -1) {
                comboToSelect.select(userIndex);
            } else {
                comboToSelect.select(0);
            }
        }
    }

    /**
     * 
     * @return
     */
    protected boolean isAuthenticationNeeded() {
        if (getConnection() == null || !getConnection().isComplete()) {
            return false;
        }
        IRepositoryFactory repository = RepositoryFactoryProvider.getRepositoriyById(getConnection().getRepositoryId());
        if (repository == null) {
            return false;
        }
        return repository.isAuthenticationNeeded();
    }

    private void addListeners() {
        if (connectionsViewer != null) {
            connectionsViewer.addSelectionChangedListener(new ISelectionChangedListener() {

                public void selectionChanged(SelectionChangedEvent event) {
                    try {
                        final ConnectionBean connection = getConnection();
                        if (connection == null) {
                            return;
                        }
                        if (beforeConnBean != null && connection.equals(beforeConnBean)) {
                            return;
                        }
                        beforeConnBean = connection;
                        user.setText(connection.getUser() == null ? "" : connection.getUser()); //$NON-NLS-1$
                        passwordText.setText(connection.getPassword() == null ? "" : connection.getPassword()); //$NON-NLS-1$
                        updateServerFields();
                        // updateButtons();
                        updateVisible();

                        // Validate data
                        if (validateFields()) {
                            populateProjectList();
                            validateProject();
                        }
                        setStatusArea();
                        validateUpdate();
                    } catch (PersistenceException e) {
                        ExceptionHandler.process(e);
                    } catch (JSONException e) {
                        ExceptionHandler.process(e);
                    }
                    displayPasswordComposite();
                }

            });
        }
        if (PluginChecker.isSVNProviderPluginLoaded()) {

            ModifyListener modifyListener = new ModifyListener() {

                public void modifyText(ModifyEvent e) {
                    unpopulateProjectList();
                    dialog.updateButtons();
                    setRepositoryContextInContext();
                }
            };

            passwordText.addModifyListener(modifyListener);

            fillProjectsBtn.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    // Validate data
                    if (validateFields()) {
                        populateProjectList();
                        validateProject();
                    }
                    try {
                        setStatusArea();
                    } catch (PersistenceException e1) {
                        ExceptionHandler.process(e1);
                    }
                    setRepositoryContextInContext();
                }
            });

            branchesViewer.addSelectionChangedListener(new ISelectionChangedListener() {

                public void selectionChanged(SelectionChangedEvent event) {
                    PreferenceManipulator prefManipulator = new PreferenceManipulator(CorePlugin.getDefault()
                            .getPreferenceStore());
                    String branch = getBranch();
                    if (branch == null) {
                        branch = SVNConstant.EMPTY;
                    }
                    prefManipulator.setLastSVNBranch(branch);
                }
            });
        }

        if (projectViewer != null) {
            projectViewer.addSelectionChangedListener(new ISelectionChangedListener() {

                public void selectionChanged(SelectionChangedEvent event) {
                    PreferenceManipulator prefManipulator = new PreferenceManipulator(CorePlugin.getDefault()
                            .getPreferenceStore());
                    Project project = getProject();
                    prefManipulator.setLastProject(project.getLabel());
                    setBranchesSetting(project, false);
                    dialog.updateButtons();
                    setRepositoryContextInContext();
                }
            });
        }

        manageConnectionsButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    ConnectionsDialog connectionsDialog = new ConnectionsDialog(getShell());
                    int open = connectionsDialog.open();
                    if (open == Window.OK) {
                        PreferenceManipulator prefManipulator = new PreferenceManipulator(CorePlugin.getDefault()
                                .getPreferenceStore());
                        prefManipulator.saveConnections(connectionsDialog.getConnections());

                        LoginComposite.this.storedConnections = connectionsDialog.getConnections();
                        perReader.saveConnections(LoginComposite.this.storedConnections);
                        fillContents();
                        // updateVisible();
                        // validateUpdate();
                        final ConnectionBean connection = getConnection();
                        if (connection == null) {
                            return;
                        }
                        beforeConnBean = connection;
                        user.setText(connection.getUser() == null ? "" : connection.getUser()); //$NON-NLS-1$
                        passwordText.setText(connection.getPassword() == null ? "" : connection.getPassword()); //$NON-NLS-1$

                        updateServerFields();
                        // updateButtons();
                        updateVisible();

                        // Validate data
                        if (validateFields()) {
                            populateProjectList();
                            validateProject();
                        }
                        setStatusArea();
                        validateUpdate();
                        displayPasswordComposite();
                    }
                    // setStatusArea();
                } catch (PersistenceException e1) {
                    ExceptionHandler.process(e1);
                } catch (JSONException e2) {
                    ExceptionHandler.process(e2);
                }
            }
        });

        manageProjectsButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ManageItem item = null;
                if (!manageViewer.getSelection().isEmpty()) {
                    IStructuredSelection sel = (IStructuredSelection) manageViewer.getSelection();
                    item = (ManageItem) sel.getFirstElement();

                }
                item.run();
                try {
                    setStatusArea();
                } catch (PersistenceException e1) {
                    ExceptionHandler.process(e1);
                }
            }
        });

        openProjectBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setRepositoryContextInContext();
                dialog.okPressed();
            }
        });

        restartBut.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                isRestart = true;
                perReader.saveLastConnectionBean(getConnection());
                dialog.okPressed();
            }
        });

        updateBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                // install and update all patches;
                try {
                    ICoreTisService tisService = (ICoreTisService) GlobalServiceRegister.getDefault().getService(
                            ICoreTisService.class);
                    afterUpdate = false;
                    if (tisService != null) {
                        JSONObject archivaProperties = getArchivaServicesProperties(getAdminURL());
                        String archivaServicesURL = archivaProperties.getString(ARCHIVA_SERVICES_URL_KEY)
                                + ARCHIVA_SERVICES_SEGMENT;
                        String repository = archivaProperties.getString(ARCHIVA_REPOSITORY_KEY);
                        String username = archivaProperties.getString(ARCHIVA_USER);
                        String password = archivaProperties.getString(ARCHIVA_USER_PWD);
                        List<String> repositories = new ArrayList<String>();
                        // if no repository return,just use a empty repositories array
                        if (repository != null) {
                            repositories.add(repository);
                        }
                        tisService.downLoadAndInstallUpdateSites(archivaServicesURL, username, password, updateSiteToInstall,
                                repositories);
                        afterUpdate = true;
                        tisService.setNeedResartAfterUpdate(afterUpdate);
                        updateSiteToInstall.clear();
                    }
                    // need to relauch the studio automaticlly after updating
                    isRestart = true;
                    perReader.saveLastConnectionBean(getConnection());
                    dialog.okPressed();
                } catch (Exception e1) {
                    ExceptionHandler.process(e1);
                }
            }
        });
    }

    private void validateUpdate() throws JSONException {
        // need get archiva url and repository by tac
        String archivaServiceURL;
        String repository;
        String username;
        String password;
        ConnectionBean currentBean = getConnection();
        String repositoryId = null;
        // at 1st time open the studio there are no bean at all,so need avoid NPE
        if (currentBean != null) {
            repositoryId = currentBean.getRepositoryId();
        }
        // if workspace different,no need to spent time check patches
        try {
            if (repositoryId != null && repositoryId.equals("remote") && isSVNProviderPluginLoadedRemote() && isWorkSpaceSame()) { //$NON-NLS-1$
                JSONObject archivaProperties = getArchivaServicesProperties(getAdminURL());

                archivaServiceURL = archivaProperties.getString(ARCHIVA_SERVICES_URL_KEY) + ARCHIVA_SERVICES_SEGMENT;
                repository = archivaProperties.getString(ARCHIVA_REPOSITORY_KEY);
                username = archivaProperties.getString(ARCHIVA_USER);
                password = archivaProperties.getString(ARCHIVA_USER_PWD);
                List<String> repositories = new ArrayList<String>();
                if (repository != null) {
                    repositories.add(repository);
                }
                if (archivaServiceURL != null) {
                    boolean needUpdate = needUpdate(username, password, archivaServiceURL, repositories);
                    if (afterUpdate) {
                        iconLabel.setImage(LOGIN_CRITICAL_IMAGE);
                        onIconLabel.setImage(LOGIN_CRITICAL_IMAGE);
                        colorComposite.setBackground(RED_COLOR);
                        onIconLabel.setBackground(colorComposite.getBackground());
                        statusLabel.setText("Update finished,need to restart"); //$NON-NLS-1$
                        statusLabel.setBackground(RED_COLOR);
                        statusLabel.setForeground(WHITE_COLOR);
                        Font font = new Font(null, LoginComposite.FONT_ARIAL, 9, SWT.BOLD);// Arial courier
                        statusLabel.setFont(font);
                        restartBut.setVisible(true);
                        restartBut.setEnabled(true);
                        openProjectBtn.setEnabled(false);
                        updateBtn.setEnabled(false);

                    } else if (needUpdate && isWorkSpaceSame()) {
                        iconLabel.setImage(LOGIN_CRITICAL_IMAGE);
                        onIconLabel.setImage(LOGIN_CRITICAL_IMAGE);
                        colorComposite.setBackground(RED_COLOR);
                        onIconLabel.setBackground(colorComposite.getBackground());
                        statusLabel.setText("Update is required,please click the button to update"); //$NON-NLS-1$
                        statusLabel.setBackground(RED_COLOR);
                        statusLabel.setForeground(WHITE_COLOR);
                        Font font = new Font(null, LoginComposite.FONT_ARIAL, 9, SWT.BOLD);// Arial courier
                        statusLabel.setFont(font);
                        openProjectBtn.setEnabled(!needUpdate);
                        updateBtn.setVisible(needUpdate);
                        updateBtn.setEnabled(needUpdate);
                    }
                }
            } else {
                updateBtn.setVisible(false);
                updateBtn.setEnabled(false);
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        } catch (LoginException e) {
            ExceptionHandler.process(e);
        }

    }

    private String getAdminURL() {
        String tacURL = null;
        ConnectionBean currentBean = getConnection();
        if (currentBean != null && currentBean.getRepositoryId().equals("remote")) { //$NON-NLS-1$
            tacURL = currentBean.getDynamicFields().get("url"); //$NON-NLS-1$
        }
        return tacURL;
    }

    /* should use api of tac to get the properties */
    private JSONObject getArchivaServicesProperties(String tacURL) throws PersistenceException, LoginException {
        JSONObject archivaObject = null;
        ICoreTisService tisService = (ICoreTisService) GlobalServiceRegister.getDefault().getService(ICoreTisService.class);
        if (tisService != null) {
            String userName = getConnection().getUser();
            String password = getConnection().getPassword();
            User user = PropertiesFactory.eINSTANCE.createUser();
            user.setLogin(userName);
            archivaObject = (JSONObject) tisService.getArchivaObject(user, password, tacURL);
        }

        return archivaObject;
    }

    // method need update is used to control the status of updateBtn
    private boolean needUpdate(String username, String password, String archivaURL, List<String> repositories) {

        ICoreTisService tisService = (ICoreTisService) GlobalServiceRegister.getDefault().getService(ICoreTisService.class);
        if (tisService != null) {
            try {
                if (updateSiteToInstall != null) {
                    updateSiteToInstall.clear();
                }
                updateSiteToInstall = tisService.getUpdateSitesToBeInstall(username, password, archivaURL, repositories);
            } catch (BackingStoreException e) {
                ExceptionHandler.process(e);
            }
        }
        return !updateSiteToInstall.isEmpty();
    }

    public void createNewProject() {
        Project project = null;
        NewProjectWizard newPrjWiz = new NewProjectWizard((Project[]) projectViewer.getInput());
        WizardDialog newProjectDialog = new WizardDialog(getShell(), newPrjWiz);
        newProjectDialog.setTitle(Messages.getString("LoginDialog.newProjectTitle")); //$NON-NLS-1$
        if (newProjectDialog.open() == Window.OK) {
            project = newPrjWiz.getProject();
            populateProjectList();
            selectProject(project);
        }
        validateProject();
    }

    public void importProjects() {
        ImportProjectAsAction.getInstance().run();
        populateProjectList();
        String newProject = ImportProjectAsAction.getInstance().getProjectName();
        if (newProject != null) {
            selectProject(newProject);
        }
        validateProject();
    }

    public void importDemoProject() {
        // dialog.setMessage("Importing demo project ...");
        ImportDemoProjectAction.getInstance().setShell(getShell());
        ImportDemoProjectAction.getInstance().run();
        populateProjectList();
        String newProject = ImportDemoProjectAction.getInstance().getProjectName();
        if (newProject != null) {
            selectProject(newProject);
        }
        validateProject();
    }

    public void deleteProject() {
        DeleteProjectsAsAction deleteProjectAction = new DeleteProjectsAsAction(true);
        deleteProjectAction.run();
        populateProjectList();
        dialog.updateButtons();
        validateProject();
    }

    private void createSendboxProject() {
        if (getConnection() != null) {
            setRepositoryContextInContext(); // must set the current connection, if existed
        }
        CreateSandboxProjectDialog sandboxDialog = new CreateSandboxProjectDialog(getShell(), getConnection());

        if (sandboxDialog.open() == Window.OK) {
            ConnectionUserPerReader instance = ConnectionUserPerReader.getInstance();
            this.storedConnections = instance.forceReadConnections();
            this.lastConnection = sandboxDialog.getConnectionBean().getName();

            PreferenceManipulator prefManipulator = new PreferenceManipulator(CorePlugin.getDefault().getPreferenceStore());
            prefManipulator.saveConnections(this.storedConnections);
            perReader.saveConnections(this.storedConnections);

            fillContents();
        }
        try {
            setStatusArea();
        } catch (PersistenceException e1) {
            ExceptionHandler.process(e1);
        }
        if (getConnection() != null) { // reset the context, because there are some changes in create sandbox dialog
            setRepositoryContextInContext();
        }
    }

    /**
     * smallet Comment method "updateServerFields".
     */
    private void updateServerFields() {
        setRepositoryContextInContext();
        validateFields();

        if (isAuthenticationNeeded()) {
            unpopulateProjectList();
            populateRemoteLoginElements();
        } else {
            unpopulateRemoteLoginElements();
        }
        dialog.updateButtons();
    }

    public boolean isWorkSpaceSame() {
        ConnectionBean bean = getConnection();
        if (bean == null) {
            return false;
        }
        String workspace = bean.getWorkSpace();
        // if (String.valueOf(workspace.charAt(0)).equals("/")) {
        // workspace = workspace.substring(1, workspace.length());
        // }

        String defaultPath = new Path(Platform.getInstanceLocation().getURL().getPath()).toFile().getPath();
        //        String filePath1 = defaultPath.substring(defaultPath.indexOf("/"), defaultPath.length() - 1); //$NON-NLS-1$
        //        String filePath2 = defaultPath.substring(defaultPath.indexOf("/") + 1, defaultPath.length() - 1); //$NON-NLS-1$
        return workspace.equals(defaultPath); // workspace.equals(filePath1) || workspace.equals(filePath2);
    }

    private void updateVisible() {
        List<ILoginConnectionService> loginConnectionServices = LoginConnectionManager.getRemoteConnectionService();
        boolean localConn = false;
        String errorMsg = null;
        if (getConnection() != null) {
            localConn = getConnection().getRepositoryId() == null
                    || getConnection().getRepositoryId().equals(RepositoryConstants.REPOSITORY_LOCAL_ID);
            if (loginConnectionServices.size() > 0 && getConnection().isComplete()) {
                for (ILoginConnectionService loginConncetion : loginConnectionServices) {
                    errorMsg = loginConncetion.checkConnectionValidation(getConnection().getName(), getConnection()
                            .getDescription(), getConnection().getUser(), getConnection().getPassword(), getConnection()
                            .getWorkSpace(), getConnection().getDynamicFields().get(RepositoryConstants.REPOSITORY_URL));
                    if (StringUtils.isEmpty(errorMsg)) {
                        break;
                    }
                }
            }
        }

        if (getConnection() == null) {
            manageViewer.getControl().setEnabled(false);
            manageProjectsButton.setEnabled(false);
            openProjectBtn.setEnabled(false);
            if (fillProjectsBtn != null) {
                fillProjectsBtn.setEnabled(false);
            }
            projectViewer.getControl().setEnabled(false);
            if (branchesViewer != null) {
                branchesViewer.getControl().setEnabled(false);
            }
        } else if (errorMsg != null) {
            iconLabel.setImage(LOGIN_CRITICAL_IMAGE);
            onIconLabel.setImage(LOGIN_CRITICAL_IMAGE);
            colorComposite.setBackground(RED_COLOR);
            onIconLabel.setBackground(colorComposite.getBackground());
            manageViewer.getControl().setEnabled(false);
            manageProjectsButton.setEnabled(false);
            projectViewer.getControl().setEnabled(false);
            openProjectBtn.setEnabled(false);
            if (fillProjectsBtn != null) {
                fillProjectsBtn.setEnabled(true);
            }
            if (branchesViewer != null) {
                branchesViewer.getControl().setEnabled(false);
            }
            statusLabel.setText(errorMsg);
            statusLabel.setBackground(RED_COLOR);
            statusLabel.setForeground(WHITE_COLOR);
            Font font = new Font(null, LoginComposite.FONT_ARIAL, 9, SWT.BOLD);// Arial courier
            statusLabel.setFont(font);
        } else if (getConnection() != null && projectViewer != null && projectViewer.getInput() == null) {
            manageViewer.getControl().setEnabled(false);
            manageProjectsButton.setEnabled(false);
            projectViewer.getControl().setEnabled(false);
            openProjectBtn.setEnabled(false);
            if (fillProjectsBtn != null) {
                fillProjectsBtn.setEnabled(true);
            }
            if (branchesViewer != null) {
                branchesViewer.getControl().setEnabled(false);
            }
            statusLabel.setText(Messages.getString("LoginComposite.ConnectionIncompletedMessage")); //$NON-NLS-1$
            statusLabel.setBackground(RED_COLOR);
            statusLabel.setForeground(WHITE_COLOR);
            Font font = new Font(null, LoginComposite.FONT_ARIAL, 9, SWT.BOLD);// Arial courier
            statusLabel.setFont(font);
            restartBut.setVisible(false);
        } else if (!isWorkSpaceSame()) {
            manageViewer.getControl().setEnabled(false);
            connectionsViewer.getControl().setEnabled(false);
            manageProjectsButton.setEnabled(false);
            openProjectBtn.setEnabled(false);
            if (projectViewer != null) {
                projectViewer.getControl().setEnabled(false);
            }
            if (fillProjectsBtn != null) {
                fillProjectsBtn.setEnabled(false);
            }
            if (branchesViewer != null) {
                branchesViewer.getControl().setEnabled(false);
            }
            // warningLabel.setVisible(true);
            restartBut.setVisible(true);
        } else if (inuse) {
            manageViewer.getControl().setEnabled(false);
            manageProjectsButton.setEnabled(false);
            openProjectBtn.setEnabled(false);
            //warningLabel.setText(Messages.getString("LoginComposite.Workspace_inuse")); //$NON-NLS-1$
            // warningLabel.setVisible(true);
            restartBut.setVisible(false);
            if (fillProjectsBtn != null) {
                fillProjectsBtn.setEnabled(false);
            }
            if (branchesViewer != null) {
                branchesViewer.getControl().setEnabled(false);
            }
        } else {
            manageViewer.getControl().setEnabled(true);
            manageProjectsButton.setEnabled(true);

            if (projectViewer != null) {
                final Object input = projectViewer.getInput();
                boolean enabled = input != null && ((input instanceof Project[]) && ((Project[]) input).length > 0);

                openProjectBtn.setEnabled(enabled);
            }
            if (fillProjectsBtn != null) {
                fillProjectsBtn.setEnabled(true);
            }
            if (PluginChecker.isSVNProviderPluginLoaded() && branchesViewer != null) {
                branchesViewer.getControl().setEnabled(true);
            }
            restartBut.setVisible(false);
        }
        if (PluginChecker.isSVNProviderPluginLoaded() && !localConn) {
            manageViewer.getControl().setEnabled(true);
            manageProjectsButton.setEnabled(true);
        }
    }

    private void unpopulateProjectList() {
        if (projectViewer != null) {
            projectViewer.setInput(null);
            projectViewer.getControl().setEnabled(false);
        }
        if (branchesViewer != null) {
            branchesViewer.getControl().setEnabled(false);
        }
    }

    public RepositoryContext getRepositoryContext() {
        RepositoryContext repositoryContext = new RepositoryContext();
        repositoryContext.setUser(getUser());
        Project project = getProject();
        repositoryContext.setProject(project);
        if (getConnection() != null) {
            repositoryContext.setFields(getConnection().getDynamicFields());
        }
        repositoryContext.setClearPassword(passwordText.getText());
        String branch = getBranch();
        if (project != null) {
            String branchKey = IProxyRepositoryFactory.BRANCH_SELECTION + SVNConstant.UNDER_LINE_CHAR
                    + project.getTechnicalLabel();
            if (branch != null) {
                repositoryContext.getFields().put(branchKey, branch);
            } else {
                repositoryContext.getFields().put(branchKey, SVNConstant.EMPTY);
            }
        }
        return repositoryContext;
    }

    private void setRepositoryContextInContext() {
        Context ctx = CorePlugin.getContext();
        ctx.putProperty(Context.REPOSITORY_CONTEXT_KEY, getRepositoryContext());
    }

    protected void populateProjectList() {
        Project[] projects = null;
        if (getConnection() != null) {
            String user2 = getConnection().getUser();
            String repositoryId2 = getConnection().getRepositoryId();
            String workSpace = getConnection().getWorkSpace();
            String name = getConnection().getName();
            if (user2 != null && !"".equals(user2) && repositoryId2 != null && !"".equals(repositoryId2) && workSpace != null //$NON-NLS-1$ //$NON-NLS-2$
                    && !"".equals(workSpace) && name != null && !"".equals(name)) { //$NON-NLS-1$ //$NON-NLS-2$
                boolean valid = Pattern.matches(RepositoryConstants.MAIL_PATTERN, user2);
                if (valid && RepositoryConstants.REPOSITORY_REMOTE_ID.equals(repositoryId2)) {
                    String url = getConnection().getDynamicFields().get(RepositoryConstants.REPOSITORY_URL);
                    valid = url != null || !"".equals(url); //$NON-NLS-1$
                }

                getConnection().setComplete(valid);
            }
        }

        if (getConnection() == null) {
            return;
        }
        ProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();
        repositoryFactory.setRepositoryFactoryFromProvider(RepositoryFactoryProvider.getRepositoriyById(getConnection()
                .getRepositoryId()));
        if (!getConnection().isComplete()) {
            return;
        }

        boolean initialized = false;

        final String newLine = ":\n"; //$NON-NLS-1$
        try {
            try {
                repositoryFactory.checkAvailability();
            } catch (WarningException e) {
                String warnings = e.getMessage();
                if (warnings != null && !warnings.equals(lastWarnings)) {
                    lastWarnings = warnings;
                    MessageDialog.openWarning(getShell(), "Warning", warnings); //$NON-NLS-1$
                }
            }

            try {
                IRunnableWithProgress op = new IRunnableWithProgress() {

                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        try {
                            ProxyRepositoryFactory.getInstance().initialize();
                        } catch (PersistenceException e) {
                            throw new InvocationTargetException(e);
                        }
                    }
                };
                new ProgressMonitorDialog(getShell()).run(true, false, op);
            } catch (InvocationTargetException e) {
                throw (Exception) e.getTargetException();
            } catch (InterruptedException e) {
            }

            initialized = true;
        } catch (Exception e) {
            projects = new Project[0];

            MessageDialog.openError(getShell(), Messages.getString("LoginComposite.errorTitle"), //$NON-NLS-1$
                    Messages.getString("LoginComposite.errorMessages1") + newLine + e.getMessage()); //$NON-NLS-1$
        }

        if (initialized) {
            try {
                projects = repositoryFactory.readProject();
                Arrays.sort(projects, new Comparator<Project>() {

                    public int compare(Project p1, Project p2) {
                        return p1.getLabel().compareTo(p2.getLabel());
                    }

                });
            } catch (PersistenceException e) {
                projects = new Project[0];

                MessageDialog.openError(getShell(), Messages.getString("LoginComposite.errorTitle"), //$NON-NLS-1$
                        Messages.getString("LoginComposite.errorMessages1") + newLine + e.getMessage()); //$NON-NLS-1$
            } catch (BusinessException e) {
                projects = new Project[0];

                MessageDialog.openError(getShell(), Messages.getString("LoginComposite.errorTitle"), //$NON-NLS-1$
                        Messages.getString("LoginComposite.errorMessages1") + newLine + e.getMessage()); //$NON-NLS-1$
            }
        }
        if (projectViewer != null) {
            projectViewer.setInput(projects);
        }
        // importDemoProjectAction.setExistingProjects(projects);
        // if (PluginChecker.isTIS()) {
        if (projects.length > 0) {
            // Try to select the last recently used project
            selectLastUsedProject();

            if (projectViewer != null) {
                projectViewer.getControl().setEnabled(true);
            }
            if (branchesViewer != null) {
                branchesViewer.getControl().setEnabled(true);
            }
        } else {
            if (projectViewer != null) {
                projectViewer.getControl().setEnabled(false);
            }
            if (branchesViewer != null) {
                branchesViewer.getControl().setEnabled(false);
            }
        }
        // }
        // updateSandboxButton();

        // if (isTisRemote()) {
        // ManageItem[] manageElements = getManageElements();
        // List<ManageItem> toReturn = Arrays.asList(manageElements);
        // boolean enableSandboxProject = false;
        // try {
        // enableSandboxProject = ProxyRepositoryFactory.getInstance().enableSandboxProject();
        // } catch (PersistenceException e) {
        // e.printStackTrace();
        // }
        // // Sendbox for Tis_Remote
        // if (enableSandboxProject) {
        //                toReturn.add(new ManageItem("Create sandbox project") { //$NON-NLS-1$
        //
        // @Override
        // public void run() {
        // createSendboxProject();
        // }
        //
        // });
        // }
        // manageViewer.setInput(getManageElements());
        // }
    }

    protected void populateTOSProjectList() {
        Project[] projects = null;
        if (getConnection() != null) {
            String user2 = getConnection().getUser();
            String repositoryId2 = getConnection().getRepositoryId();
            String workSpace = getConnection().getWorkSpace();
            String name = getConnection().getName();
            if (user2 != null && !"".equals(user2) && repositoryId2 != null && !"".equals(repositoryId2) && workSpace != null //$NON-NLS-1$ //$NON-NLS-2$
                    && !"".equals(workSpace) && name != null && !"".equals(name)) { //$NON-NLS-1$ //$NON-NLS-2$
                boolean valid = Pattern.matches(RepositoryConstants.MAIL_PATTERN, user2);
                if (valid && RepositoryConstants.REPOSITORY_REMOTE_ID.equals(repositoryId2)) {
                    String url = getConnection().getDynamicFields().get(RepositoryConstants.REPOSITORY_URL);
                    valid = url != null || !"".equals(url); //$NON-NLS-1$
                }
                getConnection().setComplete(valid);
            }
        }

        if (getConnection() == null) {
            return;
        }
        ProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();
        repositoryFactory.setRepositoryFactoryFromProvider(RepositoryFactoryProvider.getRepositoriyById(getConnection()
                .getRepositoryId()));
        if (!getConnection().isComplete()) {
            return;
        }

        boolean initialized = false;

        final String newLine = ":\n"; //$NON-NLS-1$
        try {
            try {
                repositoryFactory.checkAvailability();
            } catch (WarningException e) {
                String warnings = e.getMessage();
                if (warnings != null && !warnings.equals(lastWarnings)) {
                    lastWarnings = warnings;
                    MessageDialog.openWarning(getShell(), "Warning", warnings); //$NON-NLS-1$
                }
            }

            try {
                IRunnableWithProgress op = new IRunnableWithProgress() {

                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        try {
                            ProxyRepositoryFactory.getInstance().initialize();
                        } catch (PersistenceException e) {
                            throw new InvocationTargetException(e);
                        }
                    }
                };
                new ProgressMonitorDialog(getShell()).run(true, false, op);
            } catch (InvocationTargetException e) {
                throw (Exception) e.getTargetException();
            } catch (InterruptedException e) {
            }

            initialized = true;
        } catch (Exception e) {
            projects = new Project[0];

            MessageDialog.openError(getShell(), Messages.getString("LoginComposite.errorTitle"), //$NON-NLS-1$
                    Messages.getString("LoginComposite.errorMessages1") + newLine + e.getMessage()); //$NON-NLS-1$
        }
    }

    /**
     * smallet Comment method "selectLastUsedProject".
     * 
     * @param projects
     */
    private void selectLastUsedProject() {
        if (projectViewer != null) {
            Project[] projects = (Project[]) projectViewer.getInput();
            PreferenceManipulator prefManipulator = new PreferenceManipulator(CorePlugin.getDefault().getPreferenceStore());
            String lastProject = prefManipulator.getLastProject();
            if (lastProject != null) {
                Project goodProject = null;
                for (int i = 0; goodProject == null && i < projects.length; i++) {
                    if (lastProject.equals(projects[i].getTechnicalLabel()) || lastProject.equals(projects[i].getLabel())) {
                        goodProject = projects[i];
                    }
                }

                if (goodProject == null && projects.length > 0) {
                    goodProject = projects[0];
                }

                if (goodProject != null) {
                    selectProject(goodProject);
                }
            }
        }
    }

    /**
     * smallet Comment method "selectProject".
     * 
     * @param goodProject
     */
    private void selectProject(Project goodProject) {
        projectViewer.setSelection(new StructuredSelection(new Object[] { goodProject }));
        // if (PluginChecker.isTIS()) {
        setBranchesSetting(goodProject, true);
        // }
        setRepositoryContextInContext();
    }

    private void selectProject(String projectName) {
        Project[] projects = (Project[]) projectViewer.getInput();
        for (Project current : projects) {
            if (current.getLabel().equals(projectName)) {
                selectProject(current);
                return;
            }
        }
    }

    private static ConnectionBean bean = null;

    public ConnectionBean getConnection() {
        IStructuredSelection sel = (IStructuredSelection) connectionsViewer.getSelection();
        ConnectionBean firstElement = (ConnectionBean) sel.getFirstElement();
        if (!PluginChecker.isSVNProviderPluginLoaded()) {
            if (bean == null) {
                bean = new ConnectionBean();
                bean.setName("Local"); //$NON-NLS-1$
                bean.setDescription("Default connection"); //$NON-NLS-1$
                bean.setPassword(""); //$NON-NLS-1$
                List<IRepositoryFactory> listRepository = getUsableRepositoryProvider();
                bean.setRepositoryId(listRepository.get(0).getId());
                bean.setUser("test@talend.com"); //$NON-NLS-1$
                bean.setWorkSpace(getRecentWorkSpace());
                bean.setComplete(true);
            }
            return bean;
        }
        return firstElement;
    }

    private List<IRepositoryFactory> getUsableRepositoryProvider() {
        List<IRepositoryFactory> availableRepositories = RepositoryFactoryProvider.getAvailableRepositories();

        List<IRepositoryFactory> result = new ArrayList<IRepositoryFactory>();
        for (IRepositoryFactory repositoryFactory : availableRepositories) {
            if (repositoryFactory.isDisplayToUser()) {
                result.add(repositoryFactory);
            }
        }
        return result;
    }

    private String getRecentWorkSpace() {
        String filePath = new Path(Platform.getInstanceLocation().getURL().getPath()).toFile().getPath();
        return filePath;
    }

    public User getUser() {
        User toReturn = PropertiesFactory.eINSTANCE.createUser();
        toReturn.setLogin(user.getText());
        // if (PluginChecker.isTIS()) {
        try {
            toReturn.setPassword(PasswordHelper.encryptPasswd(passwordText.getText()));
        } catch (NoSuchAlgorithmException e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }
        // }
        return toReturn;
    }

    public Project getProject() {
        Project project = null;
        if (projectViewer != null && !projectViewer.getSelection().isEmpty()) {
            IStructuredSelection sel = (IStructuredSelection) projectViewer.getSelection();
            project = (Project) sel.getFirstElement();
        }
        return project;
    }

    public boolean canFinish() {
        return validateFields() && validateProject();
    }

    /**
     * Label provider for Projects. <br/>
     * 
     * $Id: LoginComposite.java 86556 2012-07-02 03:34:31Z ldong $
     * 
     */
    private class ProjectLabelProvider extends LabelProvider {

        /**
         * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
         */
        @Override
        public String getText(Object element) {
            Project prj = (Project) element;
            String toReturn = prj.getLabel() + " - " + prj.getLanguage().getName(); //$NON-NLS-1$
            if (!prj.isLocal() && !isAuthenticationNeeded()) {
                toReturn += " (remote project in offline mode)"; //$NON-NLS-1$
            }
            return toReturn;
        }

    }

    /**
     * DOC smallet LoginComposite class global comment. Detailled comment <br/>
     * 
     * $Id: LoginComposite.java 86556 2012-07-02 03:34:31Z ldong $
     * 
     */
    private class ConnectionLabelProvider extends LabelProvider {

        /**
         * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
         */
        @Override
        public String getText(Object element) {
            ConnectionBean prj = (ConnectionBean) element;
            if (prj.isComplete()) {
                return prj.getName();
            } else {
                return prj.getName() + " (" + Messages.getString("connections.form.field.imcomplete") + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        }
    }

    /**
     * clear login values.
     */
    private void unpopulateRemoteLoginElements() {
        //        passwordText.setText(""); //$NON-NLS-1$
        // passwordText.setEnabled(false);
        // passwordText.setEditable(false);
        // passwordText.setBackground(GREY_COLOR);
        // checkBtn.setEnabled(false);
        populateProjectList();
    }

    /**
     * fill login valueswith default elements.
     */
    private void populateRemoteLoginElements() {

        // passwordText.setEnabled(true);
        // passwordText.setEditable(true);
        // passwordText.setBackground(WHITE_COLOR);
        // checkBtn.setEnabled(true);

        // if (userCombo.getText().length() == 0) {
        // PreferenceManipulator prefManipulator = new
        // PreferenceManipulator(CorePlugin.getDefault().getPreferenceStore());
        // selectLast(prefManipulator.getLastUser(), userCombo);
        // }
        unpopulateProjectList();
    }

    private boolean validateFields() {
        String errorMsg = null;
        boolean valid = true;
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        boolean usesMailCheck = brandingService.getBrandingConfiguration().isUseMailLoginCheck();
        boolean serverIsLocal = !isAuthenticationNeeded();
        if (valid && getConnection() == null) {
            valid = false;
            errorMsg = Messages.getString("LoginComposite.connectionEmpty"); //$NON-NLS-1$
        } else if (valid && !getConnection().isComplete()) {
            valid = false;
            errorMsg = Messages.getString("LoginComposite.connectionIncomplete"); //$NON-NLS-1$
        } else if (valid && !serverIsLocal && user.getText().length() == 0) {
            valid = false;
            errorMsg = ""; //$NON-NLS-1$
        } else if (valid && usesMailCheck && !Pattern.matches(RepositoryConstants.MAIL_PATTERN, getUser().getLogin())) {
            valid = false;
            errorMsg = ""; //$NON-NLS-1$
        }
        if (valid && !serverIsLocal && passwordText.getText().length() == 0) {
            valid = false;
            errorMsg = Messages.getString("LoginComposite.passwordEmpty"); //$NON-NLS-1$
        }

        if (!valid) {
            // setErrorMessage(errorMsg);
            // checkBtn.setEnabled(false);
        } else {
            // setErrorMessage(null);
            if (isAuthenticationNeeded()) {
                // checkBtn.setEnabled(true);
            }
        }
        return valid;
    }

    private boolean validateProject() {
        String errorMsg = null;
        boolean valid = true;
        if (projectViewer != null && projectViewer.getCombo().getText().length() == 0) {
            valid = false;
            errorMsg = Messages.getString("LoginComposite.projectEmpty"); //$NON-NLS-1$
        }

        // if (!valid) {
        // setErrorMessage(errorMsg);
        // } else {
        // setErrorMessage(null);
        // }
        return valid;
    }

    public List<ConnectionBean> getStoredConnections() {
        return this.storedConnections;
    }

    private ISVNProviderService getSVNService() {
        ISVNProviderService service = null;
        if (PluginChecker.isSVNProviderPluginLoaded()) {
            try {
                service = (ISVNProviderService) GlobalServiceRegister.getDefault().getService(ISVNProviderService.class);
            } catch (RuntimeException e) {
                // nothing to do
            }
        }
        return service;
    }

    public String getBranch() {
        Project project = getProject();
        if (branchesViewer != null && !branchesViewer.getSelection().isEmpty() && project != null) {
            IStructuredSelection ss = (IStructuredSelection) branchesViewer.getSelection();
            String branch = (String) ss.getFirstElement();
            /*
             * verify branches
             */
            // List<String> branchList = getProjectBranches(project);
            // if (branchList != null && branchList.contains(branch)) {
            return branch;
            // }
        }
        return null;
    }

    private void setBranchesSetting(Project project, boolean lastUsedBranch) {
        if (branchesViewer != null) {
            PreferenceManipulator prefManipulator = new PreferenceManipulator(CorePlugin.getDefault().getPreferenceStore());

            List<String> projectBranches = getProjectBranches(project);
            branchesViewer.setInput(projectBranches);
            if (!projectBranches.isEmpty()) {
                String branch = null;
                if (lastUsedBranch) {
                    String lastBranch = prefManipulator.getLastSVNBranch();
                    if (lastBranch != null && projectBranches.contains(lastBranch)) {
                        branch = lastBranch;
                    }
                }
                if (branch == null) {
                    branch = projectBranches.get(0); // trunk
                    prefManipulator.setLastSVNBranch(branch);
                }

                branchesViewer.setSelection(new StructuredSelection(new Object[] { branch }));

            } else {
                prefManipulator.setLastSVNBranch(SVNConstant.EMPTY);
            }
        }
        // hideBranchesView();
    }

    private List<String> getProjectBranches(Project p) {
        List<String> brancesList = new ArrayList<String>();
        ISVNProviderService svnService = getSVNService();
        if (p != null && svnService != null) {
            try {
                if (!p.isLocal() && svnService.isSVNProject(p)) {
                    brancesList.add(SVNConstant.NAME_TRUNK);
                    String[] branchList = svnService.getBranchList(p);
                    if (branchList != null) {
                        brancesList.addAll(Arrays.asList(branchList));
                    }

                }
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }
        return brancesList;
    }

    public int calStatusLabelFont(int defaultSize, String text) {
        int fontsize = defaultSize;
        if (text == null) {
            return fontsize;
        }
        final int length = text.length();
        if (length > 38 && length <= 45) {
            fontsize -= 1;
        } else if (length > 45 && length <= 52) {
            fontsize -= 2;
        } else if (length > 52 && length <= 59) {
            fontsize -= 3;
        } else if (length > 59 && length <= 67) {
            fontsize -= 4;
        } else if (length > 67) {
            fontsize -= 5;
        }
        return fontsize;
    }
}
