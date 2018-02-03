package cz.koto.kotiheating

import android.arch.lifecycle.ViewModel
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cz.koto.kotiheating.common.vmb
import cz.koto.kotiheating.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), MainView {

	private val vmb by vmb<MainViewModel, ActivityMainBinding>(R.layout.activity_main) {
		MainViewModel()
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		vmb
	}

}

interface MainView {

}

class MainViewModel : ViewModel() {


	init {

	}
}

