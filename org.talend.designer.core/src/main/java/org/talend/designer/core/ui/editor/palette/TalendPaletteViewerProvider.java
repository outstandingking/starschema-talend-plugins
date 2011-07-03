// ============================================================================
//
// Copyright (C) 2006-2011 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.editor.palette;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.gef.ui.parts.GraphicalViewerImpl;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * 
 */
public class TalendPaletteViewerProvider extends PaletteViewerProvider {

    private static Logger log = Logger.getLogger(TalendPaletteViewerProvider.class);

    public TalendPaletteViewerProvider(EditDomain graphicalViewerDomain) {
        super(graphicalViewerDomain);
    }

    @Override
    public PaletteViewer createPaletteViewer(Composite parent) {
        // removed by 10304
        // if (SystemUtils.IS_OS_MAC_OSX || SystemUtils.IS_OS_MAC) {
        // // PTDO need check it later and fix the bug on MacOS.
        // return super.createPaletteViewer(parent);
        // }
        TalendPaletteViewer pViewer = new TalendPaletteViewer(this.getEditDomain());

        /*************************************/
        // FIXME ggu
        // use the following codes to replace this pViewer.createControl(parent);
        try {
            // reflect the mothed for supper class PaletteViewer/GraphicalViewerImpl
            Method getLightweightSystemMethod = GraphicalViewerImpl.class.getDeclaredMethod("getLightweightSystem"); //$NON-NLS-1$
            getLightweightSystemMethod.setAccessible(true);
            Object lws = getLightweightSystemMethod.invoke(pViewer);
            //
            FigureCanvas canvas = new TalendFigureCanvas(parent, (LightweightSystem) lws, pViewer);
            Method setControlMethod = EditPartViewer.class.getDeclaredMethod("setControl", Control.class); //$NON-NLS-1$
            setControlMethod.invoke(pViewer, canvas);
            //
            Method installRootFigureMethod = ScrollingGraphicalViewer.class.getDeclaredMethod("installRootFigure"); //$NON-NLS-1$
            installRootFigureMethod.setAccessible(true);
            installRootFigureMethod.invoke(pViewer);
        } catch (SecurityException e) {
            handleReflectionFailure(e);
        } catch (NoSuchMethodException e) {
            handleReflectionFailure(e);
        } catch (IllegalArgumentException e) {
            handleReflectionFailure(e);
        } catch (IllegalAccessException e) {
            handleReflectionFailure(e);
        } catch (InvocationTargetException e) {
            handleReflectionFailure(e);
        }
        /*************************************/

        configurePaletteViewer(pViewer);
        hookPaletteViewer(pViewer);
        return pViewer;
    }

    /**
     * log the exception and throw a Runtime exception cause this is serious.
     * 
     * @param iae
     */
    private static void handleReflectionFailure(Exception e) {
        // our hook is not working so say it
        log.error("Draw2D Canvas hook failed", e);
        throw new RuntimeException(e);

    }

    @Override
    protected void configurePaletteViewer(PaletteViewer pViewer) {
        pViewer.setContextMenu(new TalendPaletteContextMenuProvider(pViewer));
        pViewer.addDragSourceListener(new TalendPaletteDragSourceListener(pViewer));
    }

}
