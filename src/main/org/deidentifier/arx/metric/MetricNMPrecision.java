/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.metric;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Node;

/**
 * This class provides an implementation of a weighted precision metric as 
 * proposed in: <br>
 * Sweeney, L. (2002). Achieving k-anonymity privacy protection using generalization and suppression.<br> 
 * International Journal of Uncertainty Fuzziness and, 10(5), 2002.<br>
 * <br>
 * This metric will respect attribute weights defined in the configuration.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class MetricNMPrecision extends MetricWeighted<InformationLossDefault> {

    /** SVUID */
    private static final long serialVersionUID = -218192738838711533L;
    /** Height */
    private int[]             height;
    /** Number of cells*/
    private double            cells;

    /**
     * Creates a new instance
     */
    protected MetricNMPrecision() {
        super(false, false);
    }

    @Override
    public InformationLoss<?> createMaxInformationLoss() {
        return new InformationLossDefault(1);
    }

    @Override
    public InformationLoss<?> createMinInformationLoss() {
        return new InformationLossDefault(0);
    }

    @Override
    public String toString() {
        return "Non-Monotonic Precision";
    }
    
    @Override
    protected InformationLossDefault evaluateInternal(final Node node, final IHashGroupify g) {
        
        double total = 0d;
        double lowerBound = 0d;
        
        HashGroupifyEntry m = g.getFirstEntry();
        while (m != null) {
            if (m.count > 0) {
                for (int i = 0; i < height.length; i++) {
                    total += m.isNotOutlier ? (height[i] == 0 ? 0 : (double) m.key[i] / (double) height[i]) : 1d;
                    lowerBound += height[i] == 0 ? 0 : (double) m.key[i] / (double) height[i];
                }
            }
            m = m.nextOrdered;
        }
        
        total /= cells;
        
        // Return
        return new InformationLossDefault(total, lowerBound);
    }

    @Override
    protected void initializeInternal(final DataDefinition definition,
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {
        super.initializeInternal(definition, input, hierarchies, config);

        // Initialize maximum levels
        height = new int[hierarchies.length];
        for (int j = 0; j < height.length; j++) {
            height[j] = hierarchies[j].getArray()[0].length - 1;
        }
        this.cells = (double)input.getDataLength() * (double)input.getHeader().length;
    }
}
