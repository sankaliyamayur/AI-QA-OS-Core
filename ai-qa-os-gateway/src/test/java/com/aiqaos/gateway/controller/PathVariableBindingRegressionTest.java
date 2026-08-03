package com.aiqaos.gateway.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import org.junit.jupiter.api.Test;

/**
 * Regression (live E2E): every {@code @PathVariable}/{@code @RequestParam} endpoint that relies on the
 * inferred parameter name (e.g. {@code @PathVariable String id}) 500s at runtime — "Name for argument of
 * type [...] not specified" — unless the module is compiled with javac's {@code -parameters} flag. This
 * project has no {@code spring-boot-starter-parent} to supply it, so it is set explicitly in the root
 * {@code maven-compiler-plugin} config. This test fails if that flag is ever dropped: {@link
 * Parameter#isNamePresent()} is true only when the MethodParameters attribute was emitted.
 */
class PathVariableBindingRegressionTest {

    @Test
    void controllerMethodParameterNamesAreRetainedInBytecode() throws Exception {
        Method getStatus = ExecutionController.class.getMethod("getStatus", String.class);
        Parameter idParam = getStatus.getParameters()[0];

        assertThat(idParam.isNamePresent())
                .as("compiled without -parameters — @PathVariable binding by inferred name will 500 at runtime")
                .isTrue();
        assertThat(idParam.getName()).isEqualTo("id");
    }
}
