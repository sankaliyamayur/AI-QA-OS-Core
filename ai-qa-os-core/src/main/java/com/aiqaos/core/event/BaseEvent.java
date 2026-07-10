package com.aiqaos.core.event;

import com.aiqaos.core.contract.BaseMetadata;
import java.io.Serializable;

public abstract class BaseEvent implements Serializable {
    private BaseMetadata metadata = new BaseMetadata();

    public BaseMetadata getMetadata() { return metadata; }
    public void setMetadata(BaseMetadata metadata) { this.metadata = metadata; }
}