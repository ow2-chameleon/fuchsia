package org.ow2.chameleon.fuchsia.fool;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Unbind;
import org.osgi.framework.InvalidSyntaxException;
import org.ow2.chameleon.fuchsia.core.FuchsiaMediator;

@Component(name = "Fuchsia-FoolInitializer-Factory")
@Instantiate(name = "Fuchsia-FoolInitializer")
public class FoolInitializer {

    @Bind(aggregate = false, id = "fm")
    public void bindFuchsiaMediator(FuchsiaMediator fm) {
        try {
            fm.createLinker("FoolLinker").importDeclarationFilter("(*=*)").build();
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        } catch (MissingHandlerException e) {
            e.printStackTrace();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        } catch (UnacceptableConfiguration e) {
            e.printStackTrace();
        }

    }

    @Unbind(id = "fm")
    public void unbindFuchsiaMediator(FuchsiaMediator fm) {
        fm.destroyLinker("FoolLinker");

    }

}
