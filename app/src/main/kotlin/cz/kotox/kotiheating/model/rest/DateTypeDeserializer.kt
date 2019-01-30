package cz.kotox.kotiheating.model.rest


import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Date
import java.util.Locale

class DateTypeDeserializer : JsonDeserializer<Date> {

	@Throws(JsonParseException::class)
	override fun deserialize(jsonElement: JsonElement, typeOF: Type, context: JsonDeserializationContext): Date {
		for (format in SUPPORTED_DATE_FORMATS) {
			try {
				return SimpleDateFormat(format, Locale.US).parse(jsonElement.asString)
			} catch (e: ParseException) {
			}

		}
		throw JsonParseException("Unparseable date: \"" + jsonElement.asString
				+ "\". Supported formats: \n" + Arrays.toString(SUPPORTED_DATE_FORMATS))
	}

	companion object {
		private val SUPPORTED_DATE_FORMATS = arrayOf(
				DateConst.DATE_FORMAT_REST,
				DateConst.DATE_FORMAT_TIMEZONE,
				DateConst.DATE_FORMAT_NO_TIMEZONE)
	}
}

object DateConst {

	val DATE_FORMAT_REST = "EEE MMM d HH:mm:ss 'UTC'zzzzz yyyy"
	val DATE_FORMAT_TIMEZONE = "yyyy-MM-dd'T'HH:mm:ssZ"
	val DATE_FORMAT_NO_TIMEZONE = "yyyy-MM-dd'T'HH:mm:ss'Z'"
}
