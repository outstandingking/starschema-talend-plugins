package org.talend.core.repository;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ICoreService;

public class GeneratedJetEmitersLoginTask implements IRunnableWithProgress {

    private static Logger log = Logger.getLogger(GeneratedJetEmitersLoginTask.class);

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        log.info("Generating JetEmiters"); //$NON-NLS-1$
        SubMonitor subMonitor = SubMonitor.convert(monitor, 1);
        subMonitor.setTaskName("Generating Jet Emiters.");
        ICoreService coreService = (ICoreService) GlobalServiceRegister.getDefault().getService(ICoreService.class);
        coreService.initializeTemplates();
        subMonitor.done();

    }

}
