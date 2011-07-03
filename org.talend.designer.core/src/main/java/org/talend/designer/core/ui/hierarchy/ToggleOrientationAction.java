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
package org.talend.designer.core.ui.hierarchy;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.typehierarchy.TypeHierarchyMessages;
import org.eclipse.jface.action.Action;
import org.talend.designer.core.ui.IJobHierarchyViewPart;

/**
 * Toggles the orientationof the layout of the type hierarchy
 */
public class ToggleOrientationAction extends Action {

    private IJobHierarchyViewPart fView;

    private int fActionOrientation;

    public ToggleOrientationAction(IJobHierarchyViewPart v, int orientation) {
        super("", AS_RADIO_BUTTON); //$NON-NLS-1$
        if (orientation == IJobHierarchyViewPart.VIEW_LAYOUT_HORIZONTAL) {
            setText(TypeHierarchyMessages.ToggleOrientationAction_horizontal_label);
            setDescription(TypeHierarchyMessages.ToggleOrientationAction_horizontal_description);
            setToolTipText(TypeHierarchyMessages.ToggleOrientationAction_horizontal_tooltip);
            JavaPluginImages.setLocalImageDescriptors(this, "th_horizontal.gif"); //$NON-NLS-1$
        } else if (orientation == IJobHierarchyViewPart.VIEW_LAYOUT_VERTICAL) {
            setText(TypeHierarchyMessages.ToggleOrientationAction_vertical_label);
            setDescription(TypeHierarchyMessages.ToggleOrientationAction_vertical_description);
            setToolTipText(TypeHierarchyMessages.ToggleOrientationAction_vertical_tooltip);
            JavaPluginImages.setLocalImageDescriptors(this, "th_vertical.gif"); //$NON-NLS-1$
        } else if (orientation == IJobHierarchyViewPart.VIEW_LAYOUT_AUTOMATIC) {
            setText(TypeHierarchyMessages.ToggleOrientationAction_automatic_label);
            setDescription(TypeHierarchyMessages.ToggleOrientationAction_automatic_description);
            setToolTipText(TypeHierarchyMessages.ToggleOrientationAction_automatic_tooltip);
            JavaPluginImages.setLocalImageDescriptors(this, "th_automatic.gif"); //$NON-NLS-1$
        } else if (orientation == IJobHierarchyViewPart.VIEW_LAYOUT_SINGLE) {
            setText(TypeHierarchyMessages.ToggleOrientationAction_single_label);
            setDescription(TypeHierarchyMessages.ToggleOrientationAction_single_description);
            setToolTipText(TypeHierarchyMessages.ToggleOrientationAction_single_tooltip);
            JavaPluginImages.setLocalImageDescriptors(this, "th_single.gif"); //$NON-NLS-1$
        } else {
            Assert.isTrue(false);
        }
        fView = v;
        fActionOrientation = orientation;
    }

    public int getOrientation() {
        return fActionOrientation;
    }

    /*
     * @see Action#actionPerformed
     */
    public void run() {
        if (isChecked()) {
            fView.setViewLayout(fActionOrientation);
        }
    }

}
