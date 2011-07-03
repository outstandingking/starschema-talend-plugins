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
package org.talend.core;

import org.talend.core.model.process.IElementParameter;

/**
 * DOC bZhou class global comment. Detailled comment
 */
public interface ITDQPatternService extends IService {

    /**
     * This method is used in tis to get the patterns value in TDQ.
     * 
     * for the return result, it's a three dimensionality string array. the first is represented for all patterns. the
     * second is represented for expressions for each pattern. the third is one array included three parts:
     * String[][][0] = pattern name String[][][1] = expression body String[][][2] = expression language String[][][3] =
     * pattern path
     * 
     * @return patterns defined in TDQ as String arrays.
     */
    public String[][][] retrieveTDQPatterns();

    /**
     * This method is to override the patter list in a pattern matching component.
     * 
     * @param dbTypeParam
     * @param patternParam
     */
    public void overridePatternList(IElementParameter dbTypeParam, IElementParameter patternParam);

}
