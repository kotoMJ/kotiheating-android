package cz.koto.kotiheating.ui.profile

import android.content.Context
import android.content.DialogInterface
import android.databinding.DataBindingUtil
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import cz.koto.kotiheating.R
import cz.koto.kotiheating.databinding.DialogProfileBinding
import cz.koto.kotiheating.ui.MainActivityView
import cz.koto.kotiheating.ui.MainViewModel

fun createProfileDialog(context: Context,
						viewModel: MainViewModel,
						view: MainActivityView,
						doOnCloseLister: DialogInterface.OnClickListener): AlertDialog {
	val dialogBuilder = AlertDialog.Builder(context)
	val binding: DialogProfileBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_profile, null, false);
	dialogBuilder.setView(binding.root)
	val dialog: AlertDialog = dialogBuilder.setPositiveButton(R.string.close, doOnCloseLister).create()
	binding.viewModel = viewModel
	binding.view = view
	dialog.setCanceledOnTouchOutside(false)
	dialog.hide()
	return dialog
}
