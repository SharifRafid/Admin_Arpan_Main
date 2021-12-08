package admin.arpan.delivery.utils

import admin.arpan.delivery.R
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import kotlinx.android.synthetic.main.pop_up_edit_text.view.*

class PopUpEditText private constructor(val context : Context, val completeListener : CompleteListener?) {

    class create(val context : Context){

        private var completeListener : CompleteListener? = null

        fun setCompleteListener(listener: CompleteListener?) : create{
            this.completeListener = listener
            return this
        }

        fun show(){
            val dialog = AlertDialog.Builder(context).create()
            val dialogView = LayoutInflater.from(context).inflate(R.layout.pop_up_edit_text, null)
            dialog.setView(dialogView)
            dialog.show()
            dialogView.confirmButtonDialogPopUp.setOnClickListener {
                dialog.dismiss()
                completeListener?.onTextSubmitted(dialogView.edtPopUpEditTextMain.text.toString())
            }
        }
    }

}

interface CompleteListener {
    fun onTextSubmitted(text: String)
}
