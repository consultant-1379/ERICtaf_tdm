package com.ericsson.cifwk.tdm.application.util;

import static com.ericsson.cifwk.tdm.application.util.DataParser.readTextFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.io.ByteStreams;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public final class JsonParser {

    private static ObjectMapper mapper = new ObjectMapper();
    private static TypeFactory typeFactory = mapper.getTypeFactory();
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonParser.class);

    private JsonParser() {
    }

    public static <T> T parseObjectFile(String file, Class<T> clazz) {
        return parseObject(readTextFile(file), clazz);
    }

    public static <T> List<T> parseListFile(String file, Class<T> clazz) {
        if (file.contentEquals("data/users-customers.json")) {
            Process p;
            JSONArray array = new JSONArray();
            try {
                p = Runtime.getRuntime().exec("sh /var/log/tdmldap/ldapsearch.sh");
                p.waitFor();
                String stdOut = new String(ByteStreams.toByteArray(p.getInputStream()));
                BufferedReader bufReader = new BufferedReader(new StringReader(stdOut));
                String line = null;
                int count = 1;
                while ((line = bufReader.readLine()) != null) {
                    JSONObject item = new JSONObject();
                    JSONObject itemin = new JSONObject();
                    JSONObject iteminin = new JSONObject();
                    JSONArray arrayin = new JSONArray();
                    itemin.put("contextId", "systemId-1");
                    iteminin.put("name", "ROLE_TEST_MANAGER");
                    itemin.put("roleBean", iteminin);
                    arrayin.add(itemin);
                    item.put("roles", arrayin);
                    item.put("email", line);
                    item.put("username", line);
                    item.put("id", count);
                    array.add(item);
                    count++;
                }
            } catch (Exception e) {
                LOGGER.info("Executing ldapsearch failed e:" + e);
            }
            return parseList(array.toJSONString(), clazz);
        } else {
            return parseList(readTextFile(file), clazz);
        }
    }

    public static <T> T parseObject(String content, Class<T> clazz) {
        return parseType(content, typeFactory.constructType(clazz));
    }

    public static <T> List<T> parseList(String content, Class<T> clazz) {
        return parseType(content, typeFactory.constructCollectionType(List.class, clazz));
    }

    public static String toJson(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            String message = String.format("Error during converting '%s' to JSON", value);
            throw new RuntimeException(message, e); // NOSONAR
        }
    }

    private static <T> T parseType(String content, JavaType valueType) {
        try {
            return mapper.readValue(content, valueType);
        } catch (IOException e) {
            String message = String.format("Error during parsing string '%s' to JSON", content);
            throw new RuntimeException(message, e); // NOSONAR
        }
    }
}
