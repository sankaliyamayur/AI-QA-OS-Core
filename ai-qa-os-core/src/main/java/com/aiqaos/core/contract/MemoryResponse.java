package com.aiqaos.core.contract;

import java.util.ArrayList;
import java.util.List;

public class MemoryResponse extends BaseResponse {
    private List<String> historicalElements = new ArrayList<>();

    public List<String> getHistoricalElements() { return historicalElements; }
    public void setHistoricalElements(List<String> historicalElements) { this.historicalElements = historicalElements; }
}