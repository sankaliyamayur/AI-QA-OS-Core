package com.aiqaos.core.contract;

import java.io.Serializable;

public abstract class BaseContext implements Serializable {
    private BaseMetadata metadata = new BaseMetadata();

    public BaseMetadata getMetadata() { return metadata; }
    public void setMetadata(BaseMetadata metadata) { this.metadata = metadata; }
}