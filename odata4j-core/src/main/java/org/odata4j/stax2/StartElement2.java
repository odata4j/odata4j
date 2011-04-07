package org.odata4j.stax2;

public interface StartElement2 {

    QName2 getName();

    Attribute2 getAttributeByName(QName2 name);
    Attribute2 getAttributeByName(String name);
}
