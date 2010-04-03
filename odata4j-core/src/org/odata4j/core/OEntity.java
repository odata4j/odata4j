package org.odata4j.core;

import java.util.List;

public interface OEntity {

    public abstract List<OProperty<?>> getProperties();

    public abstract List<OProperty<?>> getKeyProperties();
}
