package com.dms.util;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import rx.Observable;


public final class JSONUtil {

    private static final Logger log = LoggerFactory.getLogger(JSONUtil.class);

    /**
     * {@link com.fasterxml.jackson.databind.ObjectMapper} instance
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        SimpleModule customFieldMapping = new SimpleModule("Custom serialization");

        customFieldMapping.addSerializer(LocalDate.class, new LocalDateJsonSerializer());
        customFieldMapping.addDeserializer(LocalDate.class, new LocalDateJsonDeserializer());

        customFieldMapping.addSerializer(LocalDateTime.class, new LocalDateTimeJsonSerializer());
        customFieldMapping.addDeserializer(LocalDateTime.class, new LocalDateTimeJsonDeserializer());

        customFieldMapping.addSerializer(Date.class, new DateJsonSerializer());
        customFieldMapping.addDeserializer(Date.class, new DateJsonDeserializer());

        customFieldMapping.addSerializer(BigDecimal.class, new BigDecimalJsonSerializer());
        customFieldMapping.addDeserializer(BigDecimal.class, new BigDecimalJsonDeserializer());

//        customFieldMapping.addSerializer(IntEnum.class, new EnumJsonSerializer());
//        customFieldMapping.addDeserializer(IntEnum.class, new IntEnumJsonDeserializer());

//        registerTypeAdapter(LocalDateTime.class, (o, aClass) -> o == null ? null : LocalDateTime.parse((String) o, DateTimeFormatter.ISO_LOCAL_DATE_TIME), o -> o == null ? null : o.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
//        registerTypeAdapter(BigDecimal.class, (o, aClass) -> o == null ? null : new BigDecimal((String) o), o -> o == null ? null : o.toString());
//
        MAPPER.registerModule(customFieldMapping);
    }

    public static Observable<JsonNode> parseAsTreeObservable(String json) {
        return Observable.create(subscriber -> {
            try {
                subscriber.onNext(MAPPER.readTree(json));
                subscriber.onCompleted();
            } catch (IOException e) {
                subscriber.onError(e);
            }
        });
    }

    /**
     * Emits objects of type T. If input value jsonNode is array - emits multiple values (assuming that each element
     * in the array is an object node) or a single value
     *
     * @param jsonNode can be array or object node only
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> Observable<T> parseObservable(JsonNode jsonNode, Class<T> clazz) {
        if (jsonNode.isArray()) {
            if (jsonNode.size() == 0) {
                return Observable.empty();
            }
            return Observable.<List<T>>create(subscriber -> {
                try {
                    List<T> parsedList = new ArrayList<>(jsonNode.size());
                    for (int i = 0; i < jsonNode.size(); i++) {
                        parsedList.add(parseChecked(jsonNode.get(i), clazz));
                    }

                    subscriber.onNext(parsedList);
                    subscriber.onCompleted();
                } catch (JsonProcessingException e) {
                    subscriber.onError(e);
                }
            }).flatMap(Observable::from);//this is very important since it natively provides backpressure
        } else {
            return Observable.create(subscriber -> {
                try {
                    subscriber.onNext(parseChecked(jsonNode, clazz));
                    subscriber.onCompleted();
                } catch (JsonProcessingException e) {
                    subscriber.onError(e);
                }
            });
        }
    }

    public static <T> Observable<T> parseObservable(String json, Class<T> tClass) {
        return parseAsTreeObservable(json).flatMap(jsonNode -> parseObservable(jsonNode, tClass));
    }

    public static <T> Observable<T> parseObservable(InputStream is, Class<T> clazz) {
        return Observable.<T>create(subscriber -> {
            try {
                Iterator<T> iterator = MAPPER.reader(clazz).readValues(is);
                while (iterator.hasNext()) {
                    subscriber.onNext(iterator.next());
                }
            } catch (IOException e) {
                subscriber.onError(e);
                return;
            }

            subscriber.onCompleted();
        }).onBackpressureBuffer(512);
    }

    /**
     * Maps given object to JSON string.
     *
     * @param object object to be mapped
     * @return {@link java.util.Optional} value of JSON string
     */
    public static Optional<String> toJSONString(final Object object) {
        if (object == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(MAPPER.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

    /**
     * Maps given object to JSON string.
     *
     * @param object object to be mapped
     * @return {@link java.util.Optional} value of JSON string
     */
    public static String toJSONStringChecked(final Object object) throws JsonProcessingException {
        if (object == null) {
            return null;
        }

        return MAPPER.writeValueAsString(object);
    }

    /**
     * Maps given object to JSON string.
     *
     * @param object object to be mapped
     * @return {@link java.util.Optional} value of JSON string
     */
    public static Observable<String> toJSONStringObservable(final Object object) {
        if (object == null) {
            return Observable.just(null);
        }

        try {
            return Observable.just(MAPPER.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            return Observable.error(e);
        }
    }

    public static JsonNode toJsonNode(final Object obj) {
        return MAPPER.valueToTree(obj);
    }

    public static Observable<JsonNode> toJsonNodeObservable(final Object pojo) {
        return Observable.create(subscriber -> {
            try {
                subscriber.onNext(MAPPER.valueToTree(pojo));
                subscriber.onCompleted();
            } catch (IllegalArgumentException e) {
                subscriber.onError(e);
            }
        });
    }

    public static <T> Optional<T> parse(String source, Class<T> clazz) {
        try {
            return Optional.of(MAPPER.readValue(source, clazz));
        } catch (IOException e) {
            log.error("parse(): ", e);
            return Optional.empty();
        }
    }

    public static <T> T parseChecked(String source, Class<T> clazz) throws IOException {
        return MAPPER.readValue(source, clazz);
    }

    public static <T> Optional<T> parse(JsonNode jsonNode, Class<T> clazz) {
        try {
            return Optional.of(MAPPER.treeToValue(jsonNode, clazz));
        } catch (IOException e) {
            log.error("parse(): ", e);
            return Optional.empty();
        }
    }

    public static <T> T parseChecked(JsonNode jsonNode, Class<T> clazz) throws JsonProcessingException {
        return MAPPER.treeToValue(jsonNode, clazz);
    }

    public static <T> List<T> parseArrayToList(JsonNode jsonNode, Class<T> clazz) {
        List<T> list = new ArrayList<>(jsonNode.size());
        for (int i = 0; i < jsonNode.size(); i++) {
        	JsonNode node = jsonNode.get(i);
        	if(node != null) {
        		T clazzInstance = parse(jsonNode.get(i), clazz).get();
        		list.add(clazzInstance);
            }
        }
        return list;
    }

    public static <T> List<T> parseArrayToListChecked(JsonNode jsonNode, Class<T> clazz) throws JsonProcessingException {
        List<T> list = new ArrayList<>(jsonNode.size());
        for (int i = 0; i < jsonNode.size(); i++) {
            T clazzInstance = parseChecked(jsonNode.get(i), clazz);
            list.add(clazzInstance);
        }
        return list;
    }

    public static Stream<Map.Entry<String, JsonNode>> readFromFile(String filename) {
        try {
            InputStream is = JSONUtil.class.getClassLoader().getResourceAsStream(filename);
            Spliterator<Map.Entry<String, JsonNode>> spliterator = Spliterators.spliteratorUnknownSize(MAPPER.readTree(is).fields(), Spliterator.ORDERED);
            return StreamSupport.stream(spliterator, false);
        } catch (IOException e) {
            return Stream.empty();
        }
    }

    public static Optional<JsonNode> getFieldValue(String json, String fieldValue) {
        try {
            JsonNode node = MAPPER.readTree(json);
            if (node.has(fieldValue)) {
                return Optional.of(node.get(fieldValue));
            } else {
                return Optional.empty();
            }
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public static Optional<JsonNode> readTree(String json) {
        try {
            return Optional.of(MAPPER.readTree(json));
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static JsonNode jsonNodeFromString(String jsonString) throws JsonProcessingException {
        try {
            return MAPPER.readTree(jsonString);
        } catch (JsonProcessingException e) {
            throw e;
        } catch (IOException ignored) {
            //other IO errors are ignored
            return null;
        }
    }

    public static Map<String, Object> toFlatMap(JsonObject obj, char delimeter) {
        Map<String, Object> map = new HashMap<>();
        addKeys("", obj.getMap(), map, delimeter);
        return map;
    }

    private static void addKeys(String currentPath, Map<String, Object> node, Map<String, Object> map, char delimeter) {
        String pathPrefix = currentPath.isEmpty() ? Character.toString(delimeter) : currentPath + delimeter;
        for (Map.Entry<String, Object> entry : node.entrySet()) {

            if (entry.getValue() instanceof Map) {
                //noinspection unchecked
                addKeys(pathPrefix + entry.getKey(), (Map<String, Object>) entry.getValue(), map, delimeter);
            } else {
                map.put(pathPrefix + entry.getKey(), entry.getValue());
            }
        }
    }

    public static Map<String, Integer> parseAsStringIntMap(JsonNode node)
	{
		Map<String, Integer> map = new HashMap<>();
		Iterator<JsonNode> iterator = node.elements();
		while (iterator.hasNext())
		{
			JsonNode element = iterator.next();
			if(element!=null) {
				Iterator<String> fieldNames = element.fieldNames();
				while (fieldNames.hasNext())
				{
					String name = fieldNames.next();
					if(name!=null) {
						JsonNode value = element.get(name);
						if(value !=null)
							map.put(name, value.asInt());
					}
				}
			}
		}
		return map;
	}
    
    private static class LocalDateJsonSerializer extends JsonSerializer<LocalDate> {
        @Override
        public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null)
                gen.writeNull();
            else
                gen.writeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
    }

    private static class LocalDateJsonDeserializer extends JsonDeserializer<LocalDate> {
        @Override
        public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String o = p.getValueAsString();
            return o == null ? null : LocalDate.parse(o, DateTimeFormatter.ISO_LOCAL_DATE);
        }
    }

    private static class LocalDateTimeJsonSerializer extends JsonSerializer<LocalDateTime> {
        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null)
                gen.writeNull();
            else
                gen.writeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
    }

    private static class LocalDateTimeJsonDeserializer extends JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String o = p.getValueAsString();
            return o == null ? null : LocalDateTime.parse(o, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }

    private static class DateJsonSerializer extends JsonSerializer<Date> {
        @Override
        public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) gen.writeNull();
            else gen.writeNumber(value.getTime());
        }
    }

    private static class BigDecimalJsonSerializer extends JsonSerializer<BigDecimal> {
        @Override
        public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) gen.writeNull();
            else gen.writeString(value.toString());
        }
    }

    private static class DateJsonDeserializer extends JsonDeserializer<Date> {
        @Override
        public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            ObjectCodec oc = p.getCodec();
            JsonNode node = oc.readTree(p);
            if (node.isNull())
                return null;
            else if (node.isNumber())
                return new Date(node.asLong());
            else throw new IllegalArgumentException("Cannot deserialize Date from " + node.getNodeType().name());
        }
    }

    private static class BigDecimalJsonDeserializer extends JsonDeserializer<BigDecimal> {
        @Override
        public BigDecimal deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            ObjectCodec oc = p.getCodec();
            JsonNode node = oc.readTree(p);
            if (node.isNull())
                return null;
            else if (node.isTextual())
                return new BigDecimal(node.asText());
            else throw new IllegalArgumentException("Cannot deserialize Date from " + node.getNodeType().name());
        }
    }
}
