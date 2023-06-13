package com.example.autos.ui.vehiculos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.autos.data.local.AutosDatabase
import com.example.autos.databinding.CarItemBinding
import com.example.autos.databinding.FragmentVehiclesBinding
import com.example.autos.domain.DomainCoche
import com.example.autos.repository.AutosRepository
import com.example.autos.util.AutoListener

private const val TAG = "xxVf"

class VehiculosFragment : Fragment() {

    private var _binding: FragmentVehiclesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val app = requireNotNull(activity).application
        val db = AutosDatabase.getDatabase(app)
        val repository = AutosRepository(db, app)
        val viewModelFactory = VehiculosViewModelFactory(repository, app)
        val viewModel = ViewModelProvider(this, viewModelFactory)[VehiculosViewModel::class.java]

        _binding = FragmentVehiclesBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        val navController = this.findNavController()
        val addCarFab = binding.addCar
        addCarFab.setOnClickListener {
            navController.navigate(VehiculosFragmentDirections.actionNavVehiclesToNavNewCar())
        }

        val adapter = AutosAdapter(AutoListener {
            repository.setActualAutoId(it.id)
            navController.navigate(VehiculosFragmentDirections.actionNavVehiclesToNavVehicle())
        })

        binding.vehiclesList.adapter = adapter

        viewModel.vehicles.observe(viewLifecycleOwner){
            if (it != null) {
//                Log.d(TAG, "vehiculos: ${it}")
                adapter.submitList(it)
                binding.tvNoVehicles.visibility = View.GONE
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class AutosAdapter(val clickListener: AutoListener): ListAdapter<DomainCoche, AutosAdapter.AutoViewHolder>(AutoDiffCallBack){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AutoViewHolder {
        return AutoViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: AutoViewHolder, position: Int) {
        val item = getItem(position)

        holder.itemView.setOnClickListener{
            clickListener.onClick(item)
        }
        holder.bind(item)
    }


    class AutoViewHolder(val binding: CarItemBinding): RecyclerView.ViewHolder(binding.root){
        companion object{
            fun from(parent: ViewGroup): AutoViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = CarItemBinding.inflate(layoutInflater, parent, false)
                return AutoViewHolder(binding)
            }
        }

        fun bind(item: DomainCoche) {
            binding.auto = item
            binding.executePendingBindings()
        }
    }

    companion object AutoDiffCallBack: DiffUtil.ItemCallback<DomainCoche>() {
        override fun areItemsTheSame(oldItem: DomainCoche, newItem: DomainCoche): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DomainCoche, newItem: DomainCoche): Boolean {
            return oldItem == newItem
        }

    }
}