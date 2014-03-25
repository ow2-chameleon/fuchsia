package org.ow2.chameleon.fuchsia.testing.common;

import org.ow2.chameleon.fuchsia.testing.common.platform.GenericImportExporterPlatformTest;
import org.junit.Assert;
import org.junit.Test;
import org.ow2.chameleon.fuchsia.core.component.manager.DeclarationBinder;
import org.ow2.chameleon.fuchsia.core.declaration.Declaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;

/**
 * Basic Tests that can be done with DeclarationBinder.
 * @param <T>
 * @param <S>
 */
public abstract class GenericTest<T extends Declaration,S extends DeclarationBinder> extends GenericImportExporterPlatformTest<T,S> {

    @Test
    public void testValidDeclarations() throws Exception {
        try {
            for(T declaration:getValidDeclarations()){
                fuchsiaDeclarationBinder.useDeclaration(declaration);
            }
        }catch(BinderException be){
            Assert.fail("A BinderException should NOT have been thrown since not all information were provided");
        }
    }

    @Test
    public void testInvalidDeclaration() throws Exception {
        try {
            for(T declaration:getInvalidDeclarations()){
                fuchsiaDeclarationBinder.useDeclaration(declaration);
            }
            Assert.fail("A BinderException should have been thrown since not all information required were provided");
        }catch(BinderException be){
            //An exception for this case is normal, since not all information were provided
        }
    }

}
