package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
class NewPostFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private val viewModel: PostViewModel by activityViewModels()

    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(
            inflater, container, false
        )

        arguments?.textArg?.let(binding.edit::setText)

        val contract =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == ImagePicker.RESULT_ERROR) {
                    Toast.makeText(requireContext(), R.string.no_intent, Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }
                val uri = result.data?.data ?: return@registerForActivityResult
                viewModel.savePhoto(PhotoModel(uri, uri.toFile()))
            }

        viewModel.photo.observe(viewLifecycleOwner) {
            if (it == null) {
                binding.previewContainer.isGone = true
                return@observe
            }
            binding.previewContainer.isVisible = true
            binding.preview.setImageURI(it.uri)
        }

        binding.clear.setOnClickListener {
            viewModel.clear()
        }

        binding.getGallery.setOnClickListener {

            ImagePicker.with(this).galleryOnly().crop().maxResultSize(1024, 1024)
                .createIntent(contract::launch) // launch- функция которая принимает на вход Интент
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this).cameraOnly().crop().maxResultSize(1024, 1024)
                .createIntent(contract::launch) // launch- функция которая принимает на вход Интент
        }

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.new_post_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
                R.id.save -> {
                    viewModel.changeContent(binding.edit.text.toString())
                    viewModel.save()
                    AndroidUtils.hideKeyboard(requireView())
                    findNavController().navigateUp()
                    true
                }

                R.id.signOut -> {

                    MaterialAlertDialogBuilder(requireActivity()) // Используйте `this` в активити
                        .setTitle(R.string.log_out).setMessage(R.string.sign_out)
                        .setPositiveButton(R.string.yes) { dialog, which ->
                            // выполните действия, когда пользователь выбирает "Да"
                            findNavController().navigate(R.id.feedFragment)
                            authViewModel.logout() //тут не особо уверен, оставлю так!!! если что понял!)
                        }.setNegativeButton(R.string.no) { dialog, which ->
                            // выполните действия, когда пользователь выбирает "Нет"
                        }.setCancelable(true).create().show()

                    true
                }

                else -> false
            }


        }, viewLifecycleOwner)

        viewModel.postCreated.observe(viewLifecycleOwner) {
            viewModel.loadPosts() // <----
            findNavController().navigateUp()
        }
        return binding.root
    }
}