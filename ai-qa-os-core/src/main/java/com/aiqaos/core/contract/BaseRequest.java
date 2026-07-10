package com.aiqaos.core.contract;

import com.aiqaos.core.dto.BaseDTO;
import jakarta.validation.constraints.NotNull;

public abstract class BaseRequest implements BaseDTO {
    @NotNull
    private BaseMetadata metadata = new BaseMetadata();

    public BaseMetadata getMetadata() { return metadata; }
    public void setMetadata(BaseMetadata metadata) { this.metadata = metadata; }
}