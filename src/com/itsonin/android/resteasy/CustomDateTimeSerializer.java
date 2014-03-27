package com.itsonin.android.resteasy;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author nkislitsin
 *
 */
public class CustomDateTimeSerializer extends JsonSerializer<Date> {

	public static final String ITSONIN_DATES = "yyyy-MM-dd'T'HH:mm:ss";

	@Override
	public void serialize(Date value, JsonGenerator gen,
			SerializerProvider provider) throws IOException,
			JsonProcessingException {
		SimpleDateFormat formatter = new SimpleDateFormat(ITSONIN_DATES);
		String formattedDate = formatter.format(value);

		gen.writeString(formattedDate);
		
	}
}
