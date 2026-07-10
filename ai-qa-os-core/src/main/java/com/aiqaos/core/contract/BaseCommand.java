package com.aiqaos.core.contract;

import com.aiqaos.core.dto.BaseDTO;

public abstract class BaseCommand implements BaseDTO {
    private BaseMetadata metadata = new BaseMetadata();

    public BaseMetadata getMetadata() { return metadata; }
    public void setMetadata(BaseMetadata metadata) { this.metadata = metadata; }
}