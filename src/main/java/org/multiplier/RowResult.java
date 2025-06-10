package org.multiplier;

import java.io.Serializable;

public class RowResult implements Serializable {
    private final int rowIndex;
    private final double[] rowData;

    public RowResult(int rowIndex, double[] rowData) {
        this.rowIndex = rowIndex;
        this.rowData = rowData;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public double[] getRowData() {
        return rowData;
    }
}
