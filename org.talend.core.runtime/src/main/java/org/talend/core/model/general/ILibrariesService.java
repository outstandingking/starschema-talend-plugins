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
package org.talend.core.model.general;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.talend.commons.exception.BusinessException;
import org.talend.core.IService;
import org.talend.core.model.general.ModuleNeeded.ELibraryInstallStatus;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.Problem;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 * 
 * $Id$
 * 
 */
public interface ILibrariesService extends IService {

    public static final String SOURCE_PERL_ROUTINES_FOLDER = "routines"; //$NON-NLS-1$

    public static final String SOURCE_SQLPATTERN_FOLDER = "sqltemplates"; //$NON-NLS-1$

    public static final String SQLPATTERN_FILE_SUFFIX = ".sqltemplate"; //$NON-NLS-1$

    public static final String TEMPLATE_SUFFIX = ".template"; //$NON-NLS-1$

    public List<URL> getSystemRoutines();

    public List<URL> getSystemSQLPatterns();

    public List<URL> getTalendRoutinesFolder() throws IOException;

    public List<URL> getTalendBeansFolder() throws IOException;

    public List<URL> getTalendRoutines();

    public String getPerlLibrariesPath();

    public void deployLibrary(URL source) throws IOException;

    public void undeployLibrary(String jarName) throws IOException;

    public URL getRoutineTemplate();

    public URL getBeanTemplate();

    public URL getSqlPatternTemplate();

    public ELibraryInstallStatus getLibraryStatus(String libName) throws BusinessException;

    public List<Problem> getProblems(INode node, IElement element);

    public void syncLibraries(IProgressMonitor... monitorWrap);

    public void syncLibrariesFromLibs(IProgressMonitor... monitorWrap);

    public void checkLibraries();

    public void addChangeLibrariesListener(IChangedLibrariesListener listener);

    public void removeChangeLibrariesListener(IChangedLibrariesListener listener);

    public void resetModulesNeeded();

    public void updateModulesNeededForCurrentJob(IProcess process);

    public boolean isLibSynchronized();

    /**
     * Listener used to fire that libraries status has been changed (new lib or new check install).
     * 
     * $Id$
     * 
     */
    public interface IChangedLibrariesListener {

        public void afterChangingLibraries();
    }
}
