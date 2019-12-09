package com.alvin.example.watchlog.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alvin.example.watchlog.advice.annotation.Sensitive;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static java.lang.String.format;

/**
 * datetime 2019/12/7 12:18
 *
 * @author zhouwenxiang
 */
public class ObjectViewUtil {

    private static int MAX_OBJECT_LENGTH = 10 * 1024 * 1024;
    private static String TAB = "    ";
    private static String ENTER = "\n";

    private static Map<Byte, String> ASCII_MAP = new HashMap<>();

    static {
        ASCII_MAP.put((byte) 0, "NUL");
        ASCII_MAP.put((byte) 1, "SOH");
        ASCII_MAP.put((byte) 2, "STX");
        ASCII_MAP.put((byte) 3, "ETX");
        ASCII_MAP.put((byte) 4, "EOT");
        ASCII_MAP.put((byte) 5, "ENQ");
        ASCII_MAP.put((byte) 6, "ACK");
        ASCII_MAP.put((byte) 7, "BEL");
        ASCII_MAP.put((byte) 8, "BS");
        ASCII_MAP.put((byte) 9, "HT");
        ASCII_MAP.put((byte) 10, "LF");
        ASCII_MAP.put((byte) 11, "VT");
        ASCII_MAP.put((byte) 12, "FF");
        ASCII_MAP.put((byte) 13, "CR");
        ASCII_MAP.put((byte) 14, "SO");
        ASCII_MAP.put((byte) 15, "SI");
        ASCII_MAP.put((byte) 16, "DLE");
        ASCII_MAP.put((byte) 17, "DC1");
        ASCII_MAP.put((byte) 18, "DC2");
        ASCII_MAP.put((byte) 19, "DC3");
        ASCII_MAP.put((byte) 20, "DC4");
        ASCII_MAP.put((byte) 21, "NAK");
        ASCII_MAP.put((byte) 22, "SYN");
        ASCII_MAP.put((byte) 23, "ETB");
        ASCII_MAP.put((byte) 24, "CAN");
        ASCII_MAP.put((byte) 25, "EM");
        ASCII_MAP.put((byte) 26, "SUB");
        ASCII_MAP.put((byte) 27, "ESC");
        ASCII_MAP.put((byte) 28, "FS");
        ASCII_MAP.put((byte) 29, "GS");
        ASCII_MAP.put((byte) 30, "RS");
        ASCII_MAP.put((byte) 31, "US");
        ASCII_MAP.put((byte) 127, "DEL");
    }

    public static String draw(Object object) {
        return draw(object, new ObjectViewConfig());
    }

    public static String draw(Object object, ObjectViewConfig config) {
        StringBuilder buf = new StringBuilder();
        try {
            if (config.isUsingJson()) {
                if (config.isIgnoreNullField()) {
                    return JSON.toJSONString(object, new SensitizeFilter(), SerializerFeature.IgnoreErrorGetter);
                }
                return JSON.toJSONString(object, new SensitizeFilter(), SerializerFeature.IgnoreErrorGetter, SerializerFeature.WriteMapNullValue);
            }
            renderObject(object, buf, config);
            return buf.toString();
        } catch (ObjectTooLargeException e) {
            buf.append(" Object size exceeds size limit: ").append(MAX_OBJECT_LENGTH);
            return buf.toString();
        } catch (Throwable t) {
            return "ERROR DATA!!!";
        }
    }

    private static void renderObject(Object obj, StringBuilder buf, ObjectViewConfig config) throws ObjectTooLargeException {
        renderObject(obj, buf, config, 0);
    }

    @SuppressWarnings({"unchecked", "DuplicatedCode"})
    private static void renderObject(Object obj, StringBuilder buf, ObjectViewConfig config, int depth) throws ObjectTooLargeException {
        int expand = config.getDepth();
        boolean printParentFields = config.isPrintParentFields();
        boolean ignoreNullField = config.isIgnoreNullField();
        if (null == obj) {
            appendStringBuilder(buf, "null");
        } else {
            Class<?> clazz = obj.getClass();
            String className = clazz.getSimpleName();

            // 7种基础类型,直接输出@类型[值]
            if (obj instanceof Integer || obj instanceof Long || obj instanceof Float || obj instanceof Double || obj instanceof Short || obj instanceof Byte || obj instanceof Boolean) {
                appendStringBuilder(buf, format("@%s[%s]", className, obj));
            }

            // Char要特殊处理,因为有不可见字符的因素
            else if (obj instanceof Character) {
                Character c = (Character) obj;
                if (c >= 32 && c <= 126) {
                    // ASCII的可见字符
                    appendStringBuilder(buf, format("@%s[%s]", className, c));
                } else if (ASCII_MAP.containsKey((byte) c.charValue())) {
                    // ASCII的控制字符
                    appendStringBuilder(buf, format("@%s[%s]", className, ASCII_MAP.get((byte) c.charValue())));
                } else {
                    // 超过ASCII的编码范围
                    appendStringBuilder(buf, format("@%s[%s]", className, c));
                }
            }

            // 字符串类型单独处理
            else if (obj instanceof String) {
                appendStringBuilder(buf, "@");
                appendStringBuilder(buf, className);
                appendStringBuilder(buf, "[");
                for (Character c : ((String) obj).toCharArray()) {
                    switch (c) {
                        case '\n':
                            appendStringBuilder(buf, "\\n");
                            break;
                        case '\r':
                            appendStringBuilder(buf, "\\r");
                            break;
                        default:
                            appendStringBuilder(buf, c.toString());
                    }
                }
                appendStringBuilder(buf, "]");
            }

            // 集合类输出
            else if (obj instanceof Collection) {
                Collection<Object> collection = (Collection<Object>) obj;
                if (reachConfiguredDepth(depth, expand) || collection.isEmpty()) {
                    // 非根节点或空集合只展示摘要信息
                    appendStringBuilder(buf, format("@%s[size=%d]", className, collection.size()));
                } else {
                    // 展开展示
                    appendStringBuilder(buf, format("@%s[", className));
                    renderArray(collection.toArray(), buf, config, depth);
                    appendStringBuilder(buf, "]");
                }
            }

            // Map类输出
            else if (obj instanceof Map) {
                Map<Object, Object> map = (Map<Object, Object>) obj;
                if (reachConfiguredDepth(depth, expand) || map.isEmpty()) {
                    // 非根节点或空集合只展示摘要信息
                    appendStringBuilder(buf, format("@%s[size=%d]", className, map.size()));
                } else {
                    // 展开展示
                    appendStringBuilder(buf, format("@%s[", className));
                    renderArray(map.entrySet().toArray(), buf, config, depth);
                    appendStringBuilder(buf, "]");
                }
            }

            // Map.Entry类输出
            else if (obj instanceof Map.Entry) {
                Map.Entry<Object, Object> entry = (Map.Entry<Object, Object>) obj;
                renderObject(entry.getKey(), buf, config, depth);
                appendStringBuilder(buf, ":");
                renderObject(entry.getValue(), buf, config, depth);
            }

            // 数组类输出
            else if (obj.getClass().isArray()) {
                String typeName = obj.getClass().getSimpleName();

                switch (typeName) {
                    case "int[]": {
                        int[] array = (int[]) obj;
                        if (reachConfiguredDepth(depth, expand) || array.length == 0) {
                            // 非根节点或空集合只展示摘要信息
                            appendStringBuilder(buf, format("@%s[size=%d]", typeName, array.length));
                        } else {
                            // 展开展示
                            appendStringBuilder(buf, format("@%s[", className));
                            appendInnerIndentation(buf, config, depth);
                            for (Object o : array) {
                                renderObject(o, buf, config, depth + 1);
                                appendDelimiter(buf);
                            }
                            appendOuterIndentation(buf, config, depth);
                            appendStringBuilder(buf, "]");
                        }
                        break;
                    }
                    case "long[]": {
                        long[] array = (long[]) obj;
                        if (reachConfiguredDepth(depth, expand) || array.length == 0) {
                            // 非根节点或空集合只展示摘要信息
                            appendStringBuilder(buf, format("@%s[size=%d]", typeName, array.length));
                        } else {
                            // 展开展示
                            appendStringBuilder(buf, format("@%s[", className));
                            appendInnerIndentation(buf, config, depth);
                            for (Object o : array) {
                                renderObject(o, buf, config, depth + 1);
                                appendDelimiter(buf);
                            }
                            appendOuterIndentation(buf, config, depth);
                            appendStringBuilder(buf, "]");
                        }
                        break;
                    }
                    case "short[]": {
                        short[] array = (short[]) obj;
                        if (reachConfiguredDepth(depth, expand) || array.length == 0) {
                            // 非根节点或空集合只展示摘要信息
                            appendStringBuilder(buf, format("@%s[size=%d]", typeName, array.length));
                        } else {
                            // 展开展示
                            appendStringBuilder(buf, format("@%s[", className));
                            appendInnerIndentation(buf, config, depth);
                            for (Object o : array) {
                                renderObject(o, buf, config, depth + 1);
                                appendDelimiter(buf);
                            }
                            appendOuterIndentation(buf, config, depth);
                            appendStringBuilder(buf, "]");
                        }
                        break;
                    }
                    case "float[]": {
                        float[] array = (float[]) obj;
                        if (reachConfiguredDepth(depth, expand) || array.length == 0) {
                            // 非根节点或空集合只展示摘要信息
                            appendStringBuilder(buf, format("@%s[size=%d]", typeName, array.length));
                        } else {
                            // 展开展示
                            appendStringBuilder(buf, format("@%s[", className));
                            appendInnerIndentation(buf, config, depth);
                            for (Object o : array) {
                                renderObject(o, buf, config, depth + 1);
                                appendDelimiter(buf);
                            }
                            appendOuterIndentation(buf, config, depth);
                            appendStringBuilder(buf, "]");
                        }
                        break;
                    }
                    case "double[]": {
                        double[] array = (double[]) obj;
                        if (reachConfiguredDepth(depth, expand) || array.length == 0) {
                            // 非根节点或空集合只展示摘要信息
                            appendStringBuilder(buf, format("@%s[size=%d]", typeName, array.length));
                        } else {
                            // 展开展示
                            appendStringBuilder(buf, format("@%s[", className));
                            appendInnerIndentation(buf, config, depth);
                            for (Object o : array) {
                                renderObject(o, buf, config, depth + 1);
                                appendDelimiter(buf);
                            }
                            appendOuterIndentation(buf, config, depth);
                            appendStringBuilder(buf, "]");
                        }
                        break;
                    }
                    case "boolean[]": {
                        boolean[] array = (boolean[]) obj;
                        if (reachConfiguredDepth(depth, expand) || array.length == 0) {
                            // 非根节点或空集合只展示摘要信息
                            appendStringBuilder(buf, format("@%s[size=%d]", typeName, array.length));
                        } else {
                            // 展开展示
                            appendStringBuilder(buf, format("@%s[", className));
                            appendInnerIndentation(buf, config, depth);
                            for (Object o : array) {
                                renderObject(o, buf, config, depth + 1);
                                appendDelimiter(buf);
                            }
                            appendOuterIndentation(buf, config, depth);
                            appendStringBuilder(buf, "]");
                        }
                        break;
                    }
                    case "char[]": {
                        char[] array = (char[]) obj;
                        if (reachConfiguredDepth(depth, expand) || array.length == 0) {
                            // 非根节点或空集合只展示摘要信息
                            appendStringBuilder(buf, format("@%s[size=%d]", typeName, array.length));
                        } else {
                            // 展开展示
                            appendStringBuilder(buf, format("@%s[", className));
                            appendInnerIndentation(buf, config, depth);
                            for (Object o : array) {
                                renderObject(o, buf, config, depth + 1);
                                appendDelimiter(buf);
                            }
                            appendOuterIndentation(buf, config, depth);
                            appendStringBuilder(buf, "]");
                        }
                        break;
                    }
                    case "byte[]": {
                        byte[] array = (byte[]) obj;
                        if (reachConfiguredDepth(depth, expand) || array.length == 0) {
                            // 非根节点或空集合只展示摘要信息
                            appendStringBuilder(buf, format("@%s[size=%d]", typeName, array.length));
                        } else {
                            // 展开展示
                            appendStringBuilder(buf, format("@%s[", className));
                            appendInnerIndentation(buf, config, depth);
                            for (Object o : array) {
                                renderObject(o, buf, config, depth + 1);
                                appendDelimiter(buf);
                            }
                            appendOuterIndentation(buf, config, depth);
                            appendStringBuilder(buf, "]");
                        }
                        break;
                    }
                    default: {
                        Object[] array = (Object[]) obj;
                        if (reachConfiguredDepth(depth, expand) || array.length == 0) {
                            // 非根节点或空集合只展示摘要信息
                            appendStringBuilder(buf, format("@%s[size=%d]", typeName, array.length));
                        } else {
                            // 展开展示
                            appendStringBuilder(buf, format("@%s[", className));
                            renderArray(array, buf, config, depth);
                            appendStringBuilder(buf, "]");
                        }
                        break;
                    }
                }
            }

            // Throwable输出
            else if (obj instanceof Throwable) {
                if (reachConfiguredDepth(depth, expand)) {
                    appendStringBuilder(buf, format("@%s[%s]", className, obj));
                } else {
                    Throwable throwable = (Throwable) obj;
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    throwable.printStackTrace(pw);
                    appendStringBuilder(buf, sw.toString());
                }

            }

            // Enum输出
            else if (obj instanceof Enum<?>) {
                appendStringBuilder(buf, format("@%s[%s]", className, obj));
            }

            // Date输出
            else if (obj instanceof Date) {
                appendStringBuilder(buf, format("@%s[%s]", className, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS").format(obj)));
            }

            // LocalDate输出
            else if (obj instanceof LocalDate) {
                LocalDate localDate = (LocalDate) obj;
                appendStringBuilder(buf, format("@%s[%s]", className, localDate));
            }

            // LocalDateTime输出
            else if (obj instanceof LocalDateTime) {
                LocalDateTime localDateTime = (LocalDateTime) obj;
                appendStringBuilder(buf, format("@%s[%s]", className, localDateTime));
            }

            // 普通Object输出
            else {
                if (reachConfiguredDepth(depth, expand)) {
                    appendStringBuilder(buf, format("@%s[%s]", className, obj));
                } else {
                    appendStringBuilder(buf, format("@%s[", className));
                    List<Field> fields = new ArrayList<>();
                    Class objClass = obj.getClass();
                    if (printParentFields) {
                        // 当父类为null的时候说明到达了最上层的父类(Object类).
                        while (objClass != null) {
                            fields.addAll(Arrays.asList(objClass.getDeclaredFields()));
                            objClass = objClass.getSuperclass();
                        }
                    } else {
                        fields.addAll(Arrays.asList(objClass.getDeclaredFields()));
                    }
                    for (Field field : fields) {
                        field.setAccessible(true);
                        try {
                            Object value = field.get(obj);
                            if (Objects.isNull(value) && ignoreNullField) {
                                continue;
                            }

                            if (String.class == field.getType() && field.getAnnotation(Sensitive.class) != null) {
                                value = SensitizeFilter.SENSITIVE_DATA;
                            }

                            appendInnerIndentation(buf, config, depth);
                            appendStringBuilder(buf, field.getName());
                            appendStringBuilder(buf, "=");
                            renderObject(value, buf, config, depth + 1);
                            appendDelimiter(buf);
                        } catch (ObjectTooLargeException t) {
                            buf.append("...");
                            break;
                        } catch (Throwable t) {
                            // ignore
                        }
                    }
                    appendOuterIndentation(buf, config, depth);
                    appendStringBuilder(buf, "]");
                }
            }
        }
    }

    private static void renderArray(Object[] collection, StringBuilder buf, ObjectViewConfig config, int depth) throws ObjectTooLargeException {
        appendInnerIndentation(buf, config, depth);
        for (Object o : collection) {
            renderObject(o, buf, config, depth + 1);
            appendDelimiter(buf);
        }
        appendOuterIndentation(buf, config, depth);
    }

    /**
     * 是否达到指定展开层数
     *
     * @param depth  当前节点的深度
     * @param expand 展开极限
     * @return true:达到 / false:未达到
     */
    private static boolean reachConfiguredDepth(int depth, int expand) {
        return depth >= expand;
    }

    /**
     * append string to a string builder, with upper limit check
     *
     * @param buf  the StringBuilder buffer
     * @param data the data to be appended
     * @throws ObjectTooLargeException if the size has exceeded the upper limit
     */
    private static void appendStringBuilder(StringBuilder buf, String data) throws ObjectTooLargeException {
        if (buf.length() + data.length() > MAX_OBJECT_LENGTH) {
            throw new ObjectTooLargeException("Object size exceeds size limit: " + MAX_OBJECT_LENGTH);
        }
        buf.append(data);
    }

    private static void appendInnerIndentation(StringBuilder buf, ObjectViewConfig config, int depth) throws ObjectTooLargeException {
        if (!config.isInline()) {
            appendStringBuilder(buf, ENTER);
            for (int i = 0; i < depth + 1; i++) {
                appendStringBuilder(buf, TAB);
            }
        }
    }

    private static void appendOuterIndentation(StringBuilder buf, ObjectViewConfig config, int depth) throws ObjectTooLargeException {
        if (!config.isInline()) {
            appendStringBuilder(buf, ENTER);
            for (int i = 0; i < depth; i++) {
                appendStringBuilder(buf, TAB);
            }
        }
    }

    private static void appendDelimiter(StringBuilder buf) throws ObjectTooLargeException {
        appendStringBuilder(buf, ", ");
    }

    private static class ObjectTooLargeException extends Exception {

        private static long serialVersionUID = 8282096990309594268L;

        ObjectTooLargeException(String message) {
            super(message);
        }
    }

    /**
     * object打印配置
     */
    public static class ObjectViewConfig {
        /**
         * 展开层数
         */
        private int depth = 1;
        /**
         * 是否使用fastjson序列化
         */
        private boolean usingJson = false;
        /**
         * 是否打印父类属性
         */
        private boolean printParentFields = true;
        /**
         * 是否不打印对象的null属性
         */
        private boolean ignoreNullField = true;
        /**
         * 是否单行打印
         */
        private boolean inline = true;

        public ObjectViewConfig() {
        }

        public ObjectViewConfig(int depth, boolean usingJson, boolean printParentFields, boolean ignoreNullField, boolean inline) {
            this.depth = depth;
            this.usingJson = usingJson;
            this.printParentFields = printParentFields;
            this.ignoreNullField = ignoreNullField;
            this.inline = inline;
        }

        public int getDepth() {
            return depth;
        }

        public void setDepth(int depth) {
            this.depth = depth;
        }

        public boolean isUsingJson() {
            return usingJson;
        }

        public void setUsingJson(boolean usingJson) {
            this.usingJson = usingJson;
        }

        public boolean isPrintParentFields() {
            return printParentFields;
        }

        public void setPrintParentFields(boolean printParentFields) {
            this.printParentFields = printParentFields;
        }

        public boolean isIgnoreNullField() {
            return ignoreNullField;
        }

        public void setIgnoreNullField(boolean ignoreNullField) {
            this.ignoreNullField = ignoreNullField;
        }

        public boolean isInline() {
            return inline;
        }

        public void setInline(boolean inline) {
            this.inline = inline;
        }
    }

}
