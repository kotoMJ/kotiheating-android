package cz.koto.kotiheating.model.db

import android.arch.persistence.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import cz.koto.kotiheating.model.entity.ScheduleType
import cz.koto.ktools.inject
import java.util.Date
import kotlin.collections.HashMap

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
	fun scheduleTypeToString(scheduleType: ScheduleType?): String? {
		return scheduleType?.toString()
	}

	@TypeConverter
	fun stringToScheduleType(string: String?): ScheduleType? {
		return ScheduleType.fromString(string)
	}

	@TypeConverter
	fun listListFloatToString(listListFloat: MutableList<MutableList<Float>>?): String? {
		return gson.toJson(listListFloat)
	}


	@TypeConverter
	fun stringToListListFloat(listListFloatAsString: String?): MutableList<MutableList<Float>>? {
		if (listListFloatAsString == null) {
			return mutableListOf()
		}
		val listType = object : TypeToken<MutableList<MutableList<Float>>>() {}.type

		return gson.fromJson(listListFloatAsString, listType);
	}
}