package ru.netology.nmedia.util


import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import javax.security.auth.callback.Callback

//class AuthenticationDialogFragment : DialogFragment() {
//
//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        // Используйте MaterialAlertDialogBuilder для создания диалога
//        return MaterialAlertDialogBuilder(requireContext())
//            .setTitle("Аутентификация")
//            .setMessage("Вы хотите пройти аутентификацию сейчас?")
//            // Добавьте кнопку действия, например "Да", и обработчик нажатия
//            .setPositiveButton("Да") { dialog, which ->
//                // Обработка согласия пользователя пройти аутентификацию
//            }
//            // Добавьте кнопку отмены действия, например "Отмена"
//            .setNegativeButton("Отмена", null)
//            .create()
//    }
//
//
//}
class AuthenticationDialogFragment(private val callback: () -> Unit) : DialogFragment() {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle("Аутентификация")
            .setMessage("Вы хотите пройти аутентификацию сейчас?")
            .setPositiveButton("Да") { dialog, which ->
                // Обработка согласия пользователя пройти аутентификацию
                callback.invoke()
            }
            .setNegativeButton("Отмена", null)
            .create()

    companion object {
        const val TAG = "PurchaseConfirmationDialog"
    }
}


