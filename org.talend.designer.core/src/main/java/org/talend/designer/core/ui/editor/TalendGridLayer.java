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
package org.talend.designer.core.ui.editor;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.editparts.GridLayer;
import org.eclipse.swt.graphics.Color;

/**
 * Grid that will be used for the designer. (modification of the default grid to have black points)
 * 
 * $Id: TalendGridLayer.java 7038 2007-11-15 14:05:48Z plegall $
 * 
 */
public class TalendGridLayer extends GridLayer {

    public static final Color GRID_COLOR = ColorConstants.black;

    public TalendGridLayer() {
        super();
        setForegroundColor(GRID_COLOR);
    }

    @Override
    protected void paintGrid(Graphics g) {
        paintGrid(g, this, origin, gridX, gridY);
    }

    protected void paintGrid(Graphics g, IFigure f, org.eclipse.draw2d.geometry.Point origin, int distanceX, int distanceY) {
        Rectangle clip = g.getClip(Rectangle.SINGLETON);

        if (distanceX > 0 && distanceY > 0) {
            if (origin.x >= clip.x) {
                while (origin.x - distanceX >= clip.x) {
                    origin.x -= distanceX;
                }
            } else {
                while (origin.x < clip.x) {
                    origin.x += distanceX;
                }
            }

            if (origin.y >= clip.y) {
                while (origin.y - distanceY >= clip.y) {
                    origin.y -= distanceY;
                }
            } else {
                while (origin.y < clip.y) {
                    origin.y += distanceY;
                }
            }

            for (int i = origin.x; i < clip.x + clip.width; i += distanceX) {
                for (int j = origin.y; j < clip.y + clip.height; j += distanceY) {
                    g.drawPoint(i, j);
                }
            }
        }
    }
}
