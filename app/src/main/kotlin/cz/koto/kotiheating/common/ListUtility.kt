package cz.koto.kotiheating.common

fun compareLists(list1: List<Comparable<*>>, list2: List<Comparable<*>>): Int {
	for (i in 0 until Math.min(list1.size, list2.size)) {
		val elem1 = list1[i]
		val elem2 = list2[i]

		if (elem1.javaClass != elem2.javaClass) {
			TODO("Decide what to do when you encounter values of different classes")
		}

		compareValues(elem1, elem2).let {
			if (it != 0) return it
		}
	}
	return compareValues(list1.size, list2.size)
}