package cz.koto.kotiheating.model.db

import android.arch.persistence.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import cz.koto.ktools.inject
import java.util.Date

class Converters {

	val gson by inject<Gson>()

	@TypeConverter
	fun fromTimestamp(value: Long?): Date? {
		return if (value == null) null else Date(value)
	}

	@TypeConverter
	fun dateToTimestamp(date: Date?): Long? {
		return date?.time
	}

	@TypeConverter
	fun mapToString(map: Map<String, Double>?): String? {
		return map?.let { gson.toJson(it) }
	}

	@TypeConverter
	fun stringToMap(json: String?): Map<String, Double>? {
		return gson.fromJson(json, HashMap<String, Double>().javaClass)
	}

	@TypeConverter
	fun listListFloatToString(listListInt: MutableList<MutableList<Int>>?): String? {
		return gson.toJson(listListInt)
	}


	@TypeConverter
	fun stringToListListFloat(listListIntAsString: String?): MutableList<MutableList<Int>>? {
		if (listListIntAsString == null) {
			return mutableListOf()
		}
		val listType = object : TypeToken<MutableList<MutableList<Int>>>() {}.type

		return gson.fromJson(listListIntAsString, listType);
	}
}