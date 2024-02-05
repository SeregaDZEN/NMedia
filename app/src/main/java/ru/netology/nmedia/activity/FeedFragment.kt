package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel

class FeedFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.edit(post)

            }

            override fun onLike(post: Post) {
                viewModel.like(post)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }
        })
        binding.list.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadPosts()
//            val handler = Handler(Looper.getMainLooper())
//            handler.postDelayed({binding.swipeRefresh.isRefreshing = false}, 3000)

        }


        binding.buttonScroll.setOnClickListener {
            binding.buttonScroll.visibility = View.GONE
            binding.bell.visibility = View.GONE
            binding.countPost.text = ""
        }


        viewModel.newerCount.observe(viewLifecycleOwner) { postCount ->
            if (postCount > 0) {
                binding.buttonScroll.visibility = View.VISIBLE
                binding.countPost.text = postCount.toString()
                binding.bell.visibility = View.VISIBLE
            } else binding.buttonScroll.visibility = View.GONE

        }

        viewModel.data.observe(viewLifecycleOwner) { state ->

            adapter.submitList(state.posts)
            val newPost = state.posts.size > adapter.currentList.size && adapter.itemCount > 0
            if (newPost) binding.list.smoothScrollToPosition(0)
            binding.buttonScroll.visibility = View.VISIBLE
            binding.bell.visibility = View.VISIBLE

        }


        viewModel.dataState.observe(viewLifecycleOwner) { state ->

            if (state.errorMessage.isNotEmpty()) {
                Snackbar.make(binding.root, state.errorMessage, Snackbar.LENGTH_INDEFINITE)
                    .setAction(
                        "ok"
                    ) {
                        Toast.makeText(view?.context, "Кнопка нажата!", Toast.LENGTH_SHORT)
                            .show()
                    }.setAnchorView(binding.fab).show()

            }
            if (state.error) {
                Snackbar.make(binding.root, "ошибка сети", Snackbar.LENGTH_LONG)
                    .setAction("retry") {
                        viewModel.loadPosts()
                    }.setAnchorView(binding.fab).show()
            }
            binding.progress.isVisible = state.loading
            binding.swipeRefresh.isRefreshing = state.loading


        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        return binding.root

    }

}
