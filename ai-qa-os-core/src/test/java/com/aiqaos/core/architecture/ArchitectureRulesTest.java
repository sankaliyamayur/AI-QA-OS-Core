package com.aiqaos.core.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

/**
 * DX-6: Workspace & Architecture Validators
 *
 * Automated ArchUnit test suite enforcing AI-QA-OS architectural integrity:
 * 1. Dependency Inversion: 'core' must depend strictly INWARD (no dependencies on brain, orchestration, execution, etc.)
 * 2. Constructor Injection: No @Autowired on fields
 * 3. Domain Model Isolation: Domain entities in core must not depend on Web controllers or Gateway filters
 */
public class ArchitectureRulesTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setUp() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.aiqaos");
    }

    @Test
    @DisplayName("DX-6: Core module must not depend on higher-level modules (brain, orchestration, execution, gateway)")
    void coreModuleShouldNotDependOnOuterModules() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.aiqaos.core..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "com.aiqaos.brain..",
                        "com.aiqaos.orchestration..",
                        "com.aiqaos.execution..",
                        "com.aiqaos.gateway..",
                        "com.aiqaos.healing..",
                        "com.aiqaos.learning.."
                );

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("DX-6: No field injection with @Autowired — enforce constructor injection across all modules")
    void noAutowiredFields() {
        ArchRule rule = noFields()
                .should().beAnnotatedWith("org.springframework.beans.factory.annotation.Autowired")
                .because("Constructor injection is required for immutability and testability across AI-QA-OS");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("DX-6: Model classes in core should not depend on Spring Web Controllers")
    void coreModelsShouldBeWebFree() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.aiqaos.core.model..")
                .should().dependOnClassesThat()
                .resideInAPackage("org.springframework.web..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("DX-6: Exception classes must reside in exception packages")
    void exceptionsShouldHaveStandardNamingAndHierarchy() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Exception")
                .should().resideInAPackage("..exception..");

        rule.check(importedClasses);
    }
}
