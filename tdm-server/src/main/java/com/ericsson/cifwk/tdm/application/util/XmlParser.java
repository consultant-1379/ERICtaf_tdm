package com.ericsson.cifwk.tdm.application.util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;

import static com.ericsson.cifwk.tdm.application.util.DataParser.readTextFile;

public final class XmlParser {

    private XmlParser() {
    }

    public static <T> T parseObjectFile(String fileToLoadFrom, Class<T> clazz) {
        String contentXml = readTextFile(fileToLoadFrom);
        return parseXmlStringToObject(clazz, contentXml);
    }

    private static <T> T parseXmlStringToObject(Class<T> clazz, String contentXml) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            StringReader reader = new StringReader(contentXml);
            T obj = clazz.cast(unmarshaller.unmarshal(reader));
            return obj;
        } catch (JAXBException e) {
            throw new RuntimeException("Error during unmarshalling XML file", e); // NOSONAR
        }
    }
}
