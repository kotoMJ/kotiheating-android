package cz.koto.kotiheating.common

/**
 * https://material.io/guidelines/style/color.html#color-color-palette
 */
fun getColorForTemperature(temperature: Float?): String {

	when (temperature) {
		null -> return ""
		/* marginal freeze values : Light Blue */
		in 0..1 -> return "#81D4FA"//200
		in 1..2 -> return "#81D4FA"//200
		in 2..3 -> return "#81D4FA"//200
		in 3..4 -> return "#81D4FA"//200

		/* NON-FREEZE minimum : Cyan */
		in 4..5 -> return "#B2EBF2"//100
		in 5..6 -> return "#B2EBF2"//100
		in 6..7 -> return "#80DEEA"//200
		in 7..8 -> return "#4DD0E1"//300
		in 8..9 -> return "#26C6DA"//400
		in 9..10 -> return "#00BCD4"//500
		in 10..11 -> return "#00ACC1"//600
		in 11..12 -> return "#0097A7"//700
		in 12..13 -> return "#00838F"//800
		in 13..14 -> return "#006064"//900

		/* Sleep values set : Light Green*/
		in 14..15 -> return "#DCEDC8"//100
		in 15..16 -> return "#C5E1A5"//200
		in 16..17 -> return "#AED581"//300
		in 17..18 -> return "#9CCC65"//400
		in 18..19 -> return "#8BC34A"//500

		/* Living values set : Amber */
		in 19..20 -> return "#FFE082"//200
		in 20..21 -> return "#FFCA28"//400
		in 21..22 -> return "#FFC107"//500
		in 22..23 -> return "#FFB300"//600
		in 13..24 -> return "#FFA000"//700
		in 24..25 -> return "#FF8F00"//800
		in 25..26 -> return "#FF6F00"//900

		/* Overheating but acceptable values : Orange*/
		in 26..27 -> return "#E65100"//900
		in 27..28 -> return "#E65100"//900
		in 28..29 -> return "#E65100"//900
		in 29..30 -> return "#E65100"//900

		/* Overheating alert values : Deep Orange*/
		in 30..1000 -> return "#FF3D00"//A400

		//All minus degrees : Light Blue
		else -> return "#E1F5FE"//50
	}

}