package com.example.autos.ui.refueling

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.autos.R
import com.example.autos.data.local.AutosDatabase
import com.example.autos.databinding.FragmentRepostajesBinding
import com.example.autos.databinding.RepostajeItemBinding
import com.example.autos.domain.DomainRefueling
import com.example.autos.repository.AutosRepository
import com.example.autos.ui.home.HomeFragment

private const val TAG = "xxReposF"

class RepostajesFragment: Fragment() {

    val viewModel: RefuelingViewModel by activityViewModels { RefuelingViewModel.Factory }
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Log.d(TAG,"onCreate")
        preferences = requireContext().applicationContext.getSharedPreferences("preferences",Context.MODE_PRIVATE)
        val actualAutoId = preferences.getInt("auto_id", -1)
        if (viewModel.carId != actualAutoId)
            viewModel.reloadData(actualAutoId)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentRepostajesBinding>(inflater, R.layout.fragment_repostajes, container, false)


        val adapter = RepostajesAdapter()
        binding.listRepostajes.adapter = adapter

        viewModel.repostajes.observe(viewLifecycleOwner){
//            Log.d(TAG,"observando repostajes: $it")
            if (!it.isNullOrEmpty()){
                adapter.submitList(it)
                binding.tvNoRegister.visibility = View.GONE
            }
            viewModel.resetStatus()
        }
        return binding.root
    }

}

class RepostajesAdapter(): ListAdapter<DomainRefueling, RepostajesAdapter.RepostajeViewHolder>(RefuelDiffCallBack){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepostajeViewHolder {
        return RepostajeViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RepostajeViewHolder, position: Int) {
        val item = getItem(position)

        val itemsCount = this.itemCount
        if (position < itemsCount -1){
            item.recorrido = item.kms - (getItem(position +1).kms)
        } else {
            item.recorrido = item.kms - HomeFragment.getInitialKms()
        }
        holder.bind(item)
        holder.itemView.setOnClickListener{
            item.expand = !item.expand
            notifyItemChanged(position)
        }
    }

    class RepostajeViewHolder(
        val binding: RepostajeItemBinding
    ): RecyclerView.ViewHolder(binding.root) {

        companion object{
            fun from(parent: ViewGroup): RepostajeViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RepostajeItemBinding.inflate(layoutInflater, parent, false)
                return RepostajeViewHolder(binding)
            }
        }

        fun bind(item: DomainRefueling) {
            binding.refuel = item

            binding.tvEurosLitro.visibility = if (item.expand) View.VISIBLE else View.GONE
            binding.tvEuroLitroData.visibility = if (item.expand) View.VISIBLE else View.GONE
            binding.tvLitros.visibility = if (item.expand) View.VISIBLE else View.GONE
            binding.tvLitrosData.visibility = if (item.expand) View.VISIBLE else View.GONE
            binding.tvEuros.visibility = if (item.expand) View.VISIBLE else View.GONE
            binding.tvCost.visibility = if (item.expand) View.VISIBLE else View.GONE

            binding.executePendingBindings()
        }
    }

    companion object RefuelDiffCallBack: DiffUtil.ItemCallback<DomainRefueling>(){
        override fun areItemsTheSame(
            oldItem: DomainRefueling,
            newItem: DomainRefueling
        ): Boolean {
            return oldItem.refuelId == newItem.refuelId
        }

        override fun areContentsTheSame(
            oldItem: DomainRefueling,
            newItem: DomainRefueling
        ): Boolean {
            return oldItem == newItem
        }
    }
}