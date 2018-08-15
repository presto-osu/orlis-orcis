package org.itishka.pointim.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.itishka.pointim.model.point.TextWithImages;

import java.lang.reflect.Type;

/**
 * Created by Tishka17 on 25.10.2014.
 */
public class TextSerializer implements JsonSerializer<TextWithImages> {

    @Override
    public JsonElement serialize(TextWithImages src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.originalText.toString());
    }
}
