package com.bazzi.core.generic;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.MapConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class DefineMapConverter extends MapConverter {

    private static final String ATTR_KEY = "key";
    private static final String ATTR_VALUE = "value";
    private static final String ENTRY_NAME = "entry";

    public DefineMapConverter(Mapper mapper) {
        super(mapper);
    }

    public boolean canConvert(Class type) {
        return type.equals(HashMap.class)
                || type.equals(Hashtable.class)
                || type.getName().equals("java.util.LinkedHashMap")
                || type.getName().equals("java.util.concurrent.ConcurrentHashMap")
                || type.getName().equals("sun.font.AttributeMap");// Used by java.awt.Font in JDK 6
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Map<?, ?> map = (Map<?, ?>) source;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            writer.startNode(ENTRY_NAME);
            writer.addAttribute(ATTR_KEY, entry.getKey().toString());
            writer.addAttribute(ATTR_VALUE, entry.getValue().toString());
//            writer.setValue(entry.getValue().toString());
            writer.endNode();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Map map = (Map) createCollection(context.getRequiredType());
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            Object key = reader.getAttribute(ATTR_KEY);
            Object value = reader.getAttribute(ATTR_VALUE);
//            Object value = reader.getValue();
            map.put(key, value);
            reader.moveUp();
        }
        return map;
    }
}

