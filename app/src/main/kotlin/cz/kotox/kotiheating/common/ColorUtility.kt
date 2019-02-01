package cz.kotox.kotiheating.common

import android.graphics.Color
import androidx.annotation.ColorInt

val SWIPE_REFRESH_COLORS = intArrayOf(
	android.R.color.holo_blue_bright,
	android.R.color.holo_green_light,
	android.R.color.holo_orange_light,
	android.R.color.holo_red_light
)

/**
 * https://material.io/guidelines/style/color.html#color-color-palette
 */
fun getColorForTemperature(temperature: Int?): String {

	when (temperature) {
		null -> return "#ffffff"

		/* marginal freeze values : Light Blue */
		in 0..10 -> return "#81D4FA"//200
		in 10..20 -> return "#81D4FA"//200
		in 20..30 -> return "#81D4FA"//200
		in 30..40 -> return "#81D4FA"//200

		/* NON-FREEZE minimum : Cyan */
		in 40..50 -> return "#B2EBF2"//100
		in 50..60 -> return "#B2EBF2"//100
		in 60..70 -> return "#80DEEA"//200
		in 70..80 -> return "#4DD0E1"//300
		in 80..90 -> return "#26C6DA"//400
		in 90..100 -> return "#00BCD4"//500
		in 100..110 -> return "#00ACC1"//600
		in 110..120 -> return "#0097A7"//700
		in 120..130 -> return "#00838F"//800
		in 130..140 -> return "#006064"//900

		/* Sleep values set : Light Green*/
		in 140..150 -> return "#DCEDC8"//100
		in 150..160 -> return "#C5E1A5"//200
		in 160..170 -> return "#AED581"//300
		in 170..180 -> return "#9CCC65"//400
		in 180..190 -> return "#8BC34A"//500

		/* Living values set : Amber */
		in 190..200 -> return "#FFE082"//200
		in 200..210 -> return "#FFCA28"//400
		in 210..220 -> return "#FFC107"//500
		in 220..230 -> return "#FFB300"//600
		in 130..240 -> return "#FFA000"//700
		in 240..250 -> return "#FF8F00"//800
		in 250..260 -> return "#FF6F00"//900

		/* Overheating but acceptable values : Orange*/
		in 260..270 -> return "#E65100"//900
		in 270..280 -> return "#E65100"//900
		in 280..290 -> return "#E65100"//900
		in 290..300 -> return "#E65100"//900

		/* Overheating alert values : Deep Orange*/
		in 300..1000 -> return "#FF3D00"//A400

		//All minus degrees : Light Blue
		else -> return "#E1F5FE"//50
	}

}

fun getTextColorByBackground(@ColorInt backGroundColor: Int): Int {
	//val backGroundColor = Color.parseColor(colorHexaString.replace("#", ""))
	return if ((Color.red(backGroundColor) * 0.299 + Color.green(backGroundColor) * 0.587 + Color.blue(backGroundColor) * 0.114) > 186) {
		adjustAlpha(Color.parseColor("#000000"), 0.6f)
	} else {
		adjustAlpha(Color.parseColor("#ffffff"), 0.6f)
	}
}

fun adjustAlpha(@ColorInt color: Int, factor: Float): Int {
	val alpha = Math.round((Color.alpha(color) * factor))
	val red = Color.red(color)
	val green = Color.green(color)
	val blue = Color.blue(color)
	return Color.argb(alpha, red, green, blue)
}