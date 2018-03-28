package cz.koto.kotiheating.ui;


class StatusItem(var temperature: Float, val hour: Int) : Comparable<StatusItem> {

	override fun compareTo(other: StatusItem): Int {
		if (other.temperature == temperature && other.hour == hour) {
			return 0
		} else if (other.hour == hour) {
			return if (other.temperature > temperature) -1 else 1
		} else {
			return if (other.hour > hour) -1 else 1
		}
	}

	override fun toString(): String {
		return "StatusItem(temperature=$temperature, hour=$hour)"
	}


}
