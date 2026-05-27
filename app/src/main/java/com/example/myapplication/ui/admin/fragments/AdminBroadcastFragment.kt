package com.example.myapplication.ui.admin.fragments

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentAdminBroadcastBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdminBroadcastFragment : Fragment(R.layout.fragment_admin_broadcast) {

    private var _binding: FragmentAdminBroadcastBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAdminBroadcastBinding.bind(view)

        binding.btnSendBroadcast.setOnClickListener {
            val subject = binding.etSubject.text.toString().trim()
            val message = binding.etMessage.text.toString().trim()

            if (subject.isEmpty() || message.isEmpty()) {
                Toast.makeText(context, "Harap lengkapi Subjek dan Detail Pesan!", Toast.LENGTH_SHORT).show()

            }

            // Simulasi penambahan entri pengumuman baru ke dalam daftar riwayat dinamis
            addNewBroadcastToHistory(subject, message)

            // Mengosongkan isian form setelah broadcast berhasil terkirim
            binding.etSubject.text.clear()
            binding.etMessage.text.clear()

            Toast.makeText(context, "Pengumuman berhasil di-broadcast ke seluruh Tutor!", Toast.LENGTH_LONG).show()
        }
    }

    private fun addNewBroadcastToHistory(subject: String, message: String) {
        val context = requireContext()
        val cardView = CardView(context).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
                bottomMargin = 16
            }
            radius = 24f
            setCardBackgroundColor(Color.parseColor("#1E293B"))
        }

        val linearLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val tvSubject = TextView(context).apply {
            text = "📢 $subject"
            setTextColor(Color.parseColor("#06B6D4"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setTypeface(null, Typeface.BOLD)
        }

        val tvMessage = TextView(context).apply {
            text = message
            setTextColor(Color.parseColor("#CBD5E1"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setPadding(0, 8, 0, 0)
        }

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = timeFormat.format(Calendar.getInstance().time)

        val tvMeta = TextView(context).apply {
            text = "Baru saja dikirim • Jam $currentTime"
            setTextColor(Color.parseColor("#475569"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
            setPadding(0, 12, 0, 0)
        }

        linearLayout.addView(tvSubject)
        linearLayout.addView(tvMessage)
        linearLayout.addView(tvMeta)
        cardView.addView(linearLayout)

        // Taruh card riwayat terbaru di baris paling atas (index 0)
        binding.llBroadcastHistory.addView(cardView, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}