package admin.arpan.delivery.ui.feedback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import admin.arpan.delivery.R
import android.content.Context
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_user_feed_back.view.*


class UserFeedBackFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL,R.style.Theme_AdminArpan)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_feed_back, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.title_text_view.setOnClickListener {
            dismiss()
        }
        FirebaseFirestore.getInstance().collection("feedbacks")
            .document("feedbacks")
            .addSnapshotListener{ value, error ->
                error?.printStackTrace()
                if (value != null) {
                    val feedbacksList = value.get("feedbacks") as List<String>
                    view.listViewFeedBacks.adapter = ArrayAdapter(
                        activity as Context,
                        R.layout.textview_item_list_view,
                        R.id.topTitle,
                        feedbacksList
                    )
                }
            }
    }

}